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

/**
 * Indicates a template child record has a simple value where its template parent has a record.
 */
public class SimpleOverrideOfComplexProblem extends MismatchedTemplateStructureProblem
{
    public SimpleOverrideOfComplexProblem(String path, String message, String key, String templateParentPath)
    {
        super(path, message, key, templateParentPath);
    }

    @Override
    public void solve(RecordManager recordManager)
    {
        if (parentStillHasRecord(recordManager))
        {
            Record record = recordManager.select(getPath());
            if (record.containsKey(key))
            {
                MutableRecord mutableRecord = record.copy(false, true);
                mutableRecord.remove(key);
                recordManager.update(getPath(), mutableRecord);
            }
        }

        // Now let the super implementation fill in skeletons.
        super.solve(recordManager);
    }
}
