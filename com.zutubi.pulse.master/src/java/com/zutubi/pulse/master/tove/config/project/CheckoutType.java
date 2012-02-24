package com.zutubi.pulse.master.tove.config.project;

/**
 * Defines where/how the checkout from an SCM is done at the start of a build stage.
 */
public enum CheckoutType
{
    /**
     * Nothing is checked out.
     */
    NO_CHECKOUT,
    /**
     * A clean checkout is performed to the temporary recipe directory.
     */
    CLEAN_CHECKOUT,
    /**
     * An incremental checkout is performed to the persistent project work directory.
     */
    INCREMENTAL_CHECKOUT,
}
