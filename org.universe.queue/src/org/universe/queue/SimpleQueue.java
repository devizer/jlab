package org.universe.queue;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.universe.System6;
import org.universe.jcl.apparency.ThreadSafe;
import org.universe.sql.JdbcCommand;
import org.universe.sql.SqlDialect;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

@ThreadSafe
// If connection thread safe, then SimpleQueue is thread safe.
public class SimpleQueue {

    public static final QueueStatistic LocalStat = new QueueStatistic();
    // static final int TRANSACTION_ISOLATION = Connection.TRANSACTION_SERIALIZABLE;

    static final int TRANSACTION_ISOLATION = Connection.TRANSACTION_REPEATABLE_READ;
    private Callable<DataSource> dataSourceFactory;
    SqlDialect _dialect;
    Object SyncDialect = new Object();

    public SimpleQueue(final Callable<DataSource> dataSourceFactory) {
        if (dataSourceFactory == null)
            throw new IllegalArgumentException("dataSourceFactory argument is null");

        this.dataSourceFactory = dataSourceFactory;
    }

    public SimpleQueue(final DataSource dataSource) {
        if (dataSource == null)
            throw new IllegalArgumentException("dataSource argument is null");

        this.dataSourceFactory = new Callable<DataSource>() {
            @Override
            public DataSource call() throws Exception {
                return dataSource;
            }
        };
    }

    public void publish(String queueName, byte[] message) throws Exception {
        publish(queueName, null, message);
    }

    // when optionalKey is not null - Insert only new message with the optionalKey.
    // returns true if key is new
    // optionalKey is null - Inserts always. Returns always true.
    public boolean publish(String queueName, String optionalKey, byte[] message) throws Exception {

        final String sql = "Insert Into SimpleQueue (" +
                "  Id, OptionalKey, QueueName, Message, CreatedAt, ModifiedAt, AckDate, HandlersCount, Locked) " +
                "  Values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Timestamp now = new Timestamp(new Date().getTime());
        // String newId = UUID.randomUUID().toString().replace("-", "");
        String newId = System6.UuidTo26InsensitiveChars(UUID.randomUUID());
        Object[] parameters = new Object[] {
                newId,
                optionalKey,
                queueName,
                message,
                now,
                now,
                null,
                0,
                false
        };

        if (optionalKey == null)
        {
            QueryRunner qr = new QueryRunner(dataSourceFactory.call());
            qr.update(sql, parameters);
            LocalStat.incPublish(queueName);
            return true;
        }
        else
        {
            DataSource ds = dataSourceFactory.call();
            Connection connection = ds.getConnection();
            connection.setAutoCommit(true);
            getDialect().BeginTransaction(connection);
            // JdbcCommand.execute(connection, getDialect().Begin);
            // connection.setTransactionIsolation(TRANSACTION_ISOLATION);
            QueryRunner qr = new QueryRunner();

            try
            {
                String sqlExists = "Select Id From SimpleQueue Where OptionalKey = ?";
                String id = qr.query(connection, sqlExists, new ScalarHandler<String>(), optionalKey);
                if (id != null)
                {
                    // JdbcCommand.execute(connection, getDialect().Commit);
                    getDialect().CommitTransaction(connection);
                    // connection.commit();
                    return false;
                }
                else
                {
                    // connection = dataSourceFactory.call().getConnection();
                    // qr.update(connection, sql, parameters);
                    JdbcCommand.update(connection, sql, parameters);
                    getDialect().CommitTransaction(connection);
                    // connection.commit();
                    LocalStat.incPublish(queueName);
                    return true;
                }
            }
            catch(Exception ex)
            {
                try {
                    // connection.Rollback();
                    getDialect().RollbackTransaction(connection);
                }
                catch(Exception ex2) {/* shout up */}

                throw new RuntimeException("Publish failed", ex);
            }
            finally {
                connection.close();
            }
        }
    }

