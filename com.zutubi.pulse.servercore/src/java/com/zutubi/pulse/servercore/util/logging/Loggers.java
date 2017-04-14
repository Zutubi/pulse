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

package com.zutubi.pulse.servercore.util.logging;

import com.zutubi.util.logging.Logger;

/**
 */
public class Loggers
{
    private static final String NAME_EVENTS = "com.zutubi.pulse.master.events";

    private static Logger eventLogger;

    /**
     * The Pulse event logger.  All logging sent to this logger will end up in the
     * event log file when event logging is active.  By default, this includes all
     * events.
     *
     * @return the logger for the event log file.
     */
    public static synchronized Logger getEventLogger()
    {
        if (eventLogger == null)
        {
            // The logger for the com.zutubi.pulse.master.events package.  Note, that
            // to ensure the events end up in the events log file, the event handler
            // needs to be bound to the same package.
            eventLogger = Logger.getLogger(NAME_EVENTS);

            // events should only go to the event log.
            eventLogger.setUseParentHandlers(false);
        }
        return eventLogger;
    }
}
