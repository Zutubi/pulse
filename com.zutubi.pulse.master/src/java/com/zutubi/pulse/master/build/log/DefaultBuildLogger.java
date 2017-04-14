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

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.dependency.ivy.IvyMessageOutputStreamAdapter;
import static com.zutubi.pulse.core.dependency.ivy.IvyUtils.toLevel;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.MessageLogger;

public class DefaultBuildLogger extends AbstractFileLogger implements BuildLogger
{
    private static final String PRE_MARKER = "============================[ task output below ]============================";
    private static final String POST_MARKER = "============================[ task output above ]============================";

    private int hookCount = 0;

    public DefaultBuildLogger(LogFile logFile)
    {
        super(logFile);
    }

    public void preamble(BuildResult build)
    {
        if (build.isPersonal())
        {
            logMarker(build.getOwnerName() + " personal build " + build.getNumber());
            logMarker("Project " + build.getProject().getName());
        }
        else
        {
            logMarker("Project '" + build.getProject().getName() + "' build " + build.getNumber() + ".");
        }

        Version version = Version.getVersion();
        logMarker("Pulse version " + version.getVersionNumber() + " (#" + version.getBuildNumber() + ").");
    }

    public void preBuild()
    {
        hookCount = 0;
        logMarker("Running pre build hooks...");
    }

    public void preBuildCompleted()
    {
        logMarker(String.format("Pre build hooks complete (%d hook%s run).", hookCount, hookCount == 1 ? "" : "s"));
    }

    public void hookCommenced(String name)
    {
        hookCount++;
        logMarker("Hook '" + name + "' commenced");
        if (writer != null)
        {
            writer.println(PRE_MARKER);
            writer.flush();
        }
    }

    public void hookCompleted(String name)
    {
        if (writer != null)
        {
            completeOutput();
            writer.println(POST_MARKER);
            writer.flush();
        }
        logMarker("Hook '" + name + "' completed");
    }

    public void commenced(BuildResult build)
    {
        logMarker("Build commenced", build.getStamps().getStartTime());
    }

    public void status(String message)
    {
        logMarker(message);
    }

    public void completed(BuildResult build)
    {
        logMarker("Build completed with status " + build.getState().getPrettyString(), build.getStamps().getEndTime());
    }

    public void postBuild()
    {
        hookCount = 0;
        logMarker("Running post build hooks...");
    }

    public void postBuildCompleted()
    {
        logMarker(String.format("Post build hooks complete (%d hook%s run).", hookCount, hookCount == 1 ? "" : "s"));
    }

    public void preIvyResolve()
    {
        logMarker("Resolving dependencies...");
    }

    public void postIvyResolve(String... errors)
    {
        if (errors.length == 0)
        {
            logMarker("Dependencies resolved.");
        }
        else
        {
            logMarker("Dependency resolution completed with errors:");
            logErrors(errors);
        }
    }

    public void preIvyPublish()
    {
        logMarker("Publishing to internal repository...");
    }

    public void postIvyPublish(String... errors)
    {
        if (errors.length == 0)
        {
            logMarker("Publish to internal repository complete.");
        }
        else
        {
            logMarker("Publish to internal repository completed with errors:");
            logErrors(errors);
        }
    }

    private void logErrors(String... errors)
    {
        for (String error : errors)
        {
            logMarker("    - " + error);
        }
    }

    /**
     * Get an adapter to allow this build logger to receive messages ment for an ivy
     * message logger.
     *
     * @return a message logger instance that delegates logging to this build logger instance.
     */
    public MessageLogger getMessageLogger()
    {
        return new AbstractMessageLogger()
        {
            private final Logger LOG = Logger.getLogger(IvyMessageOutputStreamAdapter.class);

            protected void doProgress()
            {
                // noop.
            }

            protected void doEndProgress(String msg)
            {
                // noop.
            }

            public void rawlog(String msg, int level)
            {
                log(msg, level);
            }

            public void log(String msg, int level)
            {
                if (isLoggable(level))
                {
                    status(msg);
                }
            }

            private boolean isLoggable(int level)
            {
                // delegate the checks for isLoggable to teh IvyMessageOutputStreamAdapter, so
                // that the configuration of the ivy message log level can be controlled in one location.
                return LOG.isLoggable(toLevel(level));
            }
        };
    }
}
