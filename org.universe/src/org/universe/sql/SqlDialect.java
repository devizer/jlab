package org.universe.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 12/3/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqlDialect {

    public final String Id;
    public final String LimitTop;
    public final String LimitBottom;
    private final String Begin;
    private final String Commit;
    private final String Rollback;

    public void BeginTransaction(Connection con) throws SQLException {
        JdbcCommand.execute(con, Begin);
    }

    public void RollbackTransaction(Connection con) throws SQLException {
        JdbcCommand.execute(con, Rollback);
    }

    public void CommitTransaction(Connection con) throws SQLException {
        JdbcCommand.execute(con, Commit);
    }

    public SqlDialect(String id, String limitTop, String limitBottom, String begin, String commit, String rollback) {
        Id = id;
        LimitTop = limitTop;
        LimitBottom = limitBottom;
        Begin = begin;
        Commit = commit;
        Rollback = rollback;
    }

    public static final SqlDialect MYSQL = new SqlDialect(
            "mysql",
            "",
            "Limit %s",
            "Begin;",
            "Commit;",
            "Rollback;"
    );

    public static final SqlDialect DERBY = new SqlDialect(
            "derby",
            "",
            "FETCH FIRST %s ROWS ONLY",
            "/* Begin */",
            "Commit",
            "Rollback"
    )
    {
        ReentrantLock lock = new ReentrantLock();

        @Override
        public void BeginTransaction(Connection con) throws SQLException {
            lock.lock();
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }

        @Override
        public void RollbackTransaction(Connection con) throws SQLException {
            con.rollback();
            lock.unlock();
        }

        @Override
        public void CommitTransaction(Connection con) throws SQLException {
            con.commit();
            lock.unlock();
        }
    };

    public static final SqlDialect SQLSERVER = new SqlDialect(
            "sqlserver",
            "TOP %s",
            "",
            "Begin Transaction; SET TRANSACTION ISOLATION LEVEL SERIALIZABLE; ",
            "Commit Transaction;",
            "Rollback Transaction;"
    );

    public static final SqlDialect SQLITE = new SqlDialect(
            "sqlite",
            "",
            "LIMIT %s",
            "Begin;",
            "Commit;",
            "Rollback;"
    )
    {
        ReentrantLock lock = new ReentrantLock();

        @Override
        public void BeginTransaction(Connection con) throws SQLException {
            lock.lock();
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        }

        @Override
        public void RollbackTransaction(Connection con) throws SQLException {
            con.rollback();
            lock.unlock();
        }

        @Override
        public void CommitTransaction(Connection con) throws SQLException {
            con.commit();
            lock.unlock();
        }

    };

    public static SqlDialect getByUrl(String url)
    {
        if (url != null)
        {
            url = url.toLowerCase();
            if (url.startsWith("jdbc:derby:"))
                return DERBY;
            else if (url.startsWith("jdbc:mysql:"))
                return MYSQL;
            else if (url.startsWith("jdbc:sqlite:"))
                return SQLITE;
            else if (url.startsWith("jdbc:sqlserver:"))
                return SQLSERVER;
        }

        return null;
    }

    public static SqlDialect getById(String id)
    {
        List<SqlDialect> x = Arrays.asList(MYSQL, DERBY, SQLITE, SQLSERVER);
        for(SqlDialect i : x)
            if (i.Id.equalsIgnoreCase(id))
                return i;

        return null;
    }

    public static boolean equals(SqlDialect one, SqlDialect two)
    {
        if ((one == null && two == null) || one == two)
            return true;

        if (one != null && two != null)
            return String.CASE_INSENSITIVE_ORDER.compare(one.Id, two.Id) == 0;

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SqlDialect)
            return equals(this, (SqlDialect) obj);
        else
            return false;
    }

    @Override
    public String toString() {
        return Id;
    }
}
