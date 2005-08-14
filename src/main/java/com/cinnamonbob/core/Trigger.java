package com.cinnamonbob.core;

/**
 * 
 *
 */
public interface Trigger
{
    void setSchedule(Schedule schedule);
    void trigger();
    void enable();
    void disable();
    boolean isEnabled();}
