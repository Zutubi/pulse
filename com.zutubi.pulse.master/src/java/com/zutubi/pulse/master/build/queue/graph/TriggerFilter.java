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

package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.util.adt.TreeNode;

/**
 * The trigger filter is used to filter nodes in a downstream graph for
 * which the project at a particular node either does not have a dependent
 * build trigger configured or that trigger is paused.  
 */
public class TriggerFilter extends GraphFilter
{
    private Scheduler scheduler;

    public void run(TreeNode<BuildGraphData> node)
    {
        if (isDownstream(node))
        {
            // get the projects dependency trigger.
            TriggerConfiguration trigger = TriggerUtils.getTrigger(node.getData().getProjectConfig(), DependentBuildTriggerConfiguration.class);

            // Check the trigger.  If configured and active, then we want to traverse to
            // the children, otherwise not.
            if (trigger == null)
            {
                toTrim.add(node);
            }
            else
            {
                if (!scheduler.getTrigger(trigger.getTriggerId()).isActive())
                {
                    toTrim.add(node);
                }
            }
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
