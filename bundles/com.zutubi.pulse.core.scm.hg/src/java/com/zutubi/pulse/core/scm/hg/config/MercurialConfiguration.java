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

package com.zutubi.pulse.core.scm.hg.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.hg.MercurialClient;
import com.zutubi.tove.annotations.*;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Required;

/**
 * Configures integration with the Mercurial (http://mercurial.selenic.com/) SCM.
 */
@SymbolicName("zutubi.mercurialConfig")
@Form(fieldOrder = {"repository", "branch", "inactivityTimeoutEnabled", "inactivityTimeoutSeconds", "monitor", "customPollingInterval", "pollingInterval", "includedPaths", "excludedPaths", "quietPeriodEnabled", "quietPeriod"})
public class MercurialConfiguration extends PollableScmConfiguration
{
    @Required
    private String repository;
    private String branch;
    @ControllingCheckbox(checkedFields = "inactivityTimeoutSeconds") @Wizard.Ignore
    private boolean inactivityTimeoutEnabled = false;
    @Wizard.Ignore
    private int inactivityTimeoutSeconds = 300;

    @Transient
    public String getType()
    {
        return MercurialClient.TYPE;
    }

    @Override
    public String getSummary()
    {
        String summary = repository;
        if (StringUtils.stringSet(branch))
        {
            summary += "[" + branch + "]";
        }

        return summary;
    }

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public boolean isInactivityTimeoutEnabled()
    {
        return inactivityTimeoutEnabled;
    }

    public void setInactivityTimeoutEnabled(boolean inactivityTimeoutEnabled)
    {
        this.inactivityTimeoutEnabled = inactivityTimeoutEnabled;
    }

    public int getInactivityTimeoutSeconds()
    {
        return inactivityTimeoutSeconds;
    }

    public void setInactivityTimeoutSeconds(int inactivityTimeoutSeconds)
    {
        this.inactivityTimeoutSeconds = inactivityTimeoutSeconds;
    }
}
