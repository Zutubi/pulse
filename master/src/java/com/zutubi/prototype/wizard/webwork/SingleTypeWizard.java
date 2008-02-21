package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.SimpleInstantiator;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.pulse.core.config.ConfigurationCreator;

/**
 * A wizard that configures a single record. perhaps including two states if
 * there are multiple extensions to choose from.
 */
public class SingleTypeWizard extends AbstractTypeWizard
{
    private CompositeType type;
    private CompositeType creatorType;
    private CompositeType stateType;
    private ConfigurationReferenceManager configurationReferenceManager;

    public void initialise()
    {
        type = (CompositeType) configurationTemplateManager.getType(insertPath).getTargetType();
        Class creatorClass = ConventionSupport.getCreator(type);
        if(creatorClass == null)
        {
            stateType = type;
        }
        else
        {
            stateType = creatorType = typeRegistry.getType(creatorClass);
        }

        addWizardStates(null, parentPath, stateType, templateParentRecord);
    }

    public void doFinish()
    {
        super.doFinish();

        TypeWizardState recordState = getCompletedStateForType(stateType);
        MutableRecord record = recordState.getDataRecord();
        if(creatorType != null)
        {
            SimpleInstantiator instantiator = new SimpleInstantiator(configurationTemplateManager, configurationReferenceManager);
            try
            {
                ConfigurationCreator creator = (ConfigurationCreator) instantiator.instantiate(creatorType, record);
                record = type.unstantiate(creator.create());
            }
            catch (TypeException e)
            {
                throw new RuntimeException(e);
            }
        }

        successPath = configurationTemplateManager.insertRecord(insertPath, record);
    }

    public Type getType()
    {
        return type;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }
}
