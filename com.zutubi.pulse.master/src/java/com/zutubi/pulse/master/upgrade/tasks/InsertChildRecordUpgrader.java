package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.RecordUtils;

/**
 * Inserts a new record as a child of an existing record.  In templated scopes,
 * ensures the record is only added at the top level, and skeletons are added
 * in descendents.
 */
class InsertChildRecordUpgrader implements RecordUpgrader, PersistentScopesAware
{
    private String name;
    private Object value;

    private PersistentScopes persistentScopes;

    /**
     * @param name  the name of the property to add
     * @param value the default value for the property, which must be a simple
     *              value (a string or a string array)
     * @throws IllegalArgumentException if the value given is not simple
     */
    public InsertChildRecordUpgrader(String name, Object value)
    {
        if (!RecordUtils.isSimpleValue(value))
        {
            throw new IllegalArgumentException("Value must be a string or string array - only simple properties can be added with this upgrader.");
        }

        this.name = name;
        this.value = value;
    }

    public void upgrade(String path, MutableRecord record)
    {
        if (record.containsKey(name))
        {
            // Defend against multiple runs as best we can.
            return;
        }

        ScopeDetails scopeDetails = persistentScopes.findByPath(path);
        if (scopeDetails instanceof TemplatedScopeDetails)
        {
            // In templated scopes we should only add the property to records
            // that have no parent.  The rest will inherit the value from their
            // root ancestor.
            if (((TemplatedScopeDetails) scopeDetails).hasAncestor(path))
            {
                return;
            }
        }

        record.put(name, value);
    }

    public void setPersistentScopes(PersistentScopes persistentScopes)
    {
        this.persistentScopes = persistentScopes;
    }
}