package com.zutubi.pulse.core.commands.core;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * A way to map command exit codes to specified result states.
 */
@SymbolicName("zutubi.statusMappingConfig")
public class StatusMappingConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    private int code;
    @Required
    private String status;

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Transient
    public ResultState getResultState()
    {
        for(ResultState state: ResultState.getCompletedStates())
        {
            if(state.getPrettyString().equalsIgnoreCase(status))
            {
                return state;
            }
        }

        return null;
    }

    public void validate(ValidationContext context)
    {
        if(TextUtils.stringSet(status) && getResultState() == null)
        {
            context.addFieldError("status", "unknown or incomplete build state '" + status + "'");
        }
    }
}
