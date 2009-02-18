package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.tove.annotations.ControllingSelect;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * A transient configuration type used to provide richer type-selection in the
 * project wizard.
 */
@SymbolicName("zutubi.projectTypeSelectionConfig")
@Form(fieldOrder = { "primaryType", "commandType" })
public class ProjectTypeSelectionConfiguration extends AbstractConfiguration
{
    public static final String TYPE_CUSTOM      = "custom project";
    public static final String TYPE_MULTI_STEP  = "multi-step project";
    public static final String TYPE_SINGLE_STEP = "single-step project";
    public static final String TYPE_VERSIONED   = "versioned project";

    public static final Map<String, Class> TYPE_MAPPING = new HashMap<String, Class>();
    static
    {
        TYPE_MAPPING.put(TYPE_CUSTOM, CustomTypeConfiguration.class);
        TYPE_MAPPING.put(TYPE_MULTI_STEP, MultiRecipeTypeConfiguration.class);
        TYPE_MAPPING.put(TYPE_VERSIONED, VersionedTypeConfiguration.class);
    }

    @ControllingSelect(enableSet = {TYPE_SINGLE_STEP}, optionProvider = "ProjectTypeOptionProvider")
    private String primaryType = TYPE_SINGLE_STEP;
    @Select(optionProvider = "CommandTypeOptionProvider")
    private String commandType;

    public String getPrimaryType()
    {
        return primaryType;
    }

    public void setPrimaryType(String primaryType)
    {
        this.primaryType = primaryType;
    }

    public String getCommandType()
    {
        return commandType;
    }

    public void setCommandType(String commandType)
    {
        this.commandType = commandType;
    }
}
