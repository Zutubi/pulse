package com.cinnamonbob.web.wizard;

/**
 * <class-comment/>
 */
public class OneStepWizard extends BaseWizard
{
    TestWizardState state;

    public OneStepWizard()
    {
        state = new TestWizardState(this, "a");
        setCurrentState(state);
    }

}
