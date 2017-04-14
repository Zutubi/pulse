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

package com.zutubi.pulse.master.bootstrap.velocity;

import com.zutubi.util.logging.Logger;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Implementation of the velocity log system interface that maps velocity log events
 * to the Pulse logging system.
 */
public class CustomLogSystem implements LogSystem
{
    private static final Logger LOG = Logger.getLogger(CustomLogSystem.class);

    private boolean enabled = false;

    public void init(RuntimeServices runtimeServices) throws Exception
    {
        // Do nothing
    }

    public void logVelocityMessage(int level, String message)
    {
        // Ignore it all because Velocity's loggin is b0rked!
        if (enabled)
        {
            switch (level)
            {
                case DEBUG_ID:
                    LOG.debug(message);
                    break;
                case INFO_ID:
                    LOG.info(message);
                    break;
                case WARN_ID:
                    LOG.warning(message);
                    break;
                case ERROR_ID:
                    LOG.error(message);
                    break;
                default:
                    LOG.fine(message);
                    break;
            }
        }
    }
}
