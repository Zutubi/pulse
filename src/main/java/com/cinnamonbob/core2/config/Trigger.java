package com.cinnamonbob.core2.config;

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
