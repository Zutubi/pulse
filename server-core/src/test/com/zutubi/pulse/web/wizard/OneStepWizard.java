package com.cinnamonbob.web.wizard;

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
        completeState = new WizardCompleteState(this, "success");
        state.setNextState(completeState.getStateName());
        addInitialState(state.getStateName(), state);
        addFinalState(completeState.getStateName(), completeState);
    }

}
