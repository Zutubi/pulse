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

package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.logging.Logger;

/**
 * Helper base for configuration check handlers that test database connections.
 */
public abstract class AbstractDatabaseConfigurationCheckHandler<T extends Configuration>  extends AbstractConfigurationCheckHandler<T>
{
    private static final Logger LOG = Logger.getLogger(AbstractDatabaseConfigurationCheckHandler.class);
    private static final String MYSQL_INSANITY = "** BEGIN NESTED EXCEPTION **";

    protected void processException(Exception e) throws PulseException
    {
        LOG.warning(e);
        String message = e.getMessage();
        int i = message.indexOf(MYSQL_INSANITY);
        if(i >= 0)
        {
            message = message.substring(0, i).trim();
        }

        Throwable t = e;
        while ((t = t.getCause()) != null)
        {
            message += " Caused by: " + t.getMessage();
        }

        throw new PulseException(message, e);
    }
}
