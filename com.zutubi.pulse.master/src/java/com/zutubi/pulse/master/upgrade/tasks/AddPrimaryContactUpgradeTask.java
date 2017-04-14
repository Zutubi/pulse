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

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * Upgrade task to set a primary contact point for all users that have at least
 * one contact point defined.
 */
public class AddPrimaryContactUpgradeTask extends AbstractUpgradeTask
{
    private static final String PATTERN_ALL_PREFERENCES = "users/*/preferences";

    private static final String PROPERTY_CONTACTS = "contacts";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_PRIMARY = "primary";
    private static final String PROPERTY_PERMANENT = "permanent";

    private static final String TYPE_EMAIL = "zutubi.emailContactConfig";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Map<String, Record> allPreferences = recordManager.selectAll(PATTERN_ALL_PREFERENCES);
        for (Map.Entry<String, Record> entry: allPreferences.entrySet())
        {
            upgradePreferences(entry.getKey(), entry.getValue());
        }
    }

    private void upgradePreferences(String path, Record preferencesRecord)
    {
        Record contactsRecord = (Record) preferencesRecord.get(PROPERTY_CONTACTS);
        String contactsPath = PathUtils.getPath(path, PROPERTY_CONTACTS);
        if (contactsRecord.size() > 0)
        {
            String primaryContactName = markPrimaryContact(contactsPath, contactsRecord);
            markOtherContacts(contactsPath, contactsRecord, primaryContactName);
        }
    }

    private String markPrimaryContact(String contactsPath, Record contactsRecord)
    {
        Record chosenRecord = choosePrimaryContact(contactsRecord);
        String chosenName = (String) chosenRecord.get(PROPERTY_NAME);

        MutableRecord mutableRecord = chosenRecord.copy(false, true);
        mutableRecord.put(PROPERTY_PRIMARY, "true");
        mutableRecord.putMeta(PROPERTY_PERMANENT, "true");
        recordManager.update(PathUtils.getPath(contactsPath, chosenName), mutableRecord);

        return chosenName;
    }

    private Record choosePrimaryContact(Record contactsRecord)
    {
        // Prefer an email contact.
        for (String childKey: contactsRecord.nestedKeySet())
        {
            Record contact = (Record) contactsRecord.get(childKey);
            if (contact.getSymbolicName().equals(TYPE_EMAIL))
            {
                return contact;
            }
        }

        // Just use the first contact.
        return (Record) contactsRecord.get(contactsRecord.nestedKeySet().iterator().next());
    }

    private void markOtherContacts(String contactsPath, Record contactsRecord, String primaryContactName)
    {
        for (String childKey: contactsRecord.nestedKeySet())
        {
            if (!childKey.equals(primaryContactName))
            {
                Record contactRecord = (Record) contactsRecord.get(childKey);
                MutableRecord mutableRecord = contactRecord.copy(false, true);
                mutableRecord.put(PROPERTY_PRIMARY, "false");
                recordManager.update(PathUtils.getPath(contactsPath, childKey), mutableRecord);
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}