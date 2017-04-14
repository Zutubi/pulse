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
import com.zutubi.tove.security.ActorProvider;

/**
 * Adapts from Spring Security logged-in users to the config systems ActorProvider
 * interface.
 */
public class SpringSecurityActorProvider implements ActorProvider
{
    public Actor getActor()
    {
        return SecurityUtils.getLoggedInUser();
    }
}
