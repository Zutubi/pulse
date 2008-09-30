package com.zutubi.tove.wizard;

import com.zutubi.tove.type.CompositeType;

/**
 * Interface for all states of an {@link com.zutubi.tove.wizard.webwork.AbstractTypeWizard}.
 */
public interface TypeWizardState extends WizardState
{
    /**
     * Returns the base type this state is configuring.  When this state is
     * not configuring a type (e.g. the select state which merely chooses a
     * type for a following state), this method return null.  Otherwise, it
     * returns the base type being configured (may be different from the
     * actual type being configured e.g. when the base type is extendable).
     *
     * @see #getType()
     *
     * @return the base type being configured with this state, or null if the
     *         state does not configure a type
     */
    CompositeType getConfiguredBaseType();

    /**
     * Returns the type associated with this state.  For example, a base type
     * for a type selection state, or the actual type being configured for a
     * type state.  The state will not always produce a record of this type,
     * e.g. select states.
     *
     * @return the type associated with this state
     */
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

    /**
     * Indicates if a configuration check form should be displayed for this
     * state.
     *
     * @return true if a configuration check is available for this state
     */
    boolean hasConfigurationCheck();
}
