/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

/**
 * Adds a new property to an existing record, with a fixed default value.  The
 * property value must be simple (a string or string array).
 */
class AddPropertyRecordUpgrader implements RecordUpgrader, PersistentScopesAware
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
    public AddPropertyRecordUpgrader(String name, Object value)
    {
        if (!RecordUpgradeUtils.isSimpleValue(value))
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
