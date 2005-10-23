package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface Trigger
{
    long getId();
    void trigger();
    void enable();
    void disable();
    boolean isEnabled();
    String getType();
    String getSummary();
}
