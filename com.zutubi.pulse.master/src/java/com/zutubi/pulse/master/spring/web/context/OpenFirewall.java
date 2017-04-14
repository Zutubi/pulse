/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
