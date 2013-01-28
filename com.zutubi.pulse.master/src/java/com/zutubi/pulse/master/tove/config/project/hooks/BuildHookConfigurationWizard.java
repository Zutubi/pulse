package com.zutubi.pulse.master.tove.config.project.hooks;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.master.tove.wizard.AbstractChainableState;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This wizard walks a user through the build hook configuration process.
 */
public class BuildHookConfigurationWizard extends AbstractTypeWizard
{
    private CompositeType hookType;
    private CompositeType taskType;

    public void initialise()
    {
        hookType = typeRegistry.getType(BuildHookConfiguration.class);
        taskType = typeRegistry.getType(BuildHookTaskConfiguration.class);

        List<CompositeType> taskExtensions = taskType.getExtensions();
        List<AbstractChainableState> states = addWizardStates(null, parentPath, hookType, templateParentRecord);
        TemplateRecord inheritedRecord = (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("task"));
        if(inheritedRecord == null)
        {
            for(AbstractChainableState previousState: states)
            {
                CompositeType type = previousState.getType();
                List<CompositeType> compatibleTasks = filterTaskTypes(taskExtensions, type.getClazz());
                addWizardStatesForExtensions(Arrays.asList(previousState), parentPath, taskType, compatibleTasks);
            }
        }
        else
        {
            addWizardStates(states, null, taskType, inheritedRecord);
        }
    }

    private List<CompositeType> filterTaskTypes(List<CompositeType> taskExtensions, final Class hookClass)
    {
        return Lists.newArrayList(Iterables.filter(taskExtensions, new Predicate<CompositeType>()
        {
            public boolean apply(CompositeType compositeType)
            {
                CompatibleHooks compatible = compositeType.getAnnotation(CompatibleHooks.class, true);
                return compatible == null || CollectionUtils.contains(compatible.value(), hookClass);
            }
        }));
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = getCompletedStateForType(hookType).getDataRecord();
        record.put("task", getCompletedStateForType(taskType).getDataRecord());

        successPath = configurationTemplateManager.insertRecord(insertPath, record);
    }

    public Type getType()
    {
        return hookType;
    }
}
