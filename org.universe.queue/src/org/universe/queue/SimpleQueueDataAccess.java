package org.universe.queue;

import java.sql.Connection;

public class SimpleQueueDataAccess {

    Connection con;

    public SimpleQueueDataAccess(Connection con) {
        this.con = con;
    }

}
