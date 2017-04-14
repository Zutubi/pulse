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

            CustomFieldsPostProcessorConfiguration config = (CustomFieldsPostProcessorConfiguration) getConfig();
            for (Map.Entry<Object, Object> entry: properties.entrySet())
            {
                ppContext.addCustomField(config.getScope(), entry.getKey().toString(), entry.getValue().toString());
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