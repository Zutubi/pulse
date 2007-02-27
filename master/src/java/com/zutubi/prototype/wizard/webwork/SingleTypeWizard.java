package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.TemplateRecord;
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

    private static final TemplateRecord EMPTY_RECORD = new TemplateRecord("empty", null, new MutableRecord());

    public SingleTypeWizard(String path)
    {
        this.path = path;
    }

    public void initialise()
    {
        Type type = configurationPersistenceManager.getType(path);
        if (type instanceof CollectionType)
        {
            type = ((CollectionType) type).getCollectionType();
        }

        LOG.warning("TODO: load template record for path: " + path + ", currently using empty template record.");
        
        // for now, use an empty template record.
        TemplateRecord templateRecord = EMPTY_RECORD;

        // the template record represents the existing data.
        addWizardStates(wizardStates, type, templateRecord);

        currentState = wizardStates.getFirst();
    }

    public void doFinish()
    {
        LOG.warning("TODO: implement persistence of data.");
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
