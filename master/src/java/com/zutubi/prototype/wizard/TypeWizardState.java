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
}
