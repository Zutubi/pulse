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

package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * A record cleanup task that nulls out a reference.  Should only be used
 * when the reference is not required.
 */
public class NullifyReferenceCleanupTask extends RecordCleanupTaskSupport
{
    private boolean hasTemplateParent;

    public NullifyReferenceCleanupTask(String referencingPath, boolean hasTemplateParent)
    {
        super(referencingPath);
        this.hasTemplateParent = hasTemplateParent;
    }

    public boolean run(RecordManager recordManager)
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        Record parentRecord = recordManager.select(parentPath);
        if (parentRecord != null)
        {
            MutableRecord newValue = parentRecord.copy(false, true);
            String key = PathUtils.getBaseName(getAffectedPath());
            if (hasTemplateParent)
            {
                // Remove our override.
                newValue.remove(key);
            }
            else
            {
                newValue.put(key, "0");
            }

            recordManager.update(parentPath, newValue);
            return true;
        }
        
        return false;
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.PARENT_UPDATE;
    }
}
