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
    protected LinkedList<TypeWizardState> wizardStates = new LinkedList<TypeWizardState>();
    protected Map<String, TypeWizardState> configureStates = new HashMap<String, TypeWizardState>();

    protected TypeWizardState currentState;

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

    protected TypeRegistry typeRegistry;
    protected ConfigurationPersistenceManager configurationPersistenceManager;
    protected ConfigurationTemplateManager configurationTemplateManager;

    protected TypeWizardState addWizardStates(CompositeType baseType, TemplateRecord templateParentRecord)
    {
        CompositeType type = baseType;

        // If we have a template record, then we are configuring an override.
        // The type is pre-determined as it must be the same as the template
        // parent.
        if(templateParentRecord != null)
        {
            type = typeRegistry.getType(templateParentRecord.getSymbolicName());
            TemplateRecord templateRecord = new TemplateRecord("", templateParentRecord, type, type.createNewRecord(false));
            SingleStepWizardState singleStepState = new SingleStepWizardState(baseType, type, templateRecord);
            addState(singleStepState, true);
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

                SingleStepWizardState singleStepState = new SingleStepWizardState(baseType, type, null);
                addState(singleStepState, true);
            }
            else
            {
                TwoStepWizardState state = new TwoStepWizardState(baseType);
                state.setTypeRegistry(typeRegistry);
                addState(state.getFirstState(), false);
                addState(state.getSecondState(), true);
            }
        }

        return wizardStates.get(wizardStates.size() - 1);
    }

    private void addState(TypeWizardState state, boolean configuration)
    {
        wizardStates.add(state);
        if(configuration)
        {
            configureStates.put(state.getBaseType().getSymbolicName(), state);
        }
    }

    public TypeWizardState getCurrentState()
    {
        return currentState;
    }

    public TypeWizardState getStateForType(CompositeType type)
    {
        return configureStates.get(type.getSymbolicName());
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
        int currentIndex = wizardStates.indexOf(currentState);
        if (currentIndex > 0)
        {
            transitions.add(PREVIOUS);
        }
        if (currentIndex < wizardStates.size() - 1)
        {
            transitions.add(NEXT);
        }
        if (currentIndex == wizardStates.size() - 1)
        {
            transitions.add(FINISH);
        }
        transitions.add(CANCEL);

        return transitions;
    }

    public WizardState doNext()
    {
        // only step forward if there is another state to step to.
        int currentIndex = wizardStates.indexOf(currentState);
        if (currentIndex < wizardStates.size() - 1)
        {
            currentState = wizardStates.get(currentIndex + 1);
        }
        return currentState;
    }

    public WizardState doPrevious()
    {
        int currentIndex = wizardStates.indexOf(currentState);
        if (currentIndex > 0)
        {
            currentState = wizardStates.get(currentIndex - 1);
        }
        return currentState;
    }

    public WizardState doRestart()
    {
        currentState = wizardStates.getFirst();
        return currentState;
    }

    public void doCancel()
    {
        // cleanup any resources.
    }

    public int getStateCount()
    {
        return wizardStates.size();
    }

    public int getCurrentStateIndex()
    {
        return wizardStates.indexOf(currentState);
    }

    public Iterable<? extends WizardState> getStates()
    {
        return wizardStates;
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

    public abstract class AbstractTypeWizardState implements TypeWizardState
    {
        private CompositeType baseType;
        private Set<String> ignoredFields = new HashSet<String>();

        protected AbstractTypeWizardState(CompositeType baseType)
        {
            this.baseType = baseType;
        }

        public CompositeType getBaseType()
        {
            return baseType;
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

        public SingleStepWizardState(CompositeType baseType, CompositeType type, TemplateRecord templateRecord)
        {
            super(baseType);
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

        public String getName()
        {
            return getBaseType().getSymbolicName();
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
        private MutableRecord selectionRecord;
        private CompositeType baseType;
        private Map<String, MutableRecord> typeRecordCache = new TreeMap<String, MutableRecord>();

        private TypeRegistry typeRegistry;

        public TwoStepWizardState(CompositeType baseType)
        {
            this.baseType = baseType;
            this.selectionRecord = new MutableRecordImpl();
        }

        /**
         * The first state will be a select field.
         *
         * @return the first wizard state
         */
        public TypeWizardState getFirstState()
        {
            // should be rendering the base type, or maybe rendering a custom type that this wizard state understands.
            // Question: how to pass the properties up to the renderer.

            // we need a base type.
            return new SelectWizardState();
        }

        public TypeWizardState getSecondState()
        {
            // should be rendering the selected type.

            // This second wizard state is the one that populates the internal record.  And it also does one thing
            // extra - sets the symbolic name for the type selected in step one.

            // getRecord() -> set symbolicName during this call.

            return new ConfigurationWizardState();
        }

        public void setTypeRegistry(TypeRegistry typeRegistry)
        {
            this.typeRegistry = typeRegistry;
        }

        public class SelectWizardState implements TypeWizardState
        {
            private static final String SELECT_FIELD_NAME = "option";

            public CompositeType getBaseType()
            {
                return baseType;
            }

            public CompositeType getType()
            {
                return baseType;
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
        }

        private class ConfigurationWizardState extends AbstractTypeWizardState
        {
            public ConfigurationWizardState()
            {
                super(baseType);
            }

            public CompositeType getType()
            {
                String selectedSymbolicName = getSelectedSymbolicName();
                return typeRegistry.getType(selectedSymbolicName);
            }

            public String getName()
            {
                return baseType.getSymbolicName();
            }

            public Record getRenderRecord()
            {
                return getDataRecord();
            }

            public MutableRecord getDataRecord()
            {
                String selectedSymbolicName = getSelectedSymbolicName();
                if (!typeRecordCache.containsKey(selectedSymbolicName))
                {
                    CompositeType selectedType = typeRegistry.getType(selectedSymbolicName);
                    typeRecordCache.put(selectedSymbolicName, selectedType.createNewRecord(true));
                }
                return typeRecordCache.get(selectedSymbolicName);
            }

            private String getSelectedSymbolicName()
            {
                return (String) selectionRecord.get("option");
            }
        }
    }

}
