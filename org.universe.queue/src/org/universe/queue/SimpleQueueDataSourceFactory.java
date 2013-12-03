package org.universe.queue;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.Callable;

public class SimpleQueueDataSourceFactory implements Callable<DataSource> {

    public final String driverClass;
    public final String url;
    String user;
    String password = null;
    private PoolableConnectionFactory factory;
    private PoolingDataSource poolingDataSource;
    private BasicDataSource bds;


    public SimpleQueueDataSourceFactory(String url) {
        this.url = url;
        driverClass = null;
        user = null;
    }

    public SimpleQueueDataSourceFactory(String driverClass, String url) {
        this.driverClass = driverClass;
        this.url = url;
        user = null;
    }

    public SimpleQueueDataSourceFactory(String driverClass, String url, String user, String password) {
        this.driverClass = driverClass;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    synchronized private void init() throws ClassNotFoundException {
        if (factory == null)
        {
            Properties props = System.getProperties();

            if (user != null)
                props.put("user", user);

            if (password != null)
                props.put("password", password);

            if (driverClass != null && driverClass.trim().length() > 0)
                Class.forName(driverClass).toString();

            GenericObjectPool pool = new GenericObjectPool(null);
            // pool.setMaxActive(1);


            factory = new PoolableConnectionFactory(
                    new DriverManagerConnectionFactory(url, props),
                    pool,
                    null, null, false, true);

            poolingDataSource = new PoolingDataSource(factory.getPool());

            bds = new BasicDataSource();
            bds.setUrl(url);
            if (user != null) bds.setUsername(user);
            if (password != null) bds.setPassword(password);

        }
    }

    @Override
    public DataSource call() throws Exception {
        if (factory == null)
            init();

        // return poolingDataSource;
        return poolingDataSource;
    }

}
