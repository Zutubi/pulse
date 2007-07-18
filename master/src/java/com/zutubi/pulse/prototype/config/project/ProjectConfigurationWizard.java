package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;

import java.util.List;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ProjectConfigurationWizard extends AbstractTypeWizard
{
    private CompositeType projectType;
    private CompositeType scmType;
    private CompositeType typeType;

    public void initialise()
    {
        projectType = typeRegistry.getType(ProjectConfiguration.class);

        scmType = (CompositeType) projectType.getProperty("scm").getType();
        typeType = (CompositeType) projectType.getProperty("type").getType();

        List<AbstractChainableState> states = addWizardStates(null, parentPath, projectType, templateParentRecord);
        states = addWizardStates(states, null, scmType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("scm")));
        addWizardStates(states, null, typeType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("type")));
    }

    public void doFinish()
    {
        super.doFinish();

        MutableRecord record = projectType.createNewRecord(false);
        record.update(getCompletedStateForType(projectType).getDataRecord());
        record.put("scm", getCompletedStateForType(scmType).getDataRecord());
        record.put("type", getCompletedStateForType(typeType).getDataRecord());

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        
        successPath = configurationTemplateManager.insertRecord(ConfigurationRegistry.PROJECTS_SCOPE, record);
    }

    public Type getType()
    {
        return projectType;
    }
}
