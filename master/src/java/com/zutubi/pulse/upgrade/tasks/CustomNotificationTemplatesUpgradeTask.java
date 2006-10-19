package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class CustomNotificationTemplatesUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Custom notification templates";
    }

    public String getDescription()
    {
        return "Upgrades subscriptions to allow a choice of notification templates";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException, IOException
    {
        // Currently:
        //   - Both personal and project build subscriptions have all fields,
        //     with a boolean property 'personal' distinguishing them
        //   - email contact points have a "type" property that indicates the
        //     template used to render them (plain or html)
        //
        // Now:
        //   - Personal and Project build subscriptions are split into two
        //     classes: there is a discriminator column "type" that replaces
        //     the "personal" boolean with either "personal" or "project"
        //   - All subscriptions have an additional "template" property which
        //     holds the name of the template used to render them.  This should
        //     be set to "simple-instant-message" for Jabber contact points and
        //     either "html-email" or "plain-text-email" for email contact
        //     points, depending on the old "type" property on the contact.
        //   - The "type" property should be dropped from email contact points.
        updateSubscriptions(con);
        dropColumn(con);
    }

    private void updateSubscriptions(Connection con) throws SQLException, IOException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, personal, contact_id FROM subscription");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                Long id = JDBCUtils.getLong(rs, "id");
                Boolean personal = JDBCUtils.getBool(rs, "personal");
                Long contactId = JDBCUtils.getLong(rs, "contact_id");

                String template = getTemplate(con, contactId);
                updateSubscription(con, id, personal, template);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private String getTemplate(Connection con, Long contactId) throws SQLException, IOException
    {
        String result = "html-email";
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT type, properties FROM contact_point WHERE id = ?");
            stmt.setLong(1, contactId);
            rs = stmt.executeQuery();
            if(rs.next())
            {
                String type = JDBCUtils.getString(rs, "type");
                if(type.equals("EMAIL"))
                {
                    Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(rs.getBytes("properties")));
                    String emailType = properties.getProperty("type");
                    if(emailType.equals("html"))
                    {
                        result = "html-email";
                    }
                    else
                    {
                        result = "plain-text-email";
                    }

                    updateProperties(con, contactId, properties);
                }
                else
                {
                    result = "simple-instant-message";
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        return result;
    }

    private void updateSubscription(Connection con, Long id, Boolean personal, String template) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("UPDATE subscription SET type = ?, template = ? WHERE id = ?");
            stmt.setString(1, personal ? "PERSONAL" : "PROJECT");
            stmt.setString(2, template);
            stmt.setLong(3, id);

            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void updateProperties(Connection con, Long contactId, Properties properties) throws IOException, SQLException
    {
        properties.remove("type");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        properties.store(baos, "");
        byte [] data = baos.toByteArray();

        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("UPDATE contact_point SET properties = ? WHERE id = ?");
            stmt.setBytes(1, data);
            stmt.setLong(2, contactId);

            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void dropColumn(Connection con) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("ALTER TABLE subscription DROP COLUMN personal");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
