package com.cinnamonbob.web.wizard;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public abstract class BaseWizardState implements WizardState
{
    private final String stateName;
    private Wizard wizard;

    private Map<String, String> fieldErrors = new HashMap<String, String>();
    private List<String> actionErrors = new LinkedList<String>();

    public BaseWizardState(Wizard wizard, String name)
    {
        this.wizard = wizard;
        this.stateName = name;
    }

    public void initialise()
    {

    }

    public void validate()
    {

    }

    public void execute()
    {

    }

    public final String getStateName()
    {
        return stateName;
    }

    public final Wizard getWizard()
    {
        return wizard;
    }

    public final boolean hasErrors()
    {
        return getActionErrors().size() > 0 || getFieldErrors().size() > 0;
    }

    public final List<String> getActionErrors()
    {
        return actionErrors;
    }

    public final void clearErrors()
    {
        getFieldErrors().clear();
        getActionErrors().clear();
    }

    public final Map<String, String> getFieldErrors()
    {
        return fieldErrors;
    }

    public final void addFieldError(String field, String message)
    {
        getFieldErrors().put(field, message);
    }

    public final void addActionError(String message)
    {
        getActionErrors().add(message);
    }
}
