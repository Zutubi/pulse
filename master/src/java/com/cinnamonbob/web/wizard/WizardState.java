package com.cinnamonbob.web.wizard;

import java.util.Map;
import java.util.Collection;

/**
 * <class-comment/>
 */
public interface WizardState
{
    void validate();
    void execute();

    void initialise();

    String getStateName();

    String getNextState();

    boolean hasErrors();

    Map<String, String> getFieldErrors();

    Collection getActionErrors();

    void clearErrors();
}
