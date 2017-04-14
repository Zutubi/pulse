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

package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;

import java.io.File;

/**
 * <p>
 * A default implementation of {@link PostProcessor} which hides some
 * implementation details and provides utility methods for common cases.
 * This is still quite a low-level implementation, consider also more
 * targeted base classes.
 * </p>
 * <p>
 * This implementation handles standard fail on error and warning
 * capabilities.
 * </p>
 *
 * @see TextFilePostProcessorSupport
 * @see LineBasedPostProcessorSupport
 */
public abstract class OutputPostProcessorSupport extends PostProcessorSupport
{
    protected OutputPostProcessorSupport(OutputPostProcessorConfigurationSupport config)
    {
        super(config);
    }

    public OutputPostProcessorConfigurationSupport getConfig()
    {
        return (OutputPostProcessorConfigurationSupport) super.getConfig();
    }

    public final void process(File artifactFile, final PostProcessorContext ppContext)
    {
        processFile(artifactFile, new PostProcessorContext()
        {
            public ExecutionContext getExecutionContext()
            {
                return ppContext.getExecutionContext();
            }

            public ResultState getResultState()
            {
                return ppContext.getResultState();
            }

            public void addTests(TestSuiteResult suite, NameConflictResolution conflictResolution)
            {
                ppContext.addTests(suite, conflictResolution);
            }

            public void addFeature(Feature feature)
            {
                switch(feature.getLevel())
                {
                    case ERROR:
                        if (getConfig().isFailOnError())
                        {
                            failCommand("Error features detected");
                        }
                        break;
                    case WARNING:
                        if (getConfig().isFailOnWarning())
                        {
                            failCommand("Warning features detected");
                        }
                        break;
                }

                ppContext.addFeature(feature);
            }

            public void failCommand(String message)
            {
                ppContext.failCommand(message);
            }

            public void errorCommand(String message)
            {
                ppContext.errorCommand(message);
            }

            public void addFeatureToCommand(Feature feature)
            {
                ppContext.addFeatureToCommand(feature);
            }

            public void addCustomField(FieldScope scope, String name, String value)
            {
                ppContext.addCustomField(scope, name, value);
            }
        });
    }

    protected abstract void processFile(File artifactFile, PostProcessorContext ppContext);
}
