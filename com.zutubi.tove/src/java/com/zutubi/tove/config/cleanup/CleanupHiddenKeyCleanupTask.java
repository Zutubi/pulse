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

import com.zutubi.tove.type.record.*;

import java.util.Set;

/**
 * A reference cleanup task that cleans up a hidden key for a record.  Note
 * that the hidden key may not always be present: if the parent itself is
 * hidden, for example.
 */
public class CleanupHiddenKeyCleanupTask extends RecordCleanupTaskSupport
{
    public CleanupHiddenKeyCleanupTask(String path)
    {
        super(path);
    }

    public boolean run(RecordManager recordManager)
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());

        Record parent = recordManager.select(parentPath);
        if(parent != null && TemplateRecord.getHiddenKeys(parent).contains(baseName))
        {
            MutableRecord mutableParent = parent.copy(false, true);
            TemplateRecord.restoreItem(mutableParent, baseName);
            recordManager.update(parentPath, mutableParent);
            return true;
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

    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
    }
}
