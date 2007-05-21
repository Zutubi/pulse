package com.zutubi.prototype.wizard.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.model.SelectFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.SimpleType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.prototype.wizard.TypeWizardState;
import static com.zutubi.prototype.wizard.WizardTransition.*;
import com.zutubi.validation.ValidationAware;

import java.util.*;

/**
 *
 * 
 */
public abstract class AbstractTypeWizard implements Wizard
{
    protected TypeRegistry typeRegistry;
    protected ConfigurationPersistenceManager configurationPersistenceManager;

    protected TypeWizardState currentState;

    protected String successPath;

    protected LinkedList<TypeWizardState> wizardStates = new LinkedList<TypeWizardState>();

    protected TypeWizardState addWizardStates(List<TypeWizardState> wizardStates, CompositeType type, TemplateRecord templateRecord)
    {
        int extensionCount = type.getExtensions().size();
        if(extensionCount < 2)
        {
            // No point showing the first state, just check if we are
            // configuring a type itself or a type with only one extension.
            if(extensionCount == 1)
            {
                type = typeRegistry.getType(type.getExtensions().get(0));
            }

            SingleStepWizardState singleStepState = new SingleStepWizardState(type, templateRecord);
            wizardStates.add(singleStepState);
        }
        else
        {
            TwoStepWizardState state = new TwoStepWizardState(type, templateRecord);
            state.setTypeRegistry(typeRegistry);
            wizardStates.add(state.getFirstState());
            wizardStates.add(state.getSecondState());
        }

        return wizardStates.get(wizardStates.size() - 1);
    }

    public TypeWizardState getCurrentState()
    {
        return currentState;
    }

    public String getSuccessPath()
    {
        return successPath;
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

    /**
     * Required resource.
     * 
     * @param configurationPersistenceManager
     */
    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public abstract class AbstractTypeWizardState implements TypeWizardState
    {
        private Set<String> ignoredFields = new HashSet<String>();
        
        @SuppressWarnings({"unchecked"})
        public void updateRecord(Map parameters)
        {
            Record post = PrototypeUtils.toRecord(getType(), parameters);

            // apply the posted record details to the current state's record.
            getRecord().update(post);
        }

        public Messages getMessages()
        {
            return Messages.getInstance(getType().getClazz());
        }

        public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
        {
            CompositeType type = getType();
            FormDescriptor descriptor = formDescriptorFactory.createDescriptor(path, type, name);
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
            validationCallback.addIgnoredFields(ignoredFields);
            return configurationPersistenceManager.validate(path, null, currentState.getRecord(), validationCallback) != null;
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

        private TemplateRecord templateRecord;

        private MutableRecord record = null;

        public SingleStepWizardState(CompositeType type, TemplateRecord record)
        {
            this.type = type;
            this.templateRecord = record;
            this.record = type.createNewRecord();

            // extract initial values from the template record.
            if (record != null)
            {
                for (TypeProperty property : type.getProperties(SimpleType.class))
                {
                    this.record.put(property.getName(), record.get(property.getName()));
                }
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

        public TemplateRecord getTemplateRecord()
        {
            return templateRecord;
        }

        public MutableRecord getRecord()
        {
            return record;
        }
    }

    /**
     * Some types need a little more than just a single wizard state / form to present all of the
     * information.  In particular, types that define multiple extension points.  These types need two-step
     * wizards.
     */
    public class TwoStepWizardState
    {
        private TypeRegistry typeRegistry;

        private CompositeType type;

        private TemplateRecord record;

        private MutableRecord selectionRecord;

        private Map<String, MutableRecord> typeRecordCache = new TreeMap<String, MutableRecord>();

        public TwoStepWizardState(CompositeType type, TemplateRecord record)
        {
            this.type = type;
            this.record = record;
            this.selectionRecord = new MutableRecordImpl();
            selectionRecord.setSymbolicName(type.getSymbolicName());
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

            public SelectWizardState()
            {
                // initialise the data.
                if (getTemplateRecord() != null)
                {
                    selectionRecord.put(SELECT_FIELD_NAME, getTemplateRecord().getSymbolicName());
                }
            }

            public TemplateRecord getTemplateRecord()
            {
                return record;
            }

            public CompositeType getType()
            {
                return type;
            }

            public MutableRecord getRecord()
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
                return Messages.getInstance(type.getClazz());
            }

            public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path, String name)
            {
                FormDescriptor descriptor = new FormDescriptor();
                descriptor.setName(name);
                descriptor.setId("select.state");

                SelectFieldDescriptor select = new SelectFieldDescriptor();
                select.setName("option");
                select.setType("select");
                select.setList(type.getExtensions());
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
                // initialise the states data using the template record if it exists.
                if (record != null)
                {
                    MutableRecord data = record.flatten();
                    typeRecordCache.put(type.getSymbolicName(), data);
                }
            }

            public TemplateRecord getTemplateRecord()
            {
                return record;
            }

            public CompositeType getType()
            {
                String selectedSymbolicName = getSelectedSymbolicName();
                return typeRegistry.getType(selectedSymbolicName);
            }

            public MutableRecord getRecord()
            {
                String selectedSymbolicName = getSelectedSymbolicName();
                if (!typeRecordCache.containsKey(selectedSymbolicName))
                {
                    CompositeType selectedType = typeRegistry.getType(selectedSymbolicName);
                    MutableRecord r = selectedType.createNewRecord();
                    typeRecordCache.put(selectedSymbolicName, r);
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
