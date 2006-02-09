package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.ValidationAware;

/**
 * <class-comment/>
 */
public interface WizardState extends ValidationAware
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
     * @return
     */
    String getStateName();

    /**
     * Return the name of the state following this one. This method will be called after
     * the state has been validated and executed.
     *
     * @return
     */
    String getNextStateName();

    /**
     * Clear any errors from a previous validation attempt.
     */
    void clearErrors();

    /**
     * Reset this states default values.
     */
    void reset();
}
