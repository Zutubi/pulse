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

package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.annotations.Internal;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A build hook that is triggered automatically at some point in a build.
 */
@SymbolicName("zutubi.autoBuildHookConfig")
public abstract class AutoBuildHookConfiguration extends AbstractBuildHookConfiguration
{
    @Internal
    private boolean enabled = true;
    private boolean runForPersonal = false;
    private boolean allowManualTrigger = true;
    private boolean failOnError = false;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public boolean isRunForPersonal()
    {
        return runForPersonal;
    }

    public void setRunForPersonal(boolean runForPersonal)
    {
        this.runForPersonal = runForPersonal;
    }

    public boolean isAllowManualTrigger()
    {
        return allowManualTrigger;
    }

    public void setAllowManualTrigger(boolean allowManualTrigger)
    {
        this.allowManualTrigger = allowManualTrigger;
    }

    public boolean failOnError()
    {
        return isFailOnError();
    }

    public boolean enabled()
    {
        return isEnabled();
    }

    @Override
    public boolean canManuallyTriggerFor(BuildResult result)
    {
        return allowManualTrigger && (result == null || !result.isPersonal() || runForPersonal);
    }

    protected boolean triggeredByBuildType(BuildResult buildResult)
    {
        return !buildResult.isPersonal() || isRunForPersonal();
    }
}
