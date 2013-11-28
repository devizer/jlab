package org.universe.sql;

import java.sql.*;

public class JdbcCommand {

    // На MS SQL Server клиенте криво метаданные читаются.
    public static int update(Connection con, String sql, Object... parameters) throws SQLException {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement(sql);
            fillParameters(stmt, parameters);
            int ret = stmt.executeUpdate();
            return ret;
        }
        finally {
            if (stmt != null) stmt.close();
        }
    }

    // Derby fetch Message using BeanHandler: Stream or LOB value cannot be retrieved more than once.
    public static byte[] selectSingleBlob(Connection con, String sql, Object... parameters) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(sql);
            fillParameters(stmt, parameters);
            rs = stmt.executeQuery();
            if (rs.next())
            {
                Blob blob = rs.getBlob(1);
                long blobLength = blob.length();
                byte[] copy = blob.getBytes(1, (int) blobLength);
                return copy;
            }
        }
        finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }

        return null;
    }

    // MS SQL криво meta-данные читаются.
    public static Object selectFirstValue(Connection con, String sql, Object... parameters) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareStatement(sql);
            fillParameters(stmt, parameters);

            rs = stmt.executeQuery();
            if (rs.next())
            {
                return rs.getObject(1);
            }
        }
        finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }

        return null;
    }

    private static void fillParameters(PreparedStatement stmt, Object[] parameters) throws SQLException {
        if (parameters == null || parameters.length == 0)
            return;

        for(int i=0; i<parameters.length; i++)
            if (parameters[i] == null)
                stmt.setNull(i+1, Types.VARCHAR);
            else
                stmt.setObject(i+1, parameters[i]);
    }
}
