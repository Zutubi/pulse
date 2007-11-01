package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.type.record.MutableRecord;

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

        List<AbstractChainableState> states = addWizardStates(null, parentPath, hookType, templateParentRecord);
        addWizardStates(states, null, taskType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("task")));
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
