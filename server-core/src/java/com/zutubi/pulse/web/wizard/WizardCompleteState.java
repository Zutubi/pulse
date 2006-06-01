package com.zutubi.pulse.web.wizard;

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

    public String getNextStateName()
    {
        return this.getStateName();
    }

    public void initialise()
    {
        super.initialise();
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

    public void reset()
    {

    }
}
