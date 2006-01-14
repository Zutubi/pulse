package com.cinnamonbob.web.wizard;

/**
 * <class-comment/>
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
     *
     */
    void process();

    String traverseForward();

    String traverseBackward();

    void cancel();
}
