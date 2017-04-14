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
import com.zutubi.pulse.master.build.queue.RecipeQueue;
import com.zutubi.pulse.master.build.queue.SchedulingController;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import com.zutubi.util.StringUtils;

/**
 * Toggles the state of the build or recipe queue.
 */
public class ToggleQueueAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(ToggleQueueAction.class);

    private static final String QUEUE_BUILD = "build";
    private static final String QUEUE_STAGE = "stage";
    
    private String queueName;
    private RecipeQueue recipeQueue;
    private SchedulingController schedulingController;
    private SimpleResult result;

    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        if (accessManager.hasPermission(ServerPermission.ADMINISTER.name(), null))
        {
            if (QUEUE_STAGE.equals(queueName))
            {
                if(recipeQueue.isRunning())
                {
                    recipeQueue.stop();
                    result = new SimpleResult(true, I18N.format("queue.paused", StringUtils.capitalise(queueName)));
                }
                else
                {
                    recipeQueue.start();
                    result = new SimpleResult(true, I18N.format("queue.resumed", StringUtils.capitalise(queueName)));
                }
            }
            else if (QUEUE_BUILD.equals(queueName))
            {
                if (schedulingController.isRunning())
                {
                    schedulingController.pause();
                    result = new SimpleResult(true, I18N.format("queue.paused", StringUtils.capitalise(queueName)));
                }
                else
                {
                    schedulingController.resume();
                    result = new SimpleResult(true, I18N.format("queue.resumed", StringUtils.capitalise(queueName)));
                }
            }
            // pause for effect.
            Thread.sleep(500);
        }
        else
        {
            result = new SimpleResult(false, I18N.format("toggle.not.permitted", queueName));
        }
        
        return SUCCESS;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }
}
