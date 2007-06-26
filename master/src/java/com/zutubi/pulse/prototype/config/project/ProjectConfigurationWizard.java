package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;

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

        addWizardStates(projectType, templateParentRecord);
        addWizardStates(scmType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("scm")));
        addWizardStates(typeType, (TemplateRecord) (templateParentRecord == null ? null : templateParentRecord.get("type")));

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        MutableRecord record = projectType.createNewRecord(false);
        record.update(getStateForType(projectType).getDataRecord());
        record.put("scm", getStateForType(scmType).getDataRecord());
        record.put("type", getStateForType(typeType).getDataRecord());

        configurationTemplateManager.setParentTemplate(record, templateParentRecord.getHandle());
        if(template)
        {
            configurationTemplateManager.markAsTemplate(record);
        }
        
        successPath = configurationTemplateManager.insertRecord("project", record);
    }

    public Type getType()
    {
        return projectType;
    }
}
