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

package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import java.util.*;

/**
 * Configuration for instances of {@link MsBuildCommand}.
 */
@SymbolicName("zutubi.msbuildCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "buildFile", "targets", "configuration", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class MsBuildCommandConfiguration extends NamedArgumentCommandConfiguration
{
    private static final String EXECUTABLE_PROPERTY = "msbuild.bin";
    private static final String DEFAULT_EXECUTABLE = "msbuild";

    private static final String FLAG_TARGET   = "/target:";
    private static final String FLAG_PROPERTY = "/property:";

    private static final String PROPERTY_SEPARATOR     = "=";
    private static final String PROPERTY_CONFIGURATION = "Configuration";

    @BrowseScmFileAction(baseDirField = "workingDir")
    private String buildFile;
    private String targets;
    private String configuration;
    @Addable("build-property")
    private Map<String, BuildPropertyConfiguration> buildProperties = new LinkedHashMap<String, BuildPropertyConfiguration>();

    public MsBuildCommandConfiguration()
    {
        super(MsBuildCommand.class, EXECUTABLE_PROPERTY, DEFAULT_EXECUTABLE);
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();

        // Add the build file first so it is easy to distinguish in the full
        // command line.
        if (StringUtils.stringSet(buildFile))
        {
            result.add(new NamedArgument("build file", buildFile));
        }

        if (StringUtils.stringSet(targets))
        {
            List<String> flaggedTargets = new LinkedList<String>();
            for (String target: targets.split("\\s+"))
            {
                flaggedTargets.add(FLAG_TARGET + target);
            }

            result.add(new NamedArgument("targets", targets, flaggedTargets));
        }

        // We support the configuration property explicitly as it is so common.
        if (StringUtils.stringSet(configuration))
        {
            result.add(new NamedArgument("configuration", configuration, Arrays.asList(FLAG_PROPERTY + PROPERTY_CONFIGURATION + PROPERTY_SEPARATOR + configuration)));
        }

        return result;
    }

    @Override
    public List<String> getCombinedArguments()
    {
        List<String> result = new LinkedList<String>(super.getCombinedArguments());

        for (BuildPropertyConfiguration property: buildProperties.values())
        {
            result.add(FLAG_PROPERTY + property.getName() + PROPERTY_SEPARATOR + property.getValue());
        }

        return result;
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public String getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(String configuration)
    {
        this.configuration = configuration;
    }

    public Map<String, BuildPropertyConfiguration> getBuildProperties()
    {
        return buildProperties;
    }

    public void setBuildProperties(Map<String, BuildPropertyConfiguration> buildProperties)
    {
        this.buildProperties = buildProperties;
    }

    public void addBuildProperty(BuildPropertyConfiguration property)
    {
        buildProperties.put(property.getName(), property);
    }
}
