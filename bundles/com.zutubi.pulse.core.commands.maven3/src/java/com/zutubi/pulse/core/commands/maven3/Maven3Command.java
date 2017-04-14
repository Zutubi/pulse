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

package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.MavenUtils;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Support for running Maven 3 - adds automatic version capturing.
 */
public class Maven3Command extends NamedArgumentCommand
{
    private static final String DEFAULT_POM_FILE = "pom.xml";

    public Maven3Command(Maven3CommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Arrays.<Class<? extends PostProcessorConfiguration>>asList(Maven3PostProcessorConfiguration.class);
    }

    @Override
    public void execute(CommandContext commandContext)
    {
        super.execute(commandContext);

        try
        {
            //TODO: use the context's variables to transfer this maven specific information around.
            PulseExecutionContext pec = (PulseExecutionContext) commandContext.getExecutionContext();
            String version = MavenUtils.extractVersion(new File(getWorkingDir(pec.getWorkingDir()), getPomFile()), "version");
            if (version != null)
            {
                commandContext.addCustomField(FieldScope.BUILD, "maven.version", version);
            }
        }
        catch (PulseException e)
        {
            commandContext.addFeature(new Feature(Feature.Level.WARNING, e.getMessage()));
        }
    }

    private String getPomFile()
    {
        Maven3CommandConfiguration config = (Maven3CommandConfiguration) getConfig();
        String pomFile = config.getPomFile();
        if (!StringUtils.stringSet(pomFile))
        {
            pomFile = DEFAULT_POM_FILE;
        }
        return pomFile;
    }
}