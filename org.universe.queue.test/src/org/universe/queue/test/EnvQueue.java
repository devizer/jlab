package org.universe.queue.test;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.universe.queue.SimpleQueue;
import org.universe.queue.SimpleQueueDataSourceFactory;
import org.universe.queue.SimpleQueueUtils;
import org.universe.sql.ConnectionMetaDataReader;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.Callable;

public class EnvQueue {

    private static String derbyMemId = "jdbc:derby:memory:" + UUID.randomUUID();

    public static Callable<DataSource> createDataSource() throws Exception {
        return derbyDisk();
    }

    static SimpleQueueDataSourceFactory derbyMemory() throws Exception {
        return  CheckSchema("derby",
                new SimpleQueueDataSourceFactory(
                        "org.apache.derby.jdbc.EmbeddedDriver",
                        derbyMemId + ";create=true"
                ));
    }

    public static SimpleQueueDataSourceFactory derbyDisk() throws Exception {
        return CheckSchema("derby",
                new SimpleQueueDataSourceFactory(
                "org.apache.derby.jdbc.EmbeddedDriver",
                "jdbc:derby:" + getFile("derby") + ";create=true"
        ));
    }

    static SimpleQueueDataSourceFactory sqliteDisk() throws Exception {
        return  CheckSchema("sqlite",
                new SimpleQueueDataSourceFactory(
                        "org.sqlite.JDBC",
                        "jdbc:sqlite:" + getFile("sqlite")
                ));
    }

    static String getFile(String type)
    {
        File file = new File(System.getProperty("user.home"), ".tmp/queue-" + type);
        file.getParentFile().mkdirs();
        if (file.exists()) file.delete();
        return file.getAbsolutePath();
    }

    static SimpleQueueDataSourceFactory sqliteMem() throws Exception {
        return CheckSchema("sqlite",
                new SimpleQueueDataSourceFactory(
                        "org.sqlite.JDBC",
                        "jdbc:sqlite::memory:"
                ));
    }

    static SimpleQueueDataSourceFactory mysql() throws Exception {
        return  CheckSchema("mysql",
                new SimpleQueueDataSourceFactory(
                        "com.mysql.jdbc.Driver",
                        "jdbc:mysql://127.0.0.1/sandbox",
                        "sandbox",
                        "sandbox"
                ));
    }

    static SimpleQueueDataSourceFactory CheckSchema(String dialect, SimpleQueueDataSourceFactory f) throws Exception {
        Connection connection = f.call().getConnection();
        SimpleQueueUtils.SimpleCheckSchema(connection, dialect);

        Connection connection2 = f.call().getConnection();
        connection.close();
        {
            ConnectionMetaDataReader rdr = ConnectionMetaDataReader.Build(connection2);
            System.out.println(rdr);
            // System.out.println(rdr.getInternalLog());
        }
/*
        {
            ConnectionMetaDataReader rdr = ConnectionMetaDataReader.Build(connection);
            System.out.println(rdr);
        }
*/
        return f;
    }

    static SimpleQueueDataSourceFactory msSql() throws Exception {
        return  CheckSchema("mssql",
                new SimpleQueueDataSourceFactory(
                        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                        "jdbc:sqlserver://192.168.1.36\\SQLEXPRESS;databaseName=sandbox;integratedSecurity=false;",
                        "sandbox",
                        "sandbox"
                ));
    }

}
