package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.PropertiesType;
import com.zutubi.pulse.committransformers.StandardCommitMessageTransformer;

import java.sql.*;
import java.io.IOException;
import java.util.Properties;

/**
 * <class comment/>
 */
public class CommitMessageLinkMigrationUpgradeTask extends DatabaseUpgradeTask
{
    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
    {
        // set all TYPE fields to STANDARD.
        updateTypeField(con);

        // create properties object, containing expression and link values, writing it into the properties field.
        createProperties(con);

        // drop two columns, expression, link.
        dropColumn(con, "commit_message_transformer", "expression");
        dropColumn(con, "commit_message_transformer", "replacement");
    }

    private void createProperties(Connection con) throws SQLException
    {
        // loop over result set.
        //    contruct properties object
        //    convert to string
        //    write the data.
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = con.prepareStatement("SELECT * FROM commit_message_transformer");
            rs = ps.executeQuery();

            PreparedStatement update = null;
            try
            {
                update = con.prepareStatement("UPDATE commit_message_transformer SET properties = ? WHERE id = ?");
                
                PropertiesType propType = new PropertiesType();

                while (rs.next())
                {
                    long id = JDBCUtils.getLong(rs, "id");

                    Properties props = new Properties();
                    props.put(StandardCommitMessageTransformer.EXPRESSION_PROPERTY, JDBCUtils.getString(rs, "expression"));
                    props.put(StandardCommitMessageTransformer.LINK_PROPERTY, JDBCUtils.getString(rs, "replacement"));

                    JDBCUtils.setString(update, 1, propType.toString(props));
                    JDBCUtils.setLong(update, 2, id);
                    update.executeUpdate();
                }
            }
            finally
            {
                JDBCUtils.close(update);
            }

        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
        }
    }

    /**
     * Drop the columns that are no longer required.
     *
     * @param con
     */
    private void dropColumn(Connection con, String table, String column) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall(String.format("ALTER TABLE %s DROP COLUMN %s", table, column));
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    /**
     * Update the type field to STANDARD. All existing linkers are of this type.
     *
     * @param con to the database being upgraded.
     */
    private void updateTypeField(Connection con) throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("UPDATE commit_message_transformer SET type = ?");
            JDBCUtils.setString(ps, 1, "STANDARD");
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    public String getName()
    {
        return "Commit message linkers.";
    }

    public String getDescription()
    {
        return "Migrate the commit message links to the new commit mesasge transformer schema.";
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
