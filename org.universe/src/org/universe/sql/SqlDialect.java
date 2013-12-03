package org.universe.sql;

import java.sql.Connection;
import java.sql.SQLException;

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
        @Override
        public void BeginTransaction(Connection con) throws SQLException {
            con.setAutoCommit(false);
        }

        @Override
        public void RollbackTransaction(Connection con) throws SQLException {
            con.rollback();
        }

        @Override
        public void CommitTransaction(Connection con) throws SQLException {
            con.commit();
        }
    };

    public static final SqlDialect SQLSERVER = new SqlDialect(
            "sqlserver",
            "TOP %s",
            "",
            "Begin Transaction;",
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
    );

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
