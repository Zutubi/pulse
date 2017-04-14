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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Defines how build stages for a project are bootstrapped.
 */
@SymbolicName("zutubi.bootstrapConfig")
@Form(fieldOrder = {"checkoutType", "checkoutSubdir", "buildType", "persistentDirPattern", "tempDirPattern"})
public class BootstrapConfiguration extends AbstractConfiguration
{
    @Required
    private CheckoutType checkoutType = CheckoutType.CLEAN_CHECKOUT;
    private String checkoutSubdir;
    @Required
    private BuildType buildType = BuildType.CLEAN_BUILD;
    @Required
    private String persistentDirPattern = "$(agent.data.dir)/work/$(project.handle)/$(stage.handle)";
    @Required
    private String tempDirPattern = "$(agent.data.dir)/recipes/$(recipe.id)/base";

    public CheckoutType getCheckoutType()
    {
        return checkoutType;
    }

    public void setCheckoutType(CheckoutType checkoutType)
    {
        this.checkoutType = checkoutType;
    }

    public String getCheckoutSubdir()
    {
        return checkoutSubdir;
    }

    public void setCheckoutSubdir(String checkoutSubdir)
    {
        this.checkoutSubdir = checkoutSubdir;
    }

    public BuildType getBuildType()
    {
        return buildType;
    }

    public void setBuildType(BuildType buildType)
    {
        this.buildType = buildType;
    }

    public String getPersistentDirPattern()
    {
        return persistentDirPattern;
    }

    public void setPersistentDirPattern(String persistentDirPattern)
    {
        this.persistentDirPattern = persistentDirPattern;
    }

    public String getTempDirPattern()
    {
        return tempDirPattern;
    }

    public void setTempDirPattern(String tempDirPattern)
    {
        this.tempDirPattern = tempDirPattern;
    }
}
