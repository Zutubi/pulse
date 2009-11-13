package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.RandomUtils;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;

import java.util.Map;

public class FixNPEOnLDAPRememberMeUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute() throws TaskException
    {
        Record ldapSettings = recordManager.select("settings/ldap");
        if (!"zutubi.ldapConfig".equals(ldapSettings.getSymbolicName()))
        {
            // this is unexpected - we must have the path wrong.
            throw new RuntimeException(ldapSettings.getSymbolicName());
        }

        if (!ldapSettings.containsKey("enabled"))
        {
            return;
        }

        Boolean enabled = Boolean.valueOf((String) ldapSettings.get("enabled"));
        if (enabled)
        {
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();

            // work through the existing users and ensure they have passwords.  Assign a random password if no is available.
            Map<String, Record> users = recordManager.selectAll("users/*");
            for (Map.Entry<String, Record> entry : users.entrySet())
            {
                Record user = entry.getValue();
                if (user.get("password") == null)
                {
                    String path = entry.getKey();

                    String randomPassword = RandomUtils.randomToken(10);
                    
                    String encodedPassword = encoder.encodePassword(randomPassword, null);

                    MutableRecord updatedUser = user.copy(true, true);
                    updatedUser.put("password", encodedPassword);
                    
                    recordManager.update(path, updatedUser);
                }
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
