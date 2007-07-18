package com.zutubi.prototype.wizard;

import com.zutubi.prototype.type.CompositeType;

/**
 *
 *
 */
public interface TypeWizardState extends WizardState
{
    CompositeType getConfiguredBaseType();

    CompositeType getType();

    TypeWizardState getNextState();

    /**
     * Indicates if this state has fields to configured.  If not, the state
     * should not be displayed to the user, it should be skipped over (which
     * can result in the wizard finishing).
     * 
     * @return true iff this state has fields to be configured.
     */
    boolean hasFields();
}
