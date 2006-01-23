package com.cinnamonbob.web;

import com.cinnamonbob.xwork.interceptor.Cancelable;

/**
 * 
 *
 */
public class ActionSupport extends com.opensymphony.xwork.ActionSupport implements Cancelable
{
    private String cancel;

    public boolean isCancelled()
    {
        return cancel != null;
    }

    public void setCancel(String name)
    {
        this.cancel = name;
    }
}
