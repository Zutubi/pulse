package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.RandomUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;

import org.acegisecurity.providers.encoding.Md5PasswordEncoder;

/**
 * Upgrade task that ensures ldap users have random passwords.
 */
public class FixLDAPRememberMeUpgradeTask extends DatabaseUpgradeTask
{

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
    {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        String blankPassword = encoder.encodePassword(null, null);

        List<Long> usersRequiringUpdates = new LinkedList<Long>();

        // a) locate users with blank passwords.
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = con.prepareCall("SELECT id, password FROM local_user");
            rs = ps.executeQuery();
            while (rs.next())
            {
                Long id = JDBCUtils.getLong(rs, "id");
                String password = JDBCUtils.getString(rs, "password");

                if (blankPassword.equals(password))
                {
                    usersRequiringUpdates.add(id);
                }
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        // b) write the data into the user_props table.
        try
        {
            ps = con.prepareStatement("UPDATE local_user SET password = ? WHERE id = ?");

            for (Long userId : usersRequiringUpdates)
            {
                JDBCUtils.setLong(ps, 2, userId);
                JDBCUtils.setString(ps, 1, RandomUtils.randomString(10));

                ps.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    public String getName()
    {
        return "LDAP User remember me fix.";
    }

    public String getDescription()
    {
        return "Data fix to correct a fault with the LDAP users remember me login functionality. ";
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