    public Message nextDelivery(String queueName) throws Exception {

        DataSource ds = dataSourceFactory.call();
        Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        getDialect().BeginTransaction(connection);
        // JdbcCommand.execute(connection, getDialect().Begin);
        // connection.setTransactionIsolation(TRANSACTION_ISOLATION);
        QueryRunner qr = new QueryRunner();

        try
        {

            SqlDialect dialect = getDialect();
            String sqlLock = (dialect.equals(SqlDialect.SQLSERVER) ? " WITH (UPDLOCK) " : "");
            String sql1 =
                    "SELECT " + String.format(dialect.LimitTop, 1) + " Id FROM SimpleQueue "
                            + sqlLock +
                            "WHERE " +
                            "   (QueueName = ?) " +
                            "   AND (Locked = 0) " +
                            "   AND ((DeliveryDate is null) or (DeliveryDate >= ?)) " +
                            "   AND (AckDate is null) " +
                            "   ORDER BY ModifiedAt " +
                            String.format(dialect.LimitBottom, 1);
                            // + (dialect.equals(SqlDialect.DERBY) ? " WITH RR" : "");


            Timestamp tsNow = new Timestamp(new Date().getTime());
            // mssql
            String idOut = JdbcCommand.selectFirstString(connection, sql1, queueName, tsNow);

            if (idOut == null)
            {
                getDialect().CommitTransaction(connection);
                return null;
            }

            String sql2 = "Update SimpleQueue Set Locked = 1, HandlersCount = HandlersCount + 1 Where Id = ?";
            qr.update(connection, sql2, idOut);

            // Test on transaction
            // if (true) throw new Exception();


            Message ret;
            if (SqlDialect.DERBY.equals(dialect))
            {
                // fucked derby
                // На дерби не работает fetch Message: Stream or LOB value cannot be retrieved more than once.
                String sql4 = "Select " +
                        "Id, OptionalKey, QueueName, CreatedAt, ModifiedAt, AckDate, HandlersCount, Locked " +
                        "From SimpleQueue Where Id = ? ";

                ret = qr.query(connection,
                        sql4,
                        new BeanHandler<Message>(Message.class),
                        idOut);

                // Message отдельно
                ret.setMessage(JdbcCommand.selectSingleBlob(
                        connection,
                        "Select Message From SimpleQueue Where Id = ?",
                        idOut));
            }
            else
            {
                String sql3 = "Select * From SimpleQueue Where Id = ? ";
                ret = qr.query(connection, sql3, new BeanHandler<Message>(Message.class), idOut);
            }

            getDialect().CommitTransaction(connection);
            LocalStat.incInProcess(queueName, 1);
            return ret;
        }
        catch(Exception ex)
        {

            try { getDialect().RollbackTransaction(connection); }
            catch(Exception ex2) {/* shout up */}

            throw new RuntimeException("Next delivery failed", ex);
        }
        finally {
            DbUtils.close(connection);
        }
    }

    // Useless
    public void nextDelivery(long timeout)
    {
    }

    public void ack(String queueName, String idMessage) throws Exception {

        DataSource ds = dataSourceFactory.call();
        Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        getDialect().BeginTransaction(connection);

        try
        {

            String sql = "Update SimpleQueue Set AckDate = ?, Locked = 0 Where Id = ?";
            Object[] parameters = new Object[] { new Timestamp(new Date().getTime()), idMessage };

            JdbcCommand.update(connection, sql, parameters);

            LocalStat.incAck(queueName);
            LocalStat.incInProcess(queueName, -1);
            getDialect().CommitTransaction(connection);
        }
        catch(Exception ex)
        {

            try { getDialect().RollbackTransaction(connection); }
            catch(Exception ex2) {/* shout up */}

            throw new RuntimeException("Ack failed", ex);
        }
        finally {
            DbUtils.close(connection);
        }
    }

    // useless
    public void reject(String idMessage)
    {
    }

