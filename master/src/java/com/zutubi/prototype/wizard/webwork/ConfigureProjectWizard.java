package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.pulse.util.logging.Logger;

import java.util.LinkedList;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public class ConfigureProjectWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(ConfigureProjectWizard.class);

    private ConfigurationPersistenceManager configurationPersistenceManager;

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecordImpl());

    private CompositeType projectType;

    public void initialise()
    {
        //todo: load template information.

        TemplateRecord templateRecord = EMPTY_RECORD;

        projectType = (CompositeType) configurationPersistenceManager.getType("project").getTargetType();
        
        CompositeType scmType = (CompositeType) projectType.getProperty("scm").getType();
        CompositeType typeType = (CompositeType) projectType.getProperty("type").getType();

        wizardStates = new LinkedList<WizardState>();
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

    /**
     * Required resource.
     *
     * @param configurationPersistenceManager
     *         instance
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
