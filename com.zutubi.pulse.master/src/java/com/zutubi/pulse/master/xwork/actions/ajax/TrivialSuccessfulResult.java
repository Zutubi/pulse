package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * A specialised result to use to indicate success with no details.
 */
public class TrivialSuccessfulResult extends SimpleResult
{
    public TrivialSuccessfulResult()
    {
        super(true, null);
    }
}
