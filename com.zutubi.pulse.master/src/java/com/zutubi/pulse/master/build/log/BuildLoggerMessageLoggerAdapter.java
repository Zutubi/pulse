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

package com.zutubi.pulse.master.build.log;

import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.PROGRESS_CHARACTER;
import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.toLevel;
import org.apache.ivy.util.AbstractMessageLogger;

import java.util.logging.Level;

/**
 * A message logger implementation that redirects ivy logging to a build logger.
 */
public class BuildLoggerMessageLoggerAdapter extends AbstractMessageLogger
{
    /**
     * The default level at which we are logging ivy messages to
     * the build logger.
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    private BuildLogger buildLogger;

    public BuildLoggerMessageLoggerAdapter(BuildLogger buildLogger)
    {
        this.buildLogger = buildLogger;
    }

    protected void doProgress()
    {
        buildLogger.log(PROGRESS_CHARACTER);
    }

    protected void doEndProgress(String msg)
    {
        buildLogger.status(msg);
    }

    public void rawlog(String msg, int level)
    {
        log(msg, level);
    }

    public void log(String msg, int level)
    {
        if (isLoggable(toLevel(level)))
        {
            buildLogger.status(msg);
        }
    }

    private boolean isLoggable(Level level)
    {
        if (level.intValue() < DEFAULT_LOG_LEVEL.intValue() || DEFAULT_LOG_LEVEL == Level.OFF)
        {
            return false;
        }
        return true;
    }

}
