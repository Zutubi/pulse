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

package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

import java.util.Arrays;

/**
 * Example configurations for the xcode command.
 */
public class XCodeCommandConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        XCodeCommandConfiguration command = new XCodeCommandConfiguration();
        command.setName("build");
        command.setTarget("MyApp");
        command.setBuildaction("clean build");
        return ExamplesBuilder.buildProject(command);
    }

    public ConfigurationExample getWorkspace()
    {
        XCodeCommandConfiguration command = new XCodeCommandConfiguration();
        command.setName("ios tests");
        command.setWorkspace("MyWorkspace.xcworkspace");
        command.setScheme("MyProjectTests");
        command.setDestinations(Arrays.asList("platform=iOS Simulator,name=iPhone 5s", "platform=iOS,name=My iPad"));
        command.setBuildaction("test");
        return ExamplesBuilder.buildProject(command);
    }
}