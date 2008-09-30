package com.zutubi.pulse.xwork.interceptor;

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
