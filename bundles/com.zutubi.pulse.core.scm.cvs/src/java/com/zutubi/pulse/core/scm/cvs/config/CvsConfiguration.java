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

package com.zutubi.pulse.core.scm.cvs.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.cvs.validation.annotation.CvsRoot;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Text;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@Form(fieldOrder = {"root", "password", "module", "branch", "monitor", "customPollingInterval", "pollingInterval", "quietPeriodEnabled", "quietPeriod", "includedPaths", "excludedPaths"})
@SymbolicName("zutubi.cvsConfig")
public class CvsConfiguration extends PollableScmConfiguration
{
    @Required @CvsRoot
    @Text
    private String root;
    @Required
    private String module;
    
    @Password
    private String password;
    private String branch;

    public CvsConfiguration()
    {
        setQuietPeriodEnabled(true);
        setQuietPeriod(5);
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getModule()
    {
        return module;
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public String getType()
    {
        return "cvs";
    }

    @Override
    public String getSummary()
    {
        String summary = root + " " + module;
        if (StringUtils.stringSet(branch))
        {
            summary += " (branch: " + branch + ")";
        }

        return summary;
    }
}
