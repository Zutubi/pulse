package com.zutubi.pulse.web.wizard;

/**
 * <class-comment/>
 */
public class TwoStepWizard extends BaseWizard
{
    TestWizardState stateOne;
    TestWizardState stateTwo;

    public TwoStepWizard()
    {
        stateOne = new TestWizardState(this, "one");
        stateOne.setNextState("two");
        stateTwo = new TestWizardState(this, "two");
        addState(stateOne);
        addState(stateTwo);
        initialState = stateOne;

        stateTwo.setNextState("success");
    }
}