    public void postpone(String queueName, String idMessage, Date deliveryAt) throws Exception {
        if (deliveryAt == null)
            deliveryAt = new Date(1);

        DataSource ds = dataSourceFactory.call();
        Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        getDialect().BeginTransaction(connection);

        try
        {
            Timestamp tsDelivery = deliveryAt == null ? null : new Timestamp(deliveryAt.getTime());
            Timestamp tsNow = deliveryAt == null ? null : new Timestamp(new Date().getTime());

            String sql = "Update SimpleQueue Set DeliveryDate=?, Locked = 0, ModifiedAt = ? Where Id = ?";
            Object[] parameters = new Object[] { tsDelivery, tsNow, idMessage};

            JdbcCommand.update(connection, sql, parameters);

            LocalStat.incInProcess(queueName, -1);
            LocalStat.incPostpone(queueName);
            getDialect().CommitTransaction(connection);
        }
        catch(Exception ex)
        {

            try { getDialect().RollbackTransaction(connection); }
            catch(Exception ex2) {/* shout up */}

            throw new RuntimeException("Postpone failed", ex);
        }
        finally {
            DbUtils.close(connection);
        }
    }

    public Message.Status getMessageStatus(String key) throws Exception {
        DataSource ds = dataSourceFactory.call();
        Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        getDialect().BeginTransaction(connection);

        try
        {
            String sql = "Select Id, OptionalKey, CreatedAt, ModifiedAt, AckDate, HandlersCount, Locked From SimpleQueue Where OptionalKey = ?";
            Object[] parameters = new Object[] { key };

            QueryRunner qr = new QueryRunner();
            Message.Status ret = qr.query(connection, sql, new BeanHandler<Message.Status>(Message.Status.class), parameters);
            getDialect().CommitTransaction(connection);
            return ret;
        }
        catch(Exception ex)
        {

            try { getDialect().RollbackTransaction(connection); }
            catch(Exception ex2) {/* shout up */}

            throw new RuntimeException("GetMessageStatus failed", ex);
        }
        finally {
            DbUtils.close(connection);
        }
    }

    public void dropMessageFromStorage(String idMessage) throws Exception {
        String sql = "Delete From SimpleQueue Where Id = ?";

        Connection connection = null;
        try
        {
            connection = dataSourceFactory.call().getConnection();
            JdbcCommand.update(dataSourceFactory.call().getConnection(), sql, idMessage);
        }
        finally
        {
            if (connection != null) connection.close();
        }
    }

    public void deleteQueue(String queueName) throws Exception {
        String sql = "Delete From SimpleQueue Where QueueName = ?";

        Connection connection = null;
        try
        {
            connection = dataSourceFactory.call().getConnection();
            JdbcCommand.update(dataSourceFactory.call().getConnection(), sql, queueName);
        }
        finally
        {
            if (connection != null) connection.close();
        }

    }

    public void purgeOldMessages(String queueName, long secondsOfGap) throws Exception {
        String sql = "Delete From SimpleQueue Where (QueueName = ?) And (CreatedAt < ?)";
        Timestamp edge = new Timestamp(new Date().getTime() - secondsOfGap*1000L);
        Connection connection = null;
        try
        {
            connection = dataSourceFactory.call().getConnection();
            JdbcCommand.update(connection, sql, queueName, edge);
        }
        finally
        {
            if (connection != null) connection.close();
        }
    }

    public void purgeOldMessages(long secondsOfGap) throws Exception {
        String sql = "Delete From SimpleQueue Where CreatedAt < ?";
        Timestamp edge = new Timestamp(new Date().getTime() - secondsOfGap*1000L);
        JdbcCommand.update(dataSourceFactory.call().getConnection(), sql, edge);
    }

    public SqlDialect getDialect() throws Exception {
        if (_dialect == null)
            synchronized (SyncDialect) {
                if (_dialect == null)
                {
                    _dialect = SqlDialect.MYSQL;
                    Connection connection = dataSourceFactory.call().getConnection();
                    try
                    {
                        String url = connection.getMetaData().getURL();
                        _dialect = SqlDialect.getByUrl(url);
                    }
                    finally
                    {
                        connection.close();
                    }
                }
            }

        return _dialect;
    }




}
