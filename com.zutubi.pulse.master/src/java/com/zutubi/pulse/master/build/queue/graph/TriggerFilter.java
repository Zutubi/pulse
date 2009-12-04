package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scheduling.Scheduler;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.TreeNode;

import java.util.Map;

/**
 * The trigger filter is used to filter nodes in a downstream graph for
 * which the project at a particular node either does not have a dependent
 * build trigger configured or that trigger is paused.  
 */
public class TriggerFilter extends GraphFilter
{
    private Scheduler scheduler;

    public void process(TreeNode<GraphData> node)
    {
        if (isDownstream(node))
        {
            // get the projects dependency trigger.
            TriggerConfiguration trigger = getTrigger(node.getData().getProject());

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

    private DependentBuildTriggerConfiguration getTrigger(Project project)
    {
        // the dependent trigger configuration customises the dependency request..
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) project.getConfig().getExtensions().get(EXTENSION_PROJECT_TRIGGERS);

        return (DependentBuildTriggerConfiguration) CollectionUtils.find(triggers.values(), new Predicate<TriggerConfiguration>()
        {
            public boolean satisfied(TriggerConfiguration trigger)
            {
                return trigger instanceof DependentBuildTriggerConfiguration;
            }
        });
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
