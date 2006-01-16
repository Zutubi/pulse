package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.Validateable;

/**
 * <class-comment/>
 */
public class TestWizardState extends BaseWizardState implements Validateable
{
    private boolean executed;
    private boolean initialised;
    private boolean validated;

    private String nextState;

    public TestWizardState(Wizard wizard, String name)
    {
        super(wizard, name);
    }

    public void setNextState(String state)
    {
        this.nextState = state;
    }

    public String getNextStateName()
    {
        return nextState;
    }

    public void execute()
    {
        super.execute();
        executed = true;
    }

    public boolean isExecuted()
    {
        return executed;
    }

    public void initialise()
    {
        super.initialise();
        initialised = true;
    }

    public boolean isInitialised()
    {
        return initialised;
    }

    public void validate()
    {
        validated = true;
    }

    public boolean isValidated()
    {
        return validated;
    }
}
