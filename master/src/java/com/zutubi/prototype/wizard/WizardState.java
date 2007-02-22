package com.zutubi.prototype.wizard;

import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.CompositeType;

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
     * @param data
     * 
     * @return
     */
    Form getForm(Object data);
}
