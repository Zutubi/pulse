package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.util.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * A task to remove the data map for triggers.  In most cases the information
 * is already duplicated in the config, except for simple triggers used by the
 * Pulse backend.  In this latter case the data is moved into proper columns.
 */
public class RemoveTriggerDataMapRefactorUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    private static final String TABLE_TRIGGER = "LOCAL_TRIGGER";
    private static final String COLUMN_DATA = "DATA";
    private static final String PARAM_INTERVAL = "interval";
    private static final String PARAM_REPEAT = "repeat";
    private static final String PARAM_START = "start";

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws Exception
    {
        refactor.patch("com/zutubi/pulse/master/upgrade/tasks/schema/Schema-2.1.5-patch-02.hbm.xml");
        moveSimpleTriggerData(con);
        refactor.dropColumn(TABLE_TRIGGER, COLUMN_DATA);
    }

    private void moveSimpleTriggerData(Connection con) throws Exception
    {
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet rs = null;
        try
        {
            selectStatement = con.prepareStatement("select ID, DATA from LOCAL_TRIGGER where TRIGGER_TYPE = 'SIMPLE'");
            updateStatement = con.prepareStatement("update LOCAL_TRIGGER set INTERVAL = ?, REPEAT_COUNT = ?, START_TIME = ? where ID = ?");

            rs = selectStatement.executeQuery();
            while (rs.next())
            {
                setParameters(rs, updateStatement);
                updateStatement.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(selectStatement);
            JDBCUtils.close(updateStatement);
        }
    }

    private void setParameters(ResultSet rs, PreparedStatement updateStatement) throws SQLException, IOException, ClassNotFoundException
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new ByteArrayInputStream(rs.getBytes(COLUMN_DATA)));
            Map<Serializable, Serializable> dataMap = (Map<Serializable, Serializable>)ois.readObject();

            long interval = (Long) dataMap.get(PARAM_INTERVAL);
            int repeat = (Integer) dataMap.get(PARAM_REPEAT);
            Date start = (Date) dataMap.get(PARAM_START);

            updateStatement.setLong(1, interval);
            updateStatement.setInt(2, repeat);
            updateStatement.setDate(3, new java.sql.Date(start.getTime()));
            updateStatement.setLong(4, rs.getLong("ID"));
        }
        finally
        {
            IOUtils.close(ois);
        }
    }
}