package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.ValidationAware;
import com.opensymphony.xwork.ValidationAwareSupport;
import com.cinnamonbob.util.logging.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * <class-comment/>
 */
public abstract class BaseWizardState implements WizardState
{
    private static final Logger LOG = Logger.getLogger(BaseWizardState.class);

    private final String stateName;
    private Wizard wizard;

    private final ValidationAware validationAware = new ValidationAwareSupport();

    public BaseWizardState(Wizard wizard, String name)
    {
        this.wizard = wizard;
        this.stateName = name;
    }

    public void initialise()
    {
        clearErrors();
    }

    public void execute()
    {

    }

    public final String getWizardStateName()
    {
        return stateName;
    }

    public void clearErrors()
    {
        validationAware.setActionErrors(null);
        validationAware.setFieldErrors(null);
        validationAware.setActionMessages(null);
    }

    public final Wizard getWizard()
    {
        return wizard;
    }

    // ---( Implement the ValidationAware interface. )---

    public void addActionError(String anErrorMessage)
    {
        LOG.severe("addActionError: " + anErrorMessage);
        validationAware.addActionError(anErrorMessage);
    }

    public void addActionMessage(String aMessage)
    {
        validationAware.addActionMessage(aMessage);
    }

    public void addFieldError(String fieldName, String errorMessage)
    {
        validationAware.addFieldError(fieldName, errorMessage);
    }

    public Collection getActionMessages()
    {
        return validationAware.getActionMessages();
    }

    public boolean hasActionErrors()
    {
        return validationAware.hasActionErrors();
    }

    public boolean hasActionMessages()
    {
        return validationAware.hasActionMessages();
    }

    public boolean hasFieldErrors()
    {
        return validationAware.hasFieldErrors();
    }

    public void setActionErrors(Collection errorMessages)
    {
        validationAware.setActionErrors(errorMessages);
    }

    public void setActionMessages(Collection messages)
    {
        validationAware.setActionMessages(messages);
    }

    public void setFieldErrors(Map errorMap)
    {
        validationAware.setFieldErrors(errorMap);
    }

    public Collection getActionErrors()
    {
        return validationAware.getActionErrors();
    }

    public Map getFieldErrors()
    {
        return validationAware.getFieldErrors();
    }

    public boolean hasErrors()
    {
        return validationAware.hasErrors();
    }
}
