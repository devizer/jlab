package org.universe.queue;

import org.universe.System6;
import org.universe.sql.SqlCodeReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/24/13
 * Time: 10:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleQueueUtils {

    // TODO: Another compliant universal way
    public static void SimpleCheckSchema(Connection con, String dialect) throws SQLException, IOException {
        List<String> list = SqlCodeReader.SplitSqlResources("/org/universe/queue/SimpleQueue-" + getKnownDialect(dialect) + ".ddl");
        for(String sql : list)
        {
            Statement stmt = con.createStatement();
            try
            {
                stmt.execute(sql);
            }
            catch(SQLException ex)
            {
                // shout up
                // System.err.println(ex + System6.lineSeparator() + "IN" + System6.lineSeparator() + sql);
            }
            finally {
                stmt.close();
            }
        }
    }

    static String getKnownDialect(String arg)
    {
        Comparator<String> c = String.CASE_INSENSITIVE_ORDER;
        if (c.compare("mysql", arg) == 0 || c.compare("sqlite", arg) == 0)
            return "mysql";

        else if (c.compare("derby", arg) == 0)
            return "derby";

        return arg;
    }

}
