package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Predicate;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.List;

/**
 * Removes incorrect permanent flags from users created by jabberwocky <= 1.6
 * exports.
 */
public class FixPermanentUsersUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_USERS = "users";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator allUsersLocator = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_USERS, PathUtils.WILDCARD_ANY_ELEMENT));
        return RecordLocators.newPredicateFilter(allUsersLocator, new Predicate<Record>()
        {
            public boolean apply(Record record)
            {
                return !record.get("userId").equals("1");
            }
        });
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newDeleteMetaProperty(Configuration.PERMANENT_KEY));
    }
}