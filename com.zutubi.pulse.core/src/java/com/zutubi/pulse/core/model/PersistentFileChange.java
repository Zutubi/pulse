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

package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;

/**
 * An entity wrapped around the {@link com.zutubi.pulse.core.scm.api.FileChange}
 * data type from the SCM API.
 */
public class PersistentFileChange extends Entity
{
    private String filename;
    private String revisionString;
    private String actionName;
    private boolean directory;

    protected PersistentFileChange()
    {

    }

    public PersistentFileChange(FileChange data)
    {
        this(data.getPath(), data.getRevision().getRevisionString(), data.getAction(), data.isDirectory());
    }

    public PersistentFileChange(String filename, String revisionString, FileChange.Action action, boolean directory)
    {
        this.filename = filename;
        this.actionName = action.name();
        this.revisionString = revisionString;
        this.directory = directory;
    }

    public FileChange asChange()
    {
        return new FileChange(filename, new Revision(revisionString), FileChange.Action.valueOf(actionName), directory);
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public FileChange.Action getAction()
    {
        return FileChange.Action.valueOf(actionName);
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void setDirectory(boolean directory)
    {
        this.directory = directory;
    }
}
