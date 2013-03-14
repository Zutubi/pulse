package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Changes the single groupBaseDn field in the LDAP config to an array called
 * groupBaseDns.
 */
public class LdapGroupDnsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath("settings", "ldap"));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        RecordUpgrader upgrader = new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                String groupDn = (String) record.remove("groupBaseDn");
                String[] groupDns = StringUtils.stringSet(groupDn) ? new String[]{groupDn} : new String[0];
                record.put("groupBaseDns", groupDns);
            }
        };

        return Arrays.asList(upgrader);
    }
}