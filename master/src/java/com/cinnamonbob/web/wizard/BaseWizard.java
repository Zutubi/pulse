package com.cinnamonbob.web.wizard;

import com.opensymphony.util.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class BaseWizard implements Wizard
{
    private WizardState currentState;

    private Map<String, WizardState> states = new HashMap<String, WizardState>();

    public BaseWizard()
    {

    }

    public WizardState getCurrentState()
    {
        return currentState;
    }

    public void setCurrentState(WizardState currentState)
    {
        this.currentState = currentState;
    }

    public WizardState getState(String state)
    {
        return states.get(state);
    }

    public void addState(WizardState state)
    {
        if (!TextUtils.stringSet(state.getStateName()))
        {
            throw new IllegalArgumentException();
        }
        states.put(state.getStateName(), state);
    }

    public boolean isComplete()
    {
        return currentState == null;
    }

    public void process()
    {

    }
}
