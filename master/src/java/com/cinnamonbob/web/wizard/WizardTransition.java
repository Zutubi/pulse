package com.cinnamonbob.web.wizard;

/**
 * <class-comment/>
 */
public interface WizardTransition
{
    WizardState getSourceState();

    WizardState getTargetState();
}
