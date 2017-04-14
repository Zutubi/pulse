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

import com.zutubi.pulse.core.commands.api.OutputProducingCommandConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * Configuration for instances of {@link PrintCommand}.
 */
@SymbolicName("zutubi.printCommandConfig")
@Form(fieldOrder = {"name", "message", "addNewline", "postProcessors", "force"})
public class PrintCommandConfiguration extends OutputProducingCommandConfigurationSupport
{
    /**
     * The message to print.
     */
    @Required
    private String message;
    /**
     * If true, add a new line after printing the message.
     */
    private boolean addNewline = true;

    public PrintCommandConfiguration()
    {
        super(PrintCommand.class);
    }

    public PrintCommandConfiguration(String name)
    {
        this();
        setName(name);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public boolean isAddNewline()
    {
        return addNewline;
    }

    public void setAddNewline(boolean addNewline)
    {
        this.addNewline = addNewline;
    }
}
