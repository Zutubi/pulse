package com.zutubi.prototype.wizard;

import com.zutubi.prototype.model.Form;

/**
 *
 *
 */
public interface WizardState
{
    /**
     * The name of the wizards state should be able to uniquely identify this state within the wizard
     *
     * @return
     */
    String getName();

    Object getData();

    /**
     * Generate the form to be displayed for this wizard state.
     * 
     * @return
     */
    Form getForm();
}
