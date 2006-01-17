package com.cinnamonbob.web.wizard;

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
    WizardState getCurrentState();

    /**
     * Returns true if this wizard has been completed and requires processing.
     *
     * @return true if complete, false otherwise.
     */
    boolean isComplete();

    /**
     * The process method is called when the wizard is complete and no more data / input
     * is required. This method is called iff the wizard is completed successfully.
     */
    void process();

    /**
     * Request that the wizard move to the next state, as determined by the current
     * state.
     *
     * @return the next state, or the current state if the traversal failed.
     */
    String traverseForward();

    /**
     * Request that the wizard move back to its previous state.
     *
     * @return the next state, or the current state if the traversal failed.
     */
    String traverseBackward();

    /**
     * Notify the wizard that it has been cancelled.
     *
     */
    void cancel();
}
