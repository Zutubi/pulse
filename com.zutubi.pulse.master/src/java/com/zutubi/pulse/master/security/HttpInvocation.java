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

import com.zutubi.tove.security.Actor;

import javax.servlet.http.HttpServletRequest;

/**
 * A value holder that contains all the available details for a http request/response - invocation.
 */
public class HttpInvocation
{
    private final HttpServletRequest httpRequest;
    private Actor actor;

    public HttpInvocation(HttpServletRequest httpRequest, Actor actor)
    {
        this.httpRequest = httpRequest;
        this.actor = actor;
    }

    public String getMethod()
    {
        return httpRequest.getMethod();
    }

    public String getPath()
    {
        return httpRequest.getRequestURI();
    }

    public Actor getActor()
    {
        return actor;
    }
}
