package com.zutubi.tove.webwork.help;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.wizard.TypeWizardState;
import com.zutubi.tove.wizard.webwork.AbstractTypeWizard;
import com.zutubi.tove.wizard.webwork.ConfigurationWizardAction;
import com.zutubi.util.logging.Logger;

/**
 * Looks up the documentation for a wizard keyed by configuration path.  This
 * action determines the type of state we are showing and chains to an
 * appropriate action when help is available.
 */
public class WizardHelpAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(WizardHelpAction.class);

    private String path;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        AbstractTypeWizard wizardInstance = (AbstractTypeWizard) ConfigurationWizardAction.getWizardInstance(path);
        if(wizardInstance == null)
        {
            LOG.warning("Request for wizard help when no wizard appears to be running.");
            return "none";
        }

        TypeWizardState state = wizardInstance.getCurrentState();
        if(state == null)
        {
            return "none";
        }

        if(state instanceof AbstractTypeWizard.AbstractTypeWizardState)
        {
            // We are configuring a type: chain to the wizard type help action.
            return "type";
        }
        else if(state instanceof AbstractTypeWizard.TwoStepStateBuilder.SelectWizardState)
        {
            // A type selection state: chain to the wizard select help action.
            return "select";
        }
        else
        {
            // No help available.
            return "none";
        }
    }
}
