package com.cinnamonbob.model;

/**
 * 
 *
 */
public interface Trigger
{
    long getId();
    void setSchedule(Schedule schedule);
    void trigger();
    void enable();
    void disable();
    boolean isEnabled();
    String getType();
    String getSummary();
}
