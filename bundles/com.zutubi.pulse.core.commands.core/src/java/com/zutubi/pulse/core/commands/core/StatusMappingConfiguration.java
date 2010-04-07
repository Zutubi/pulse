package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * A way to map command exit codes to specified result states.
 */
@SymbolicName("zutubi.statusMappingConfig")
@Form(fieldOrder = {"code", "status"})
@Table(columns = {"code", "status"})
public class StatusMappingConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    private int code;
    @Required @Select(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
    private ResultState status;

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public ResultState getStatus()
    {
        return status;
    }

    public void setStatus(ResultState status)
    {
        this.status = status;
    }

    public void validate(ValidationContext context)
    {
        if (!status.isCompleted())
        {
            context.addFieldError("status", "incomplete build state '" + status + "'");
        }
    }
}
