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

package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

public class MavenProjectHelper extends ProjectConfigurationHelper
{
    protected static final String MAVEN2_PROCESSOR_NAME = "maven 2 output processor";

    public MavenProjectHelper(ProjectConfiguration config, ConfigurationHelper helper)
    {
        super(config, helper);
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        try
        {
            Maven2CommandConfiguration command = new Maven2CommandConfiguration();
            command.setPomFile("pom.xml");
            command.setGoals("clean deploy");
            command.addPostProcessor(helper.getPostProcessor(MAVEN2_PROCESSOR_NAME, Maven2PostProcessorConfiguration.class));
            return command;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
