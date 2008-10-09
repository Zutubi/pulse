package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * Raised when the web UI hits an entity or record that does not exist.  This
 * is a situation that should never happen when using links in the UI (except
 * perhaps if there is a concurrent edit).  The handling is done in a single
 * place, the LookupErrorInterceptor, and the user will see the error
 * message.
 *
 * @see com.zutubi.pulse.master.xwork.interceptor.LookupErrorInterceptor
 */
public class LookupErrorException extends PulseRuntimeException
{
    public LookupErrorException(String errorMessage)
    {
        super(errorMessage);
    }
}
