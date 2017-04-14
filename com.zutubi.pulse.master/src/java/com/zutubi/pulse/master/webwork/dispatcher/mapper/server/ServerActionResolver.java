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

package com.zutubi.pulse.master.webwork.dispatcher.mapper.server;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 * Resolver for the root of the server/ namespace.
 */
public class ServerActionResolver extends StaticMapActionResolver
{
    public ServerActionResolver()
    {
        super("serverActivity");
        addMapping("activity", new ParameterisedActionResolver("serverActivity"));
        addMapping("history", new PagedActionResolver("serverHistory"));
        addMapping("messages", new PagedActionResolver("serverMessages"));
        addMapping("info", new ParameterisedActionResolver("serverInfo"));
    }
}
