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

package com.zutubi.pulse.master.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * An extended details class that exists so that we can distinguish authentication
 * via HTTP basic via the type of the details object.  That is, if the type is
 * this class, we know the user was authentication via HTTP basic auth.
 */
public class BasicAuthenticationDetails extends WebAuthenticationDetails
{
    public BasicAuthenticationDetails(HttpServletRequest request)
    {
        super(request);
    }
}
