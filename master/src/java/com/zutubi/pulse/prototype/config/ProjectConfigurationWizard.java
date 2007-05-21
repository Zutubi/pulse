package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ProjectConfigurationWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(ProjectConfigurationWizard.class);

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecordImpl());

    private CompositeType projectType;

    public void initialise()
    {
        TemplateRecord templateRecord = EMPTY_RECORD;

        projectType = (CompositeType) configurationPersistenceManager.getType("project").getTargetType();
        
        CompositeType scmType = (CompositeType) projectType.getProperty("scm").getType();
        CompositeType typeType = (CompositeType) projectType.getProperty("type").getType();

        wizardStates = new LinkedList<TypeWizardState>();
        addWizardStates(wizardStates, projectType, templateRecord);
        addWizardStates(wizardStates, scmType, (TemplateRecord) templateRecord.get("scm"));
        addWizardStates(wizardStates, typeType, (TemplateRecord) templateRecord.get("type"));

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        MutableRecord record = projectType.createNewRecord();
        record.update(wizardStates.get(0).getRecord());
        record.put("scm", wizardStates.get(2).getRecord());
        record.put("type", wizardStates.get(4).getRecord());

        successPath = configurationPersistenceManager.insertRecord("project", record);
    }

    public Type getType()
    {
        return projectType;
    }
}
