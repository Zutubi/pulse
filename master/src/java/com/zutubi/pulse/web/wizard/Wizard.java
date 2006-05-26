/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.wizard;

import com.opensymphony.xwork.LocaleProvider;
import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.ValidationAware;

/**
 * 
 *
 */
public interface Wizard extends Validateable, ValidationAware
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

    /**
     * Initialise this wizard. This will be called sometime after the wizard is instantiated
     * but before traverse is requested.
     */
    void initialise();

    /**
     * Attempt to transition the wizard to the named state. If this state does not exist, an
     * illegal argument exception will be thrown. If this state is not part of the wizards current
     * history, then the wizard will be reset to the start.
     *
     * @param actualState
     */
    boolean traverseBackwardTo(String actualState);

    String restart();

    void setLocaleProvider(LocaleProvider provider);

    void clearErrors();
}
