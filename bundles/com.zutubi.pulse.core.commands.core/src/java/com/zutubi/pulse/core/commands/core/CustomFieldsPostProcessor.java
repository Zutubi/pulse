package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorSupport;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;


/**
 * A post processor that does line-by-line searching with regular expressions
 * to detect features.
 */
public class CustomFieldsPostProcessor extends PostProcessorSupport
{
    public CustomFieldsPostProcessor(CustomFieldsPostProcessorConfiguration config)
    {
        super(config);
    }

    public void process(File artifactFile, PostProcessorContext ppContext)
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(artifactFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            for (Map.Entry<Object, Object> entry: properties.entrySet())
            {
                ppContext.addCustomField(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            IOUtils.close(inputStream);
        }
    }
}