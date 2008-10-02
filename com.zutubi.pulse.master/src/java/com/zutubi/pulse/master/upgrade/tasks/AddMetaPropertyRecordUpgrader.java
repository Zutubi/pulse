package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;

/**
 * Adds a new meta property to an existing record, with a fixed default value.
 * Automatically figures out if the property is NoInherit and handles it
 * accordingly.
 */
class AddMetaPropertyRecordUpgrader implements RecordUpgrader, PersistentScopesAware
{
    private String name;
    private String value;

    private PersistentScopes persistentScopes;

    /**
     * @param name  the name of the property to add
     * @param value the default value for the property
     */
    public AddMetaPropertyRecordUpgrader(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public void upgrade(String path, MutableRecord record)
    {
        if (record.containsMetaKey(name))
        {
            // Defend against multiple runs as best we can.
            return;
        }

        ScopeDetails scopeDetails = persistentScopes.findByPath(path);
        if (scopeDetails instanceof TemplatedScopeDetails &&
            !CollectionUtils.contains(TemplateRecord.NO_INHERIT_META_KEYS, name))
        {
            // In templated scopes we should only add the property to records
            // that have no parent.  The rest will inherit the value from their
            // root ancestor.  The exception is NoInherit keys, which are never
            // inherited and thus should always be added.
            if (((TemplatedScopeDetails) scopeDetails).hasAncestor(path))
            {
                return;
            }
        }

        record.putMeta(name, value);
    }

    public void setPersistentScopes(PersistentScopes persistentScopes)
    {
        this.persistentScopes = persistentScopes;
    }
}
