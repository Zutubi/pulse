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

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds the new serviceName field to jabber configuration.
 */
public class AddJabberServiceNameUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY = "serviceName";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPath("settings/jabber");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        RecordUpgrader upgrader = new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY))
                {
                    return;
                }

                String host = (String) record.get("server");
                String value = "";
                // Preserve existing behaviour: special setting of service name
                // for gtalk.
                if (host != null && host.endsWith("google.com"))
                {
                    value = "gmail.com";
                }

                record.put(PROPERTY, value);
            }
        };

        return asList(upgrader);
    }
}