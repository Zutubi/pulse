package com.cinnamonbob.web.wizard;

/**
 * <class-comment/>
 */
public interface Wizard
{
    WizardState getCurrentState();

    void setCurrentState(WizardState next);

    WizardState getState(String state);

    boolean isComplete();

    void process();
}
