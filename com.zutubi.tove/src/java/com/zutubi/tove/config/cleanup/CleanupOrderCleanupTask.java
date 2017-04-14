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

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.List;

/**
 * A reference cleanup task that ensures a declared order on a collection has
 * the deleted key removed - if such an order exists and has the key.
 */
public class CleanupOrderCleanupTask extends RecordCleanupTaskSupport
{
    public CleanupOrderCleanupTask(String path)
    {
        super(path);
    }

    public boolean run(RecordManager recordManager)
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());
        Record parentRecord = recordManager.select(parentPath);

        if (parentRecord != null)
        {
            List<String> order = CollectionType.getDeclaredOrder(parentRecord);
            if (order.remove(baseName))
            {
                MutableRecord mutableParent = parentRecord.copy(false, true);
                CollectionType.setOrder(mutableParent, order);
                recordManager.update(parentPath, mutableParent);
                return true;
            }
        }
        
        return false;
    }

    public boolean isInternal()
    {
        return true;
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.NONE;
    }
}
