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

import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * A transient type used to configure defaults for a new project.
 */
@SymbolicName("zutubi.projectDefaultsConfig")
@Form(fieldOrder = {"addScmTrigger", "addDependenciesTrigger", "addDefaultRecipe", "recipeName", "addDefaultStage", "stageName"})
public class ProjectDefaultsConfiguration extends AbstractConfiguration
{
    private boolean addScmTrigger = true;
    private boolean addDependenciesTrigger = true;
    @ControllingCheckbox(checkedFields = {"recipeName", "addDefaultStage"})
    private boolean addDefaultRecipe = true;
    private String recipeName = "default";
    @ControllingCheckbox(checkedFields = {"stageName"})
    private boolean addDefaultStage = true;
    private String stageName = "default";

    public boolean isAddScmTrigger()
    {
        return addScmTrigger;
    }

    public void setAddScmTrigger(boolean addScmTrigger)
    {
        this.addScmTrigger = addScmTrigger;
    }

    public boolean isAddDependenciesTrigger()
    {
        return addDependenciesTrigger;
    }

    public void setAddDependenciesTrigger(boolean addDependenciesTrigger)
    {
        this.addDependenciesTrigger = addDependenciesTrigger;
    }

    public boolean isAddDefaultRecipe()
    {
        return addDefaultRecipe;
    }

    public void setAddDefaultRecipe(boolean addDefaultRecipe)
    {
        this.addDefaultRecipe = addDefaultRecipe;
    }

    public String getRecipeName()
    {
        return recipeName;
    }

    public void setRecipeName(String recipeName)
    {
        this.recipeName = recipeName;
    }

    public boolean isAddDefaultStage()
    {
        return addDefaultStage;
    }

    public void setAddDefaultStage(boolean addDefaultStage)
    {
        this.addDefaultStage = addDefaultStage;
    }

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }
}
