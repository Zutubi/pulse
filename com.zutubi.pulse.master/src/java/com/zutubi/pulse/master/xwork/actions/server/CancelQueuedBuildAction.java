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

package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.build.queue.FatController;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import org.springframework.security.access.AccessDeniedException;

/**
 * Ajax action to cancel a build in the queue.
 */
public class CancelQueuedBuildAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(CancelQueuedBuildAction.class);
    
    /**
     * The id of the queued build request event to be cancelled.
     */
    private long id;
    private SimpleResult result;

    private FatController fatController;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        try
        {
            if (fatController.cancelQueuedBuild(id))
            {
                result = new SimpleResult(true, I18N.format(id == -1 ? "all.cancelled" : "request.cancelled"));
            }
            else
            {
                result = new SimpleResult(false, I18N.format(id == -1 ? "none.cancelled" : "request.not.found"));
            }
        }
        catch (AccessDeniedException e)
        {
            result = new SimpleResult(false, I18N.format("cancel.not.permitted"));
        }

        return SUCCESS;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }
}
