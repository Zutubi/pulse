package com.zutubi.prototype.wizard.webwork;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import static com.zutubi.prototype.wizard.WizardTransition.*;
import com.zutubi.pulse.prototype.config.ConfigurationExtension;

import java.util.LinkedList;
import java.util.List;

/**
 * This wizard walks a user through the project configuration process. During project configuration,
 * a user needs to configure the projects type, scm and general details.
 *
 */
public abstract class AbstractTypeWizard implements Wizard
{
    protected TypeRegistry typeRegistry;

    protected WizardState currentState;

    protected LinkedList<WizardState> wizardStates;

    protected void addWizardStates(LinkedList<WizardState> states, Type type, Record record)
    {
        // this extension thing is a little awkward, makes sense in theory, but a little awkward in practice
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            if (ConfigurationExtension.class.isAssignableFrom(ctype.getClazz()))
            {
                if (ctype.getExtensions().size() > 1)
                {
                    TwoStepWizardState state = new TwoStepWizardState(type, record);
                    state.setTypeRegistry(typeRegistry);
                    states.add(state.getFirstState());
                    states.add(state.getSecondState());
                }
                else
                {
                    SingleStepWizardState singleStepState = new SingleStepWizardState(type, record);
                    states.add(singleStepState);
                }
            }
            else
            {
                SingleStepWizardState singleStepState = new SingleStepWizardState(type, record);
                states.add(singleStepState);
            }
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
        if (currentIndex < wizardStates.size() -1)
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
        if (currentIndex < wizardStates.size() -1)
        {
            currentState = wizardStates.get(currentIndex +1);
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
        private Type type;

        /**
         * The record stores the persistent data for this wizard state.
         */
        private Record record;

        /**
         *
         * @param type
         * @param record
         */
        public SingleStepWizardState(Type type, Record record)
        {
            this.type = type;
            this.record = record;
        }

        /**
         * The type that defines this wizards state.  It is from this type that the form is generated.
         *
         * @return the state type.
         */
        public Type getType()
        {
            return type;
        }

        /**
         * The record of data for this state.  It is from the record that form fields are pre populated
         * and data is recorded.
         *
         * @return the state record.
         */
        public Record getRecord()
        {
            record.setSymbolicName(type.getSymbolicName());
            return record;
        }
    }

    /**
     * Some types need a little more than just a single wizard state / form to present all of the
     * information.  In particular, types that define multiple extension points.  These types need two-step
     * wizards.
     *
     *
     */
    public static class TwoStepWizardState
    {
        private TypeRegistry typeRegistry;

        private Type type;

        private Record record;

        private Record selectionRecord = new MutableRecord();

        public TwoStepWizardState(Type type, Record record)
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
            public Record getRecord()
            {
                selectionRecord.setSymbolicName(type.getSymbolicName());
                return selectionRecord;
            }

            public Type getType()
            {
                return type;
            }
        }

        private class ConfigurationWizardState implements WizardState
        {
            public Record getRecord()
            {
                String selectedSymbolicName = (String) selectionRecord.get("option");
                record.setSymbolicName(selectedSymbolicName);
                return record;
            }

            public Type getType()
            {
                // this selection record provides data from a ConfigurationExtension implementation.
                String selectedSymbolicName = (String) selectionRecord.get("option");
                return typeRegistry.getType(selectedSymbolicName);
            }
        }
    }

}
