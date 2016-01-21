package com.zutubi.pulse.master.rest.wizards;

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
