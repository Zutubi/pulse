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

package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for the nant command.
 */
public class NAntCommandConfigurationExamples
{
    public ConfigurationExample getSimpleBuild()
    {
        NAntCommandConfiguration command = new NAntCommandConfiguration();
        command.setName("build");
        command.setTargets("build");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getCustomBuildFile()
    {
        NAntCommandConfiguration command = new NAntCommandConfiguration();
        command.setBuildFile("my.build");
        command.setName("test");
        command.setTargets("build test");
        return ExamplesBuilder.buildProject(command);
    }
}
