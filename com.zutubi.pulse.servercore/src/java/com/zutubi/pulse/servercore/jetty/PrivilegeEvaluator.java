package com.zutubi.pulse.servercore.jetty;

import org.acegisecurity.Authentication;

/**
 * The privilege evaluator defines a set of rules that are used to determine
 * whether or not a specific authentication is allowed to continue with the
 * execution of the http request.
 */
public interface PrivilegeEvaluator
{
    /**
     * Determine whether or not to proceed with the specified http invocation.
     * @param invocation    the invocation instance providing the http request details.
     * @param auth          the authentication representing the 'user' requesting
     * the privilege to run the http request.
     * @return true if the request can proceed, false if a 403 forbidden error response
     * should generated.
     */
    boolean isAllowed(HttpInvocation invocation, Authentication auth);
}
