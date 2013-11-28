package org.universe.queue;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.universe.System6;
import org.universe.sql.JdbcCommand;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

// If connection thread safe, then SimpleQueue is thread safe.
public class SimpleQueue {

    public static final QueueStatistic LocalStat = new QueueStatistic();
    static final int TRANSACTION_ISOLATION = Connection.TRANSACTION_SERIALIZABLE;
    private Callable<DataSource> dataSourceFactory;
    Dialect _dialect;
    Object SyncDialect = new Object();

    static enum Dialect
    {
        mysql,
        derby,
        sqlite,
        mssql,
    }


    Dialect getDialect() throws Exception {
        if (_dialect == null)
            synchronized (SyncDialect) {
                if (_dialect == null)
                {
                    _dialect = Dialect.mysql;
                    Connection connection = dataSourceFactory.call().getConnection();
                    String url = connection.getMetaData().getURL();
                    if (url != null)
                    {
                        url = url.toLowerCase();
                        if (url.startsWith("jdbc:derby:"))
                            _dialect = Dialect.derby;
                        else if (url.startsWith("jdbc:mysql:"))
                            _dialect = Dialect.mysql;
                        else if (url.startsWith("jdbc:sqlite:"))
                            _dialect = Dialect.sqlite;
                        else if (url.startsWith("jdbc:sqlserver:"))
                            _dialect = Dialect.mssql;
                    }
                    connection.close();
                    // System.out.println("Dialect: " + _dialect);
                }
            }

        return _dialect;
    }


    public SimpleQueue(Callable<DataSource> dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
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
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(TRANSACTION_ISOLATION);
            QueryRunner qr = new QueryRunner();

            try
            {
                String sqlExists = "Select Id From SimpleQueue Where OptionalKey = ?";
                String id = qr.query(connection, sqlExists, new ScalarHandler<String>(), optionalKey);
                if (id != null)
                {
                    connection.commit();
                    return false;
                }
                else
                {
                    // connection = dataSourceFactory.call().getConnection();
                    qr.update(connection, sql, parameters);
                    connection.commit();
                    LocalStat.incPublish(queueName);
                    return true;
                }
            }
            catch(Exception ex)
            {
                try { connection.rollback(); }
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
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(TRANSACTION_ISOLATION);
        QueryRunner qr = new QueryRunner();

        // qr.update("Begin");

        try
        {

            Dialect dialect = getDialect();
            String limit1 = dialect == Dialect.mssql ? "TOP 1" : "";
            String limit2 = dialect == Dialect.derby ? "FETCH FIRST 1 ROWS ONLY" : dialect == Dialect.mssql ? "" : "LIMIT 1";
            String sql1 =
                    "SELECT " + limit1 + " Id FROM SimpleQueue " +
                            "WHERE " +
                            "   (QueueName = ?) " +
                            "   AND (Locked = 0) " +
                            "   AND ((DeliveryDate is null) or (DeliveryDate >= ?)) " +
                            "   AND (AckDate is null) " +
                            "   ORDER BY ModifiedAt " +
                            limit2;

            Timestamp tsNow = new Timestamp(new Date().getTime());
            // mssql
            String idOut = JdbcCommand.selectFirstString(connection, sql1, queueName, tsNow);

            if (idOut == null)
                return null;

            String sql2 = "Update SimpleQueue Set Locked = 1, HandlersCount = HandlersCount + 1 Where Id = ?";
            qr.update(connection, sql2, idOut);

            // Test on transaction
            // if (true) throw new Exception();


            Message ret;
            if (dialect == Dialect.derby)
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

            connection.commit();
            LocalStat.incInProcess(queueName, 1);
            return ret;
        }
        catch(Exception ex)
        {

            try { connection.rollback(); }
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
        String sql = "Update SimpleQueue Set AckDate = ?, Locked = 0 Where Id = ?";
        Object[] parameters = new Object[] { new Timestamp(new Date().getTime()), idMessage };

        QueryRunner qr = new QueryRunner(dataSourceFactory.call());
        qr.update(sql, parameters);

        LocalStat.incAck(queueName);
        LocalStat.incInProcess(queueName, -1);
    }

    // useless
    public void reject(String idMessage)
    {
    }

    public void postpone(String queueName, String idMessage, Date deliveryAt) throws Exception {
        if (deliveryAt == null)
            deliveryAt = new Date(1);

        Timestamp tsDelivery = deliveryAt == null ? null : new Timestamp(deliveryAt.getTime());
        Timestamp tsNow = deliveryAt == null ? null : new Timestamp(new Date().getTime());

        String sql = "Update SimpleQueue Set DeliveryDate=?, Locked = 0, ModifiedAt = ? Where Id = ?";
        Object[] parameters = new Object[] { tsDelivery, tsNow, idMessage};

        QueryRunner qr = new QueryRunner(dataSourceFactory.call());
        qr.update(sql, parameters);

        LocalStat.incInProcess(queueName, -1);
        LocalStat.incPostpone(queueName);
    }

    public Message.Status getMessageStatus(String key) throws Exception {
        String sql = "Select Id, OptionalKey, CreatedAt, ModifiedAt, AckDate, HandlersCount, Locked From SimpleQueue Where OptionalKey = ?";
        Object[] parameters = new Object[] { key };

        QueryRunner qr = new QueryRunner(dataSourceFactory.call());
        Message.Status ret = qr.query(sql, new BeanHandler<Message.Status>(Message.Status.class), parameters);
        return ret;
    }

    public void dropMessageFromStorage(String idMessage) throws Exception {
        String sql = "Delete From SimpleQueue Where Id = ?";
        JdbcCommand.update(dataSourceFactory.call().getConnection(), sql, idMessage);
        // QueryRunner qr = new QueryRunner(dataSourceFactory.call());
        // qr.update(sql, idMessage);
    }

    public void purgeOldMessages(String queueName, long secondsOfGap) throws Exception {
        String sql = "Delete From SimpleQueue Where (QueueName = ?) And (CreatedAt < ?)";
        Timestamp edge = new Timestamp(new Date().getTime() - secondsOfGap*1000L);
        JdbcCommand.update(dataSourceFactory.call().getConnection(), sql, queueName, edge);
        // QueryRunner qr = new QueryRunner(dataSourceFactory.call());
        // qr.update(sql, queueName, edge);
    }

    public void purgeOldMessages(long secondsOfGap) throws Exception {
        String sql = "Delete From SimpleQueue Where CreatedAt < ?";
        Timestamp edge = new Timestamp(new Date().getTime() - secondsOfGap*1000L);
        JdbcCommand.update(dataSourceFactory.call().getConnection(), sql, edge);
    }


}
