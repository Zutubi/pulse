package com.zutubi.pulse.master.spring.web.context;

import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An implementation of the HttpFirewall interface that is open.  That is,
 * does not change or reject any requests.
 */
public class OpenFirewall implements HttpFirewall
{
    public HttpServletResponse getFirewalledResponse(HttpServletResponse response)
    {
        return response;
    }

    public FirewalledRequest getFirewalledRequest(HttpServletRequest request) throws RequestRejectedException
    {
        return new WrappedRequest(request);
    }

    private static class WrappedRequest extends FirewalledRequest
    {
        private WrappedRequest(HttpServletRequest request)
        {
            super(request);
        }

        @Override
        public void reset()
        {

        }
    }
}
