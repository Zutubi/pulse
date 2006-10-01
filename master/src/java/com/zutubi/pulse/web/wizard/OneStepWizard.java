package com.zutubi.pulse.web.wizard;

/**
 * <class-comment/>
 */
public class OneStepWizard extends BaseWizard
{
    TestWizardState state;
    WizardCompleteState completeState;

    public OneStepWizard()
    {
        state = new TestWizardState(this, "a");
        state.setNextState("success");
        addInitialState(state);
    }

}
