package com.zutubi.pulse.master.xwork.interceptor;

/**
 * 
 *
 */
public interface Cancelable
{
    boolean isCancelled();
    void setCancel(String name);
    void doCancel();
}
