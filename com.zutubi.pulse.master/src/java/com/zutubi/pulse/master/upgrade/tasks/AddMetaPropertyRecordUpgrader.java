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
