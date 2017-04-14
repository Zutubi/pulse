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

package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;

/**
 * This problem indicates that hidden keys have been found in a record with no
 * template parent.
 */
public class UnexpectedHiddenKeysProblem extends HealthProblemSupport
{
    /**
     * Creates a new problem indicating unexpected hidden keys at the given
     * path.
     * 
     * @param path    path of the record with the hidden keys
     * @param message description of the problem
     */
    public UnexpectedHiddenKeysProblem(String path, String message)
    {
        super(path, message);
    }

    public void solve(RecordManager recordManager)
    {
        // Just wipe out the hidden keys - they are not sensible when we have
        // no template parent.
        Record record = recordManager.select(getPath());
        if (record != null && record.containsMetaKey(TemplateRecord.HIDDEN_KEY))
        {
            MutableRecord mutableRecord = record.copy(false, true);
            mutableRecord.removeMeta(TemplateRecord.HIDDEN_KEY);
            recordManager.update(getPath(), mutableRecord);
        }
    }
}
