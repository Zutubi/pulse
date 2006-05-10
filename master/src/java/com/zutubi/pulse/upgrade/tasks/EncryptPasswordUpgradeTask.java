/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.util.JDBCUtils;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.acegisecurity.providers.encoding.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class EncryptPasswordUpgradeTask implements UpgradeTask, DataSourceAware
{
    private DataSource dataSource;

    private int buildNumber;

    private List<String> errors = new LinkedList<String>();

    public String getName()
    {
        return "Encrypt user credentials";
    }

    public String getDescription()
    {
        return "This upgrade tasks is for the work done in CIB-375, where user credentials are " +
                "now encrypted in the database.";
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();

            List<IdPassword> results = new LinkedList<IdPassword>();

            // lookup all of the users, retrieving the id and password fields.
            CallableStatement stmt = null;
            ResultSet rs = null;
            try
            {
                stmt = con.prepareCall("SELECT id, password FROM user");
                rs = stmt.executeQuery();
                while (rs.next())
                {
                    results.add(new IdPassword(JDBCUtils.getLong(rs, "id"), JDBCUtils.getString(rs, "password")));
                }
            }
            finally
            {
                JDBCUtils.close(rs);
                JDBCUtils.close(stmt);
            }

            // update the passwords.
            PasswordEncoder encoder = new Md5PasswordEncoder();

            for (IdPassword data : results)
            {
                data.password = encoder.encodePassword(data.password, null);
            }

            // update the database.
            PreparedStatement ps = null;
            try
            {
                ps = con.prepareStatement("UPDATE user SET password = ? WHERE id = ?");
                for (IdPassword data : results)
                {
                    JDBCUtils.setString(ps, 1, data.password);
                    JDBCUtils.setLong(ps, 2, data.id);


                    int rowCount = ps.executeUpdate();
                    if (rowCount != 1)
                    {
                        errors.add("Failed to update password for user " + data.id +
                                ". Row count is " + rowCount + " where 1 was expected.");
                    }
                }
            }
            finally
            {
                JDBCUtils.close(ps);
            }
        }
        catch (SQLException e)
        {
            errors.add("SQLException: " + e.getMessage());
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    /**
     * Required resource.
     *
     * @param source
     */
    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }

    private class IdPassword
    {
        protected long id;
        protected String password;

        IdPassword(long id, String password)
        {
            this.id = id;
            this.password = password;
        }
    }
}
