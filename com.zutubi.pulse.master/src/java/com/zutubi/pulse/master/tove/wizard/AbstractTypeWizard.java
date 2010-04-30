package com.zutubi.pulse.master.tove.wizard;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static com.zutubi.pulse.master.tove.wizard.WizardTransition.*;

/**
 *
 * 
 */
public abstract class AbstractTypeWizard implements Wizard
{
    protected TypeWizardState rootState;
    protected TypeWizardState currentState;
    protected Stack<TypeWizardState> completedStates = new Stack<TypeWizardState>();

    /**
     * The path of the parent instance that will hold a successfully-
     * configured result.
     */
    protected String parentPath;
    /**
     * The path we will insert to.  Either the path to a composite or a
     * collection that will hold the configured composite.
     */
    protected String insertPath;
    /**
     * Path that was actually configured in the case of a successful wizard.
     * The user is taken here to see what they hath wrought.
     */
    protected String successPath;
    /**
     * The path to our template parent, if we have one.
     */
    protected String templateParentPath;
    /**
     * Template parent record, if we have a template parent.
     */
    protected TemplateRecord templateParentRecord;
    /**
     * If true, we are configuring a template (as opposed to concrete) record.
     */
    protected boolean template;

    protected int ord = 0;

    protected TypeRegistry typeRegistry;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected ConfigurationTemplateManager configurationTemplateManager;

    public int getNextUniqueId()
    {
        return ord++;
    }
    
    protected List<AbstractChainableState> addWizardStates(List<AbstractChainableState> previousStates, String parentPath, CompositeType baseType, TemplateRecord templateParentRecord)
    {
        // If we have a template record, then we are configuring an override.
        // The type is pre-determined as it must be the same as the template
        // parent.
        if(templateParentRecord != null)
        {
            CompositeType type = typeRegistry.getType(templateParentRecord.getSymbolicName());
            TemplateRecord templateRecord = new TemplateRecord("", templateParentRecord, type, type.createNewRecord(false));
            return addSingleStepState(previousStates, parentPath, baseType, type, templateRecord);
        }
        else
        {
            return addWizardStatesForExtensions(previousStates, parentPath, baseType, baseType.getExtensions());
        }
    }

    protected List<AbstractChainableState> addWizardStatesForExtensions(List<AbstractChainableState> previousStates, String parentPath, CompositeType baseType, List<CompositeType> extensions)
    {
        CompositeType type = baseType;
        int extensionCount = extensions.size();
        if(extensionCount < 2)
        {
            // No point showing the first state, just check if we are
            // configuring a type itself or a type with only one extension.
            if(extensionCount == 1)
            {
                type = extensions.get(0);
            }

            return addSingleStepState(previousStates, parentPath, baseType, type, null);
        }
        else
        {
            // Extendable types give two wizard steps: a type selection,
            // followed by a type-specific configure.
            TwoStepStateBuilder stateBuilder = new TwoStepStateBuilder(this, getNextUniqueId(), parentPath, baseType, extensions);
            addWizardState(previousStates, stateBuilder.getSelectState());
            return stateBuilder.getChainableStates();
        }
    }

    protected void addWizardState(List<AbstractChainableState> previousStates, TypeWizardState newState)
    {
        if (previousStates == null)
        {
            rootState = currentState = newState;
        }
        else
        {
            for (AbstractChainableState previousState: previousStates)
            {
                previousState.setNextState(newState);
            }
        }
    }

    protected List<AbstractChainableState> addSingleStepState(List<AbstractChainableState> previousStates, String parentPath, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
    {
        SingleStepWizardState singleStepState = new SingleStepWizardState(this, getNextUniqueId(), parentPath, baseType, type, templateRecord);
        SpringComponentContext.autowire(singleStepState);
        addWizardState(previousStates, singleStepState);
        return Arrays.asList((AbstractChainableState)singleStepState);
    }

    public TypeWizardState getCurrentState()
    {
        return currentState;
    }

    public TypeWizardState getCompletedStateForType(final CompositeType type)
    {
        return CollectionUtils.find(completedStates, new Predicate<TypeWizardState>()
        {
            public boolean satisfied(TypeWizardState state)
            {
                return type.equals(state.getConfiguredBaseType());
            }
        });
    }

    public String getSuccessPath()
    {
        return successPath;
    }

    public String getInsertPath()
    {
        return insertPath;
    }

    public String getTemplateParentPath()
    {
        return templateParentPath;
    }

    public boolean isTemplate()
    {
        return template;
    }

    public void setParameters(String parentPath, String insertPath, String templateParentPath, TemplateRecord templateParentRecord, boolean template)
    {
        this.parentPath = parentPath;
        this.insertPath = insertPath;
        this.templateParentPath = templateParentPath;
        this.templateParentRecord = templateParentRecord;
        this.template = template;
    }

    public List<WizardTransition> getAvailableActions()
    {
        // the available wizard transitions depend in part on the current state, and in part on the 'state' of the
        // wizard itself. We can finish if and only if enough of the wizard is complete... will need a separate
        // callback to check this.

        List<WizardTransition> transitions = new LinkedList<WizardTransition>();

        // locate index of current state.
        if (!completedStates.isEmpty())
        {
            transitions.add(PREVIOUS);
        }
        if (currentState.getNextState() == null)
        {
            transitions.add(FINISH);
        }
        else
        {
            transitions.add(NEXT);
        }
        transitions.add(CANCEL);

        return transitions;
    }

    public WizardState doNext()
    {
        do
        {
            completedStates.push(currentState);
            currentState = currentState.getNextState();
        }
        while(currentState != null && !currentState.hasFields());

        return currentState;
    }

    public WizardState doPrevious()
    {
        do
        {
            if (completedStates.isEmpty())
            {
                break;
            }

            currentState = completedStates.pop();
        }
        while(!currentState.hasFields());

        return currentState;
    }

    public void doFinish()
    {
        completedStates.push(currentState);
        currentState = null;
    }

    public WizardState doRestart()
    {
        currentState = rootState;
        completedStates.clear();
        return currentState;
    }

    public void doCancel()
    {
        // cleanup any resources.
    }

    public boolean isSingleStep()
    {
        return rootState.getNextState() == null;
    }

    public Iterable<? extends TypeWizardState> getStates()
    {
        List<TypeWizardState> states = new LinkedList<TypeWizardState>(completedStates);
        for(TypeWizardState current = currentState; current != null; current = current.getNextState())
        {
            states.add(current);
        }
        return states;
    }

    public abstract Type getType();

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
