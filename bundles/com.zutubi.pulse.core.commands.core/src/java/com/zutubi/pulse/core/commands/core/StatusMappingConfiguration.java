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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.tove.annotations.Dropdown;
import com.zutubi.tove.annotations.Form;
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
    @Required @Dropdown(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
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
