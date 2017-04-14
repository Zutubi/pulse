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

import com.zutubi.pulse.core.upgrade.PulseFileToToveFile;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;

/**
 * A task to update custom pulse files to the tove-file-enabled 2.1 form.
 */
public class ToveFileUpgradeTask extends AbstractUpgradeTask
{
    private static final String PATH_PATTERN_TYPE = "projects/*/type";
    private static final String TYPE_CUSTOM = "zutubi.customTypeConfig";
    private static final String PROPERTY_PULSE_FILE_STRING = "pulseFileString";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        RecordLocator recordLocator = RecordLocators.newTypeFilter(RecordLocators.newPathPattern(PATH_PATTERN_TYPE), TYPE_CUSTOM);
        Map<String, Record> typeRecords = recordLocator.locate(recordManager);
        for (Map.Entry<String, Record> typeEntry : typeRecords.entrySet())
        {
            updatePulseFile(typeEntry.getKey(), typeEntry.getValue());
        }
    }

    private void updatePulseFile(String path, Record typeRecord) throws TaskException
    {
        String pulseFileString = (String) typeRecord.get(PROPERTY_PULSE_FILE_STRING);
        if (pulseFileString != null)
        {
            MutableRecord mutableTypeRecord = typeRecord.copy(false, true);
            try
            {
                String converted = PulseFileToToveFile.convert(pulseFileString);
                mutableTypeRecord.put(PROPERTY_PULSE_FILE_STRING, converted);
                recordManager.update(path, mutableTypeRecord);
            }
            catch (Exception e)
            {
                throw new TaskException(e);
            }
        }
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}