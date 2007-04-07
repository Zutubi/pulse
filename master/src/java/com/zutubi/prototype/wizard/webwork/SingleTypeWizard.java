package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.pulse.util.logging.Logger;

/**
 *
 *
 */
public class SingleTypeWizard extends AbstractTypeWizard
{
    private static final Logger LOG = Logger.getLogger(SingleTypeWizard.class);

    private ConfigurationPersistenceManager configurationPersistenceManager;

    private String path;

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecordImpl());
    private WizardState recordState;

    public SingleTypeWizard(String path)
    {
        this.path = path;
    }

    public void initialise()
    {
        CompositeType type = (CompositeType) configurationPersistenceManager.getType(path).getTargetType();

        LOG.warning("TODO: load template record for path: " + path + ", currently using empty template record.");

        // for now, use an empty template record.
        TemplateRecord templateRecord = EMPTY_RECORD;

        // the template record represents the existing data.
        recordState = addWizardStates(wizardStates, type, templateRecord);

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        configurationPersistenceManager.insertRecord(path, recordState.getRecord());
        successPath = path;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
