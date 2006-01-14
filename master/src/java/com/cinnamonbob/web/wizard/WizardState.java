package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.ValidationAware;

/**
 * <class-comment/>
 */
public interface WizardState extends ValidationAware
{
    void execute();

    void initialise();

    String getWizardStateName();

    String getNextState();

    void clearErrors();
}
