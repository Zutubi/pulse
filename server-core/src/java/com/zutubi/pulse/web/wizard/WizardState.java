/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.wizard;

/**
 * <class-comment/>
 */
public interface WizardState
{
    /**
     * Execute this state. This method is called when this wizard state has been
     * successfully validated.
     */
    void execute();

    /**
     * Initialise this state. This method is called when a state is about to be
     * displayed.
     */
    void initialise();

    /**
     * Return the name of this state.
     *
     */
    String getStateName();

    /**
     * Return the name of the state following this one. This method will be called after
     * the state has been validated and executed.
     *
     */
    String getNextStateName();

    /**
     * Reset this states default values.
     */
    void reset();
}
