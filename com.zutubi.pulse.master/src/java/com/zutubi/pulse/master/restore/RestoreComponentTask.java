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

package com.zutubi.pulse.master.restore;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.util.monitor.FeedbackAware;
import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the restore task interface that triggers the {@link ArchiveableComponent#restore(java.io.File)} 
 * method for a single component.
 */
public class RestoreComponentTask implements Task, FeedbackAware
{
    private static final Messages I18N = Messages.getInstance(RestoreComponentTask.class);

    private ArchiveableComponent component;
    private File archiveBase;
    private List<String> errorMessages = new LinkedList<String>();
    private boolean failed;

    public RestoreComponentTask(ArchiveableComponent component, File archiveBase)
    {
        this.component = component;
        this.archiveBase = archiveBase;
    }

    public void execute() throws TaskException
    {
        try
        {
            component.restore(archiveBase);
        }
        catch (ArchiveException e)
        {
            failed = true;
            addErrorMessage("Restoration failed. Cause: " + e.getMessage() + ". See logs for more details.");
            throw new TaskException(e);
        }
    }

    public String getName()
    {
        String name = component.getName();
        String key = name + ".label";
        if (I18N.isKeyDefined(key))
        {
            return I18N.format(key);
        }
        else
        {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public ArchiveableComponent getComponent()
    {
        return component;
    }

    public boolean hasFailed()
    {
        return failed;
    }

    private void addErrorMessage(String msg)
    {
        errorMessages.add(msg);
    }

    public List<String> getErrors()
    {
        return errorMessages;
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public String getDescription()
    {
        return component.getDescription();
    }

    public void setFeedback(TaskFeedback feedback)
    {
        if (component instanceof FeedbackAware)
        {
            ((FeedbackAware)component).setFeedback(feedback);
        }
    }
}
