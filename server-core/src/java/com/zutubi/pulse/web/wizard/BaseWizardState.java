package com.zutubi.pulse.web.wizard;

import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.xwork.ValidationAware;

import java.util.Collection;
import java.util.Map;

/**
 * <class-comment/>
 */
public abstract class BaseWizardState implements WizardState, ValidationAware
{
    private static final Logger LOG = Logger.getLogger(BaseWizardState.class);

    private final String stateName;
    private Wizard wizard;

    public BaseWizardState(Wizard wizard, String name)
    {
        this.wizard = wizard;
        this.stateName = name;
    }

    public void initialise()
    {
        wizard.clearErrors();
    }

    public void execute()
    {

    }

    public void reset()
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

    public void setActionErrors(Collection errorMessages)
    {
        getWizard().setActionErrors(errorMessages);
    }

    public Collection getActionErrors()
    {
        return getWizard().getActionErrors();
    }

    public void setActionMessages(Collection messages)
    {
        getWizard().setActionMessages(messages);
    }

    public Collection getActionMessages()
    {
        return getWizard().getActionMessages();
    }

    public void setFieldErrors(Map errorMap)
    {
        getWizard().setFieldErrors(errorMap);
    }

    public Map getFieldErrors()
    {
        return getWizard().getFieldErrors();
    }

    public void addActionError(String anErrorMessage)
    {
        getWizard().addActionError(anErrorMessage);
    }

    public void addActionMessage(String aMessage)
    {
        getWizard().addActionMessage(aMessage);
    }

    public void addFieldError(String fieldName, String errorMessage)
    {
        getWizard().addFieldError(fieldName, errorMessage);
    }

    public boolean hasActionErrors()
    {
        return getWizard().hasActionErrors();
    }

    public boolean hasActionMessages()
    {
        return getWizard().hasActionMessages();
    }

    public boolean hasErrors()
    {
        return getWizard().hasErrors();
    }

    public boolean hasFieldErrors()
    {
        return getWizard().hasFieldErrors();
    }

}
