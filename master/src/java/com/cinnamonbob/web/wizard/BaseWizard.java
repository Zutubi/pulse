package com.cinnamonbob.web.wizard;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.validator.ActionValidatorManager;
import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.Validateable;
import com.cinnamonbob.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * <class-comment/>
 */
public class BaseWizard implements Wizard
{
    private Stack<WizardState> history = new Stack<WizardState>();

    private static final Logger LOG = Logger.getLogger(BaseWizard.class);

    protected WizardState initialState;

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
        if (!TextUtils.stringSet(state.getWizardStateName()))
        {
            throw new IllegalArgumentException();
        }
        states.put(state.getWizardStateName(), state);
    }

    public boolean isComplete()
    {
        return currentState instanceof WizardCompleteState;
    }

    /**
     * Override this to handle any final processing required when the wizard is
     * complete.
     *
     */
    public void process()
    {

    }

    /**
     * Override this to handle any processing required when this wizard is cancelled.
     *
     */
    public void cancel()
    {

    }

    public String traverseForward()
    {
        if (currentState == null)
        {
            currentState = initialState;
            currentState.initialise();
            return currentState.getWizardStateName();
        }

        validate(currentState);
        if (currentState.hasErrors())
        {
            // we can not progress to the next state until validation is successful.
            return currentState.getWizardStateName();
        }

        // execute current state
        currentState.execute();
        history.push(currentState);

        // get next state.
        String nextState = currentState.getNextState();
        if (nextState == null)
        {
            currentState.addActionError("Unknwon next state: " + nextState);
            return currentState.getWizardStateName();
        }

        currentState = getState(nextState);
        currentState.clearErrors();
        currentState.initialise();
        return currentState.getWizardStateName();
    }

    public String traverseBackward()
    {
        if (history.size() > 0)
        {
            currentState = history.pop();
        }
        return currentState.getWizardStateName();
    }

    private void validate(WizardState state)
    {
        //  first clear errors from any previous attempts at validation to ensure a
        // a clean validation check.
        state.clearErrors();
        assert !state.hasErrors();

        try
        {
            ActionValidatorManager.validate(state, state.getClass().getName());
            if (Validateable.class.isAssignableFrom(state.getClass()))
            {
                ((Validateable)state).validate();
            }
        }
        catch (ValidationException e)
        {
            state.addActionError("ValidationException: "+e.getMessage());
            LOG.severe(e);
        }
    }

}
