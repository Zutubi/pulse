package com.cinnamonbob.web.wizard;

/**
 *
 */
public final class WizardCompleteState extends BaseWizardState
{
    private boolean haveProcessedWizard = false;

    public WizardCompleteState(Wizard wizard, String name)
    {
        super(wizard, name);
    }

    public String getNextState()
    {
        return this.getWizardStateName();
    }

    public void initialise()
    {
        execute();
    }
    
    public void execute()
    {
        // ensure that we do not process the wizard a second time.
        if (!haveProcessedWizard)
        {
            getWizard().process();
            haveProcessedWizard = true;
        }
    }
}
