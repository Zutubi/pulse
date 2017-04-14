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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * A startup task which logs the main thread in as the system user, allowing
 * it to call secured methods.
 */
public class LoginAsSystemStartupTask implements StartupTask
{
    public void execute()
    {
        SecurityUtils.loginAsSystem();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
