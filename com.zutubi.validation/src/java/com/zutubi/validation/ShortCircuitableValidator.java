package com.zutubi.validation;

/**
 * Interface implemented by a validator that can be bypassed if the validation
 * processing that has occured prior to has indicated a validation error.
 */
public interface ShortCircuitableValidator
{
    void setShortCircuit(boolean b);

    /**
     * This validator will not be triggered if the isShortCircuit returns true
     * and the validation context indicates that a validation error has already
     * been registered.
     *
     * @return true to bypass this validator, false otherwise.
     */
    boolean isShortCircuit();
}
