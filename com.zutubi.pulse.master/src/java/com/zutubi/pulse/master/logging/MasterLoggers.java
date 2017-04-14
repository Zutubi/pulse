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

package com.zutubi.pulse.master.logging;

import com.zutubi.util.logging.Logger;

/**
 * Provides access to master-specific loggers that are configurable using
 * system settings.
 */
public class MasterLoggers
{
    private static final String NAME_CONFIG_AUDIT = "com.zutubi.pulse.master.config.audit";

    private static Logger configAuditLogger;

    /**
     * Returns the logger used to write config audit logs when they are
     * enabled.
     *
     * @return the config audit logger
     */
    public static synchronized Logger getConfigAuditLogger()
    {
        if (configAuditLogger == null)
        {
            configAuditLogger = Logger.getLogger(NAME_CONFIG_AUDIT);
            configAuditLogger.setUseParentHandlers(false);
        }

        return configAuditLogger;
    }
}
