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

package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.NoopTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * The trigger configuration for dependent build triggers.
 */
@Form(fieldOrder = { "name", "propagateStatus", "propagateVersion", "revisionHandling" })
@SymbolicName("zutubi.dependentBuildTriggerConfig")
public class DependentBuildTriggerConfiguration extends TriggerConfiguration
{
    public enum RevisionHandling
    {
        FLOAT_INDEPENDENTLY,
        FIX_WITH_UPSTREAM,
        PROPAGATE_FROM_UPSTREAM,
    }

    /**
     * If true, build requests raised by this trigger will inherit the status
     * of the completed build.
     */
    private boolean propagateStatus;

    /**
     * If true, build requests raised by this trigger will inherit the version
     * of the completed build.
     */
    private boolean propagateVersion;

    /**
     * Determines how the revision is influenced by the upstream build.
     */
    @Required
    private RevisionHandling revisionHandling = RevisionHandling.FLOAT_INDEPENDENTLY;

    public Trigger newTrigger()
    {
        return new NoopTrigger(getName(), Trigger.DEFAULT_GROUP);
    }

    public boolean isPropagateStatus()
    {
        return propagateStatus;
    }

    public void setPropagateStatus(boolean propagateStatus)
    {
        this.propagateStatus = propagateStatus;
    }

    public boolean isPropagateVersion()
    {
        return propagateVersion;
    }

    public void setPropagateVersion(boolean propagateVersion)
    {
        this.propagateVersion = propagateVersion;
    }

    public RevisionHandling getRevisionHandling()
    {
        return revisionHandling;
    }

    public void setRevisionHandling(RevisionHandling revisionHandling)
    {
        this.revisionHandling = revisionHandling;
    }
}
