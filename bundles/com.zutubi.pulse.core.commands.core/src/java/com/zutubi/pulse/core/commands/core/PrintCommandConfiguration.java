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
