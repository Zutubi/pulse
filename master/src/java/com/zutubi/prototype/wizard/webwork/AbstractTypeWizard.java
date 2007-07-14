package com.zutubi.prototype.wizard.webwork;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.ExtensionOptionProvider;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.model.SelectFieldDescriptor;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import static com.zutubi.prototype.wizard.WizardTransition.*;
import com.zutubi.pulse.core.config.Configuration;

import java.util.*;

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
     * The path we are configuring.  Either the path to a composite or a
     * collection that will hold the configured composite.
     */
    protected String configPath;
    /**
     * Path that was actually configured in the case of a successful wizard.
     * The user is taken here to see what they hath wrought.
     */
    protected String successPath;
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

    protected List<AbstractChainableState> addWizardStates(List<AbstractChainableState> previousStates, CompositeType baseType, TemplateRecord templateParentRecord)
    {
        CompositeType type = baseType;

        // If we have a template record, then we are configuring an override.
        // The type is pre-determined as it must be the same as the template
        // parent.
        if(templateParentRecord != null)
        {
            type = typeRegistry.getType(templateParentRecord.getSymbolicName());
            TemplateRecord templateRecord = new TemplateRecord("", templateParentRecord, type, type.createNewRecord(false));
            return addSingleStepState(previousStates, baseType, type, templateRecord);
        }
        else
        {
            int extensionCount = baseType.getExtensions().size();
            if(extensionCount < 2)
            {
                // No point showing the first state, just check if we are
                // configuring a type itself or a type with only one extension.
                if(extensionCount == 1)
                {
                    type = typeRegistry.getType(baseType.getExtensions().get(0));
                }

                return addSingleStepState(previousStates, baseType, type, null);
            }
            else
            {
                // Extendable types give two wizard steps: a type selection,
                // followed by a type-specific configure.
                TwoStepWizardState state = new TwoStepWizardState(ord++, baseType, typeRegistry);
                linkPreviousStates(previousStates, state.getSelectState());
                return state.getChainableStates();
            }
        }
    }

    private void linkPreviousStates(List<AbstractChainableState> previousStates, TypeWizardState newState)
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

    private List<AbstractChainableState> addSingleStepState(List<AbstractChainableState> previousStates, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
    {
        SingleStepWizardState singleStepState = new SingleStepWizardState(ord++, baseType, type, templateRecord);
        linkPreviousStates(previousStates, singleStepState);
        return Arrays.asList((AbstractChainableState)singleStepState);
    }

    public TypeWizardState getCurrentState()
    {
        return currentState;
    }

    public TypeWizardState getCompletedStateForType(CompositeType type)
    {
        for(TypeWizardState state: completedStates)
        {
            if(type.equals(state.getConfiguredBaseType()))
            {
                return state;
            }
        }
        
        return null;
    }

    public String getSuccessPath()
    {
        return successPath;
    }

    public void setParameters(String path, boolean template)
    {
        // The incoming path for a templated scope should hold the template
        // parent as its last element.
        String parentPath = PathUtils.getParentPath(path);
        if(parentPath != null && configurationTemplateManager.isTemplatedCollection(parentPath))
        {
            configPath = parentPath;
            templateParentRecord = (TemplateRecord) configurationTemplateManager.getRecord(path);
            if (templateParentRecord == null)
            {
                throw new IllegalArgumentException("Invalid wizard path '" + path + "': template parent does not exist");
            }
        }
        else
        {
            configPath = path;
        }

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
        // only step forward if there is another state to step to.
        TypeWizardState next = currentState.getNextState();
        if (next != null)
        {
            completedStates.push(currentState);
            currentState = next;
        }
        return currentState;
    }

    public WizardState doPrevious()
    {
        if (!completedStates.isEmpty())
        {
            currentState = completedStates.pop();
        }
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

    public Iterable<? extends WizardState> getStates()
    {
        List<TypeWizardState> states = new LinkedList<TypeWizardState>(completedStates);
        for(TypeWizardState current = currentState; current != null; current = current.getNextState())
        {
            states.add(current);
        }
        return states;
    }

    public abstract Type getType();

    /**
     * Required resource.
     *
     * @param typeRegistry instance
     */
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

    public abstract class AbstractChainableState implements TypeWizardState
    {
        private TypeWizardState nextState;

        public TypeWizardState getNextState()
        {
            return nextState;
        }

        public void setNextState(TypeWizardState nextState)
        {
            this.nextState = nextState;
        }
    }

    public abstract class AbstractTypeWizardState extends AbstractChainableState
    {
        private String id;
        private CompositeType baseType;
        private Set<String> ignoredFields = new HashSet<String>();

        protected AbstractTypeWizardState(String id, CompositeType baseType)
        {
            this.id = id;
            this.baseType = baseType;
        }

        public String getId()
        {
            return id;
        }

        protected CompositeType getBaseType()
        {
            return baseType;
        }

        public String getName()
        {
            return baseType.getSymbolicName();
        }

        @SuppressWarnings({"unchecked"})
        public void updateRecord(Map parameters)
        {
            getDataRecord().update(PrototypeUtils.toRecord(getType(), parameters));
        }

        public Messages getMessages()
        {
            return Messages.getInstance(getType().getClazz());
        }

        public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
        {
            CompositeType type = getType();
            FormDescriptor descriptor = formDescriptorFactory.createDescriptor(path, null, type, name);
            Iterator<FieldDescriptor> fieldIt = descriptor.getFieldDescriptors().iterator();
            while(fieldIt.hasNext())
            {
                TypeProperty property = type.getProperty(fieldIt.next().getName());
                if(property != null && property.getAnnotation(com.zutubi.config.annotations.Wizard.Ignore.class) != null)
                {
                    ignoredFields.add(property.getName());
                    fieldIt.remove();
                }
            }

            return descriptor;
        }

        public boolean validate(String path, ValidationAware validationCallback)
        {
            try
            {
                Configuration configuration = configurationTemplateManager.validate(path, null, currentState.getDataRecord(), ignoredFields);
                PrototypeUtils.mapErrors(configuration, validationCallback, null);
                return configuration.isValid();
            }
            catch (TypeException e)
            {
                validationCallback.addActionError(e.getMessage());
                return false;
            }
        }
    }

    /**
     *
     *
     */
    public class SingleStepWizardState extends AbstractTypeWizardState
    {
        /**
         * Every wizard state / form is represented by a type.
         */
        private CompositeType type;
        private MutableRecord dataRecord = null;
        private TemplateRecord templateRecord;

        public SingleStepWizardState(int ord, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
        {
            super(Integer.toString(ord), baseType);
            this.type = type;
            this.templateRecord = templateRecord;

            if (templateRecord == null)
            {
                this.dataRecord = type.createNewRecord(true);
            }
            else
            {
                this.dataRecord = (MutableRecord) templateRecord.getMoi();
            }
        }

        /**
         * The type that defines this wizards state.  It is from this type that the form is generated.
         *
         * @return the state type.
         */
        public CompositeType getType()
        {
            return type;
        }

        public CompositeType getConfiguredBaseType()
        {
            return getBaseType();
        }

        public Record getRenderRecord()
        {
            if (templateRecord == null)
            {
                return dataRecord;
            }
            else
            {
                return templateRecord;
            }
        }

        public MutableRecord getDataRecord()
        {
            return dataRecord;
        }

        @SuppressWarnings({"unchecked"})
        public void updateRecord(Map parameters)
        {
            super.updateRecord(parameters);
            if(templateRecord != null)
            {
                configurationTemplateManager.scrubInheritedValues(templateRecord, dataRecord, type);
            }
        }
    }

    /**
     * Some types need a little more than just a single wizard state / form to present all of the
     * information.  In particular, types that define multiple extension points.  These types need two-step
     * wizards.
     */
    public class TwoStepWizardState
    {
        private static final String SELECT_FIELD_NAME = "option";

        private MutableRecord selectionRecord;
        private CompositeType baseType;
        private SelectWizardState selectState;
        private UnknownTypeState unknownTypeState;
        private Map<String, ConfigurationWizardState> configureStates = new TreeMap<String, ConfigurationWizardState>();

        private TypeRegistry typeRegistry;

        public TwoStepWizardState(int ord, CompositeType baseType, TypeRegistry typeRegistry)
        {
            this.typeRegistry = typeRegistry;
            this.baseType = baseType;
            selectState = new SelectWizardState(ord);
            unknownTypeState = new UnknownTypeState(ord, baseType);
            for(String symbolicName: baseType.getExtensions())
            {
                configureStates.put(symbolicName, new ConfigurationWizardState(ord, this.typeRegistry.getType(symbolicName)));
            }
            
            this.selectionRecord = new MutableRecordImpl();
        }

        private String getSelectedSymbolicName()
        {
            return (String) selectionRecord.get(SELECT_FIELD_NAME);
        }

        public SelectWizardState getSelectState()
        {
            return selectState;
        }

        public List<AbstractChainableState> getChainableStates()
        {
            LinkedList<AbstractChainableState> result = new LinkedList<AbstractChainableState>(configureStates.values());
            result.add(unknownTypeState);
            return result;
        }

        public class SelectWizardState implements TypeWizardState
        {
            String id;

            public SelectWizardState(int ord)
            {
                id = Integer.toString(ord) + ".select";
            }

            public String getId()
            {
                return id;
            }

            public CompositeType getType()
            {
                return baseType;
            }

            public CompositeType getConfiguredBaseType()
            {
                return null;
            }

            public String getName()
            {
                return baseType.getSymbolicName() + ".select";
            }

            public Record getRenderRecord()
            {
                return selectionRecord;
            }

            public MutableRecord getDataRecord()
            {
                return selectionRecord;
            }

            public void updateRecord(Map parameters)
            {
                String[] value = (String[]) parameters.get(SELECT_FIELD_NAME);
                if(value != null)
                {
                    selectionRecord.put(SELECT_FIELD_NAME, value[0]);
                }
            }

            public Messages getMessages()
            {
                return Messages.getInstance(baseType.getClazz());
            }

            public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
            {
                FormDescriptor descriptor = new FormDescriptor();
                descriptor.setName(name);
                descriptor.setId("select.state");

                SelectFieldDescriptor select = new SelectFieldDescriptor();
                select.setName("option");
                select.setType("select");

                ExtensionOptionProvider optionProvider = new ExtensionOptionProvider(baseType);
                optionProvider.setTypeRegistry(typeRegistry);
                select.setListKey(optionProvider.getOptionKey());
                select.setListValue(optionProvider.getOptionValue());
                select.setList(optionProvider.getOptions(null, null, null));

                descriptor.add(select);
                return descriptor;
            }

            public boolean validate(String path, ValidationAware validationCallback)
            {
                return true;
            }

            public TypeWizardState getNextState()
            {
                String selectedType = getSelectedSymbolicName();
                if(selectedType == null)
                {
                    return unknownTypeState;
                }
                else
                {
                    return configureStates.get(selectedType);
                }
            }
        }

        private class UnknownTypeState extends AbstractChainableState
        {
            private String id;
            private CompositeType baseType;

            public UnknownTypeState(int ord, CompositeType baseType)
            {
                id = Integer.toString(ord) + ".unknown";
                this.baseType = baseType;
            }

            public String getId()
            {
                return id;
            }

            public String getName()
            {
                return baseType.getSymbolicName();
            }

            public Record getRenderRecord()
            {
                throw new UnsupportedOperationException("This state cannot be rendered");
            }

            public MutableRecord getDataRecord()
            {
                return null;
            }

            public void updateRecord(Map parameters)
            {
                throw new UnsupportedOperationException("This state cannot be rendered");
            }

            public Messages getMessages()
            {
                return Messages.getInstance(baseType.getClazz());
            }

            public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
            {
                throw new UnsupportedOperationException("This state cannot be rendered");
            }

            public boolean validate(String path, ValidationAware validationCallback) throws TypeException
            {
                return true;
            }

            public CompositeType getConfiguredBaseType()
            {
                return null;
            }

            public CompositeType getType()
            {
                return baseType;
            }
        }
        
        private class ConfigurationWizardState extends AbstractTypeWizardState
        {
            private CompositeType type;
            private MutableRecord record;

            public ConfigurationWizardState(int ord, CompositeType type)
            {
                super(Integer.toString(ord) + "." + baseType.getSymbolicName(), baseType);
                this.type = type;
                this.record = type.createNewRecord(true);
            }

            public CompositeType getType()
            {
                return type;
            }

            public CompositeType getConfiguredBaseType()
            {
                return baseType;
            }

            public Record getRenderRecord()
            {
                return getDataRecord();
            }

            public MutableRecord getDataRecord()
            {
                return record;
            }
        }
    }
}
