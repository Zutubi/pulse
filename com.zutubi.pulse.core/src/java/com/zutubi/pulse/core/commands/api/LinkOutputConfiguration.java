package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a capture of a link to an external system.
 *
 * @see LinkOutput
 */
@SymbolicName("zutubi.linkOutputConfig")
@Form(fieldOrder = {"name", "url"})
public class LinkOutputConfiguration extends OutputConfigurationSupport
{
    @Required
    private String url;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Class<? extends Output> outputType()
    {
        return LinkOutput.class;
    }
}
