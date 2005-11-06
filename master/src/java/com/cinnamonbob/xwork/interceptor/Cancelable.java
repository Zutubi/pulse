package com.cinnamonbob.xwork.interceptor;

/**
 * 
 *
 */
public interface Cancelable
{
    boolean isCancelled();
    void setCancel(String name);
}
