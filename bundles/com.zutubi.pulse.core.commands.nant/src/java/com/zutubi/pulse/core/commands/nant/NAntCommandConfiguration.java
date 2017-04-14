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

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link NAntCommand}.
 * <pre>
 * Usage : NAnt [options] &lt;target> &lt;target> ...
 * Options :
 *
 *   -t[argetframework]:&lt;text&gt;      Specifies the framework to target
 *   -defaultframework:&lt;text&gt;       Specifies the framework to target (Short format: /k)
 *   -buildfile&lt;text&gt;               Use given buildfile (Short format: /f)
 *   -v[erbose][+|-]                Displays more information during build process
 *   -debug[+|-]                    Displays debug information during build proces
 *
 *   -q[uiet][+|-]                  Displays only error or warning messages during build process
 *   -e[macs][+|-]                  Produce logging information without adornments
 *   -find[+|-]                     Search parent directories for build file
 *   -indent:&lt;number&gt;               Indentation level of build output
 *   -D:&lt;name&gt;=&lt;value&gt;              Use value for given property
 *   -logger:&lt;text&gt;                 Use given type as logger
 *   -l[ogfile]:&lt;filename&gt;          Use value as name of log output file
 *   -listener:&lt;text&gt;               Add an instance of class as a project listener
 *   -ext[ension]:&lt;text&gt;            Load NAnt extensions from the specified assembly
 *   -projecthelp[+|-]              Prints project help information
 *   -nologo[+|-]                   Suppresses display of the logo banner
 *   -h[elp][+|-]                   Prints this message
 *   @&lt;file&gt;                        Insert command-line settings from a text file.
 * </pre>
 */
@SymbolicName("zutubi.nantCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "buildFile", "targetFramework", "targets", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class NAntCommandConfiguration extends NamedArgumentCommandConfiguration
{
    @BrowseScmFileAction(baseDirField = "workingDir")
    private String buildFile;
    private String targetFramework;
    private String targets;

    public NAntCommandConfiguration()
    {
        super(NAntCommand.class, "nant.bin", "nant");
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    public String getTargetFramework()
    {
        return targetFramework;
    }

    public void setTargetFramework(String targetFramework)
    {
        this.targetFramework = targetFramework;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> args = new LinkedList<NamedArgument>();
        if (StringUtils.stringSet(buildFile))
        {
            args.add(new NamedArgument("build file", buildFile, asList("-buildfile:" + buildFile)));
        }

        if (StringUtils.stringSet(targetFramework))
        {
            args.add(new NamedArgument("target framework", targetFramework, asList("-targetframework:" + targetFramework)));
        }

        if (StringUtils.stringSet(targets))
        {
            args.add(new NamedArgument("targets", targets, asList(targets.split("\\s+"))));
        }

        return args;
    }
}
