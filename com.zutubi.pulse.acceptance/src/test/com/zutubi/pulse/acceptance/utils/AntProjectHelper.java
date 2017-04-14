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

import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.ant.AntPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;

public class AntProjectHelper extends ProjectConfigurationHelper
{
    protected static final String ANT_PROCESSOR_NAME = "ant output processor";

    protected AntProjectHelper(ProjectConfiguration config, ConfigurationHelper helper)
    {
        super(config, helper);
    }

    public CommandConfiguration createDefaultCommand()
    {
        try
        {
            // might be better to put this in a 'addCommand' type method?
            AntCommandConfiguration command = new AntCommandConfiguration();
            command.setBuildFile("build.xml");
            command.setName(ProjectConfigurationWizard.DEFAULT_COMMAND);
            command.addPostProcessor(helper.getPostProcessor(ANT_PROCESSOR_NAME, AntPostProcessorConfiguration.class));
            return command;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the default command target.
     *
     * @param target    the ant build target.
     */
    public void setTarget(String target)
    {
        AntCommandConfiguration command = (AntCommandConfiguration) getDefaultCommand();
        command.setTargets(target);
    }
}
