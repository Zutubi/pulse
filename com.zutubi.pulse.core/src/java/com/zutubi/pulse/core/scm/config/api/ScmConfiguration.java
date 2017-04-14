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

package com.zutubi.pulse.core.scm.config.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Base for SCM configuration types.  All SCM plugins must support at least
 * this configuration.
 */
@SymbolicName("zutubi.scmConfig")
public abstract class ScmConfiguration extends AbstractConfiguration
{
    private List<CommitterMappingConfiguration> committerMappings = new LinkedList<CommitterMappingConfiguration>();

    public List<CommitterMappingConfiguration> getCommitterMappings()
    {
        return committerMappings;
    }

    public void setCommitterMappings(List<CommitterMappingConfiguration> committerMappings)
    {
        this.committerMappings = committerMappings;
    }

    /**
     * Returns a short type string used to identify the SCM type (e.g.
     * "svn"). This type may be used by other parts of the system to
     * determine which SCM they are dealing with.  For example change viewers
     * may use different strategies to deal with different SCMs.
     *
     * @return the SCM type
     */
    @Transient
    public abstract String getType();

    /**
     * Returns a human-readable string used to summaries the SCM configuration. The string should
     * briefly allow a user to understand what source code is being referenced by the SCM.
     *
     * @return a human-readable summary of this configuration
     */
    @Transient
    public abstract String getSummary();
}
