package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.SimpleType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import static com.zutubi.prototype.wizard.WizardTransition.*;
import com.zutubi.pulse.i18n.Messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 */
public abstract class AbstractTypeWizard implements Wizard
{
    protected TypeRegistry typeRegistry;

    protected WizardState currentState;

    protected String successPath;

    protected LinkedList<WizardState> wizardStates = new LinkedList<WizardState>();

    protected WizardState addWizardStates(List<WizardState> wizardStates, CompositeType type, TemplateRecord templateRecord)
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

    public WizardState getCurrentState()
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

    /**
     * Required resource.
     *
     * @param typeRegistry instance
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public abstract static class TypeWizardState implements WizardState
    {
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

        public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path)
        {
            return formDescriptorFactory.createDescriptor(path, getType().getSymbolicName());
        }

        public abstract CompositeType getType();
    }

    /**
     *
     *
     */
    public static class SingleStepWizardState extends TypeWizardState
    {
        /**
         * Every wizard state / form is represented by a type.
         */
        private CompositeType type;

        private TemplateRecord templateRecord;

        private MutableRecord record = null;

        /**
         * @param type
         * @param record
         */
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
    public static class TwoStepWizardState
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
            this.selectionRecord = type.createNewRecord();
        }

        /**
         * The first state will be a select field.
         *
         * @return
         */
        public WizardState getFirstState()
        {
            // should be rendering the base type, or maybe rendering a custom type that this wizard state understands.
            // Question: how to pass the properties up to the renderer.

            // we need a base type.
            return new SelectWizardState();
        }

        public WizardState getSecondState()
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

        private class SelectWizardState implements WizardState
        {

            public SelectWizardState()
            {
                // initialise the data.
                if (getTemplateRecord() != null)
                {
                    selectionRecord.put("option", getTemplateRecord().getSymbolicName());
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
                String[] value = (String[]) parameters.get("option");
                if(value != null)
                {
                    selectionRecord.put("option", value[0]);
                }
            }

            public Messages getMessages()
            {
                return Messages.getInstance(type.getClazz());
            }

            public FormDescriptor createFormDescriptor(FormDescriptorFactory formDescriptorFactory, String path)
            {
                FormDescriptor descriptor = new FormDescriptor();
                descriptor.setId("select.state");

                FieldDescriptor select = new FieldDescriptor();
                select.setName("option");
                select.setType("select");
                select.addParameter("list", type.getExtensions());
                descriptor.add(select);
                return descriptor;
            }
        }

        private class ConfigurationWizardState extends TypeWizardState
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
