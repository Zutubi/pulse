package com.cinnamonbob.web.wizard;

import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.validator.DefaultActionValidatorManager;
import com.opensymphony.xwork.validator.ValidationException;

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
    private DefaultActionValidatorManager validationManager = new DefaultActionValidatorManager();
    ;

    public BaseWizard()
    {

    }

    public WizardState getCurrentState()
    {
        return currentState;
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
        return currentState instanceof WizardCompleteState;
    }

    /**
     * Override this to handle any final processing required when the wizard is completed.
     */
    public void process()
    {

    }

    /**
     * Override this to handle any processing required when this wizard is cancelled.
     */
    public void cancel()
    {

    }

    public void initialise()
    {
        currentState = initialState;
        currentState.initialise();
    }

    public boolean goTo(String requestedState)
    {
        if (getState(requestedState) == null)
        {
            return false;
        }
        if (currentState.getStateName().equals(requestedState))
        {
            return true;
        }

        while (history.size() > 0)
        {
            currentState = history.pop();

            if (currentState.getStateName().equals(requestedState))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Take a step forward.
     *
     * @return the state name for the state to be displayed next.
     */
    public String traverseForward()
    {

        validate(currentState);
        if (currentState.hasErrors())
        {
            // we can not progress to the next state until validation is successful.
            return currentState.getStateName();
        }

        // execute current state
        currentState.execute();
        history.push(currentState);

        // get next state.
        String nextState = currentState.getNextStateName();
        if (nextState == null)
        {
            currentState.addActionError("Unknwon next state: " + nextState);
            return currentState.getStateName();
        }

        currentState = getState(nextState);
        currentState.clearErrors();
        currentState.initialise();
        return currentState.getStateName();
    }

    /**
     * Take a step backwards to the previous wizard state.
     *
     * @return the state name for the next state to be displayed.
     */
    public String traverseBackward()
    {
        if (history.size() > 0)
        {
            currentState = history.pop();
        }
        return currentState.getStateName();
    }

    /**
     * Restart this wizard.
     */
    public String restart()
    {
        currentState = initialState;
        return currentState.getStateName();
    }

    /**
     * Validate the specified wizard state, using Xworks validation framework.
     *
     * @param state
     */
    private void validate(WizardState state)
    {
        //  states persist between requests, so first clear errors from any previous attempts
        // at validation to ensure a clean validation check.
        state.clearErrors();
        assert !state.hasErrors();

        try
        {
            validationManager.validate(state, state.getClass().getName());
            if (Validateable.class.isAssignableFrom(state.getClass()))
            {
                ((Validateable) state).validate();
            }
        }
        catch (ValidationException e)
        {
            state.addActionError("ValidationException: " + e.getMessage());
            LOG.error(e);
        }
    }

}
