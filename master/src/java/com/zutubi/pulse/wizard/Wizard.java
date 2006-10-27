package com.zutubi.pulse.wizard;

import java.util.List;

/**
 *
 *
 */
public interface Wizard
{
    /**
     * Retrieve the current state of the wizard.
     *
     * @return the current wizard state.
     */
    Object getCurrentState();

    /**
     * Get the actions available for the current state.  For example, the first state should always return at
     * least the WizardAction.NEXT action, and the final state should always return at least the WizardAction.FINISH
     * action.
     *
     * @return a list of wizard actions.
     */
    List<WizardTransition> getAvailableActions();

    /**
     * The process method is called when the wizard is complete and no more data / input
     * is required. This method is called iff the wizard is completed successfully.
     */
    void doFinish();

    /**
     * Request that the wizard move to the next state, as determined by the current
     * state.
     *
     * @return the next state, or the current state if the traversal failed.
     */
    Object doNext();

    /**
     * Request that the wizard move back to its previous state.
     *
     * @return the next state, or the current state if the traversal failed.
     */
    Object doPrevious();

    /**
     * Notify the wizard that it has been cancelled.
     *
     */
    void doCancel();

    /**
     * Initialise this wizard. This will be called sometime after the wizard is instantiated
     * but before traverse is requested.
     */
    void initialise();

    /**
     * Restart this wizard.
     * 
     * @return return the new state.
     */
    Object doRestart();
}
