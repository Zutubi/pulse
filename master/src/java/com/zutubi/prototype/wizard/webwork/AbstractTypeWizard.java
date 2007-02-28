package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.TemplateRecord;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import static com.zutubi.prototype.wizard.WizardTransition.*;
import com.zutubi.pulse.prototype.config.ConfigurationExtension;

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

    protected LinkedList<WizardState> wizardStates = new LinkedList<WizardState>();

    protected void addWizardStates(List<WizardState> wizardStates, CompositeType type, TemplateRecord templateRecord)
    {
        // this extension thing is a little awkward, makes sense in theory, but a little awkward in practice
        if (ConfigurationExtension.class.isAssignableFrom(type.getClazz()))
        {
            if (type.getExtensions().size() > 1)
            {
                TwoStepWizardState state = new TwoStepWizardState(type, templateRecord);
                state.setTypeRegistry(typeRegistry);
                wizardStates.add(state.getFirstState());
                wizardStates.add(state.getSecondState());
            }
            else
            {
                SingleStepWizardState singleStepState = new SingleStepWizardState(type, templateRecord);
                wizardStates.add(singleStepState);
            }
        }
        else
        {
            SingleStepWizardState singleStepState = new SingleStepWizardState(type, templateRecord);
            wizardStates.add(singleStepState);
        }
    }

    public WizardState getCurrentState()
    {
        return currentState;
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

    /**
     *
     *
     */
    public static class SingleStepWizardState implements WizardState
    {
        /**
         * Every wizard state / form is represented by a type.
         */
        private CompositeType type;

        /**
         * The record stores the persistent data for this wizard state.
         */
        private TemplateRecord templateRecord;

        private MutableRecord record = new MutableRecord();

        /**
         * @param type
         * @param record
         */
        public SingleStepWizardState(CompositeType type, TemplateRecord record)
        {
            this.type = type;
            this.templateRecord = record;

            // extract initial values from the template record.
            if (record != null)
            {
                for (TypeProperty property : type.getProperties(PrimitiveType.class))
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

        /**
         * The record of data for this state.  It is from the record that form fields are pre populated
         * and data is recorded.
         *
         * @return the state record.
         */
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

        private MutableRecord selectionRecord = new MutableRecord();

        private Map<String, MutableRecord> typeRecordCache = new TreeMap<String, MutableRecord>();

        public TwoStepWizardState(CompositeType type, TemplateRecord record)
        {
            this.type = type;
            this.record = record;
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
        }

        private class ConfigurationWizardState implements WizardState
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
                    MutableRecord r = new MutableRecord();
                    r.setSymbolicName(selectedSymbolicName);
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
