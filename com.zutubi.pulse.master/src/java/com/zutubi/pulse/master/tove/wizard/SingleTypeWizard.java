package com.zutubi.pulse.master.tove.wizard;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.api.ConfigurationCreator;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.SimpleInstantiator;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.MutableRecord;

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
            SimpleInstantiator instantiator = new SimpleInstantiator(null, configurationReferenceManager, configurationTemplateManager);
            try
            {
                ConfigurationCreator creator = (ConfigurationCreator) instantiator.instantiate(creatorType, record);
                record = type.unstantiate(creator.create(), null);
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
