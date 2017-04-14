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

import com.zutubi.tove.type.record.RecordManager;

import java.util.Set;

/**
 * A reference cleanup task that deletes a record.  Any tasks needed to
 * cleanup references to the deleted record will cascade off this.
 */
public class DeleteRecordCleanupTask extends RecordCleanupTaskSupport
{
    private boolean internal;

    public DeleteRecordCleanupTask(String path, boolean internal)
    {
        super(path);
        this.internal = internal;
    }

    public boolean run(RecordManager recordManager)
    {
        return recordManager.delete(getAffectedPath()) != null;
    }

    public boolean isInternal()
    {
        return internal;
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.DELETE;
    }

    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
        paths.add(getAffectedPath());
    }
}
