package com.zutubi.pulse.master.tove.wizard;

/**
 * A simple extension of {@link TypeWizardState} which supports a settable next
 * state.
 */
public abstract class AbstractChainableState implements TypeWizardState
{
    private TypeWizardState nextState;

    public TypeWizardState getNextState()
    {
        return nextState;
    }

    public void setNextState(TypeWizardState nextState)
    {
        this.nextState = nextState;
    }
}
