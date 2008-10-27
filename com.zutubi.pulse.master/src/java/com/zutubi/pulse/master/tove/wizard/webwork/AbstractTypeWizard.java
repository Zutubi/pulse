package com.zutubi.pulse.master.tove.wizard.webwork;

import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.handler.ExtensionOptionProvider;
import com.zutubi.pulse.master.tove.model.FormDescriptor;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.pulse.master.tove.model.FormDescriptorFactory;
import com.zutubi.pulse.master.tove.model.*;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.tove.wizard.TypeWizardState;
import com.zutubi.pulse.master.tove.wizard.Wizard;
import com.zutubi.pulse.master.tove.wizard.WizardState;
import com.zutubi.pulse.master.tove.wizard.WizardTransition;
import static com.zutubi.pulse.master.tove.wizard.WizardTransition.*;

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
    private ConfigurationRegistry configurationRegistry;

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
            TwoStepStateBuilder stateBuilder = new TwoStepStateBuilder(ord++, parentPath, baseType, extensions);
            linkPreviousStates(previousStates, stateBuilder.getSelectState());
            return stateBuilder.getChainableStates();
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

    private List<AbstractChainableState> addSingleStepState(List<AbstractChainableState> previousStates, String parentPath, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
    {
        SingleStepWizardState singleStepState = new SingleStepWizardState(ord++, parentPath, baseType, type, templateRecord);
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

    public String getInsertPath()
    {
        return insertPath;
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

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
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
        private String parentPath;
        private CompositeType baseType;
        private Set<String> ignoredFields = new HashSet<String>();

        protected AbstractTypeWizardState(String id, String parentPath, CompositeType baseType)
        {
            this.id = id;
            this.parentPath = parentPath;
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
            return "configure";
        }

        @SuppressWarnings({"unchecked"})
        public void updateRecord(Map parameters)
        {
            getDataRecord().update(ToveUtils.toRecord(getType(), parameters));
        }

        public Messages getMessages()
        {
            return Messages.getInstance(getType().getClazz());
        }

        public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
        {
            CompositeType type = getType();
            FormDescriptor descriptor = formDescriptorFactory.createDescriptor(path, null, type, !template, name);
            Iterator<FieldDescriptor> fieldIt = descriptor.getFieldDescriptors().iterator();
            while(fieldIt.hasNext())
            {
                String fieldName = fieldIt.next().getName();
                if(!includesField(type, fieldName))
                {
                    ignoredFields.add(fieldName);
                    fieldIt.remove();
                }
            }

            return descriptor;
        }

        public boolean includesField(CompositeType type, String name)
        {
            TypeProperty property = type.getProperty(name);
            return !(property != null && property.getAnnotation(com.zutubi.tove.annotations.Wizard.Ignore.class) != null);
        }

        public boolean validate(ValidationAware validationCallback)
        {
            try
            {
                Configuration configuration = configurationTemplateManager.validate(parentPath, null, currentState.getRenderRecord(), !template, false, ignoredFields);
                ToveUtils.mapErrors(configuration, validationCallback, null);
                return configuration.isValid();
            }
            catch (TypeException e)
            {
                validationCallback.addActionError(e.getMessage());
                return false;
            }
        }

        public boolean hasFields()
        {
            return getType().getSimplePropertyNames().size() > 0;
        }

        public boolean hasConfigurationCheck()
        {
            return configurationRegistry.getConfigurationCheckType(getType()) != null;
        }
    }

    /**
     * A wizard state that is used for configuring a single, non-extendable
     * type.
     */
    public class SingleStepWizardState extends AbstractTypeWizardState
    {
        /**
         * Every wizard state / form is represented by a type.
         */
        private CompositeType type;
        private MutableRecord dataRecord = null;
        private TemplateRecord templateRecord;

        public SingleStepWizardState(int ord, String parentPath, CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
        {
            super(Integer.toString(ord), parentPath, baseType);
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
    public class TwoStepStateBuilder
    {
        private static final String SELECT_FIELD_NAME = "wizard.select";

        private MutableRecord selectionRecord;
        private CompositeType baseType;
        private List<CompositeType> extensions;
        private SelectWizardState selectState;
        private UnknownTypeState unknownTypeState;
        private Map<String, ConfigurationWizardState> configureStates = new TreeMap<String, ConfigurationWizardState>();

        public TwoStepStateBuilder(int ord, String parentPath, CompositeType baseType, List<CompositeType> extensions)
        {
            this.baseType = baseType;
            this.extensions = extensions;
            selectState = new SelectWizardState(ord);
            unknownTypeState = new UnknownTypeState(ord, baseType);
            for(CompositeType extensionType: baseType.getExtensions())
            {
                configureStates.put(extensionType.getSymbolicName(), new ConfigurationWizardState(ord, parentPath, extensionType));
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
                return "select";
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
                select.setName(SELECT_FIELD_NAME);
                select.setType("select");
                select.addParameter("width", 300);

                ExtensionOptionProvider optionProvider = new ExtensionOptionProvider(extensions);
                select.setListKey(optionProvider.getOptionKey());
                select.setListValue(optionProvider.getOptionValue());
                select.setList(optionProvider.getOptions(null, null, null));

                descriptor.add(select);
                return descriptor;
            }

            public boolean validate(ValidationAware validationCallback)
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

            public boolean hasFields()
            {
                return true;
            }

            public boolean hasConfigurationCheck()
            {
                return false;
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
                return "configure";
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

            public boolean validate(ValidationAware validationCallback) throws TypeException
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

            public boolean hasFields()
            {
                return false;
            }

            public boolean hasConfigurationCheck()
            {
                return false;
            }
        }
        
        private class ConfigurationWizardState extends AbstractTypeWizardState
        {
            private CompositeType type;
            private MutableRecord record;

            public ConfigurationWizardState(int ord, String parentPath, CompositeType type)
            {
                super(Integer.toString(ord) + "." + baseType.getSymbolicName(), parentPath, baseType);
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
