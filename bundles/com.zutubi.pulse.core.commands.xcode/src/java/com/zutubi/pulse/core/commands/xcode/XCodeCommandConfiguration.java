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

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link XCodeCommand}.
 */
@SymbolicName("zutubi.xcodeCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "workspace", "scheme", "project", "target", "config", "destinations", "arch", "sdk", "buildaction", "settings", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class XCodeCommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String workspace;
    private String scheme;
    private String project;
    private String target;
    private String config;
    @StringList
    private List<String> destinations;
    private String arch;
    private String sdk;
    private String buildaction;
    @Wizard.Ignore @StringList
    private List<String> settings;

    public XCodeCommandConfiguration()
    {
        super(XCodeCommand.class, "xcode.bin", "xcodebuild");
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();
        if (StringUtils.stringSet(workspace))
        {
            result.add(new NamedArgument("workspace", workspace, "-workspace"));
        }

        if (StringUtils.stringSet(scheme))
        {
            result.add(new NamedArgument("scheme", scheme, "-scheme"));
        }

        if (StringUtils.stringSet(project))
        {
            result.add(new NamedArgument("project", project, "-project"));
        }

        if (StringUtils.stringSet(target))
        {
            result.add(new NamedArgument("target", target, "-target"));
        }

        if (StringUtils.stringSet(config))
        {
            result.add(new NamedArgument("configuration", config, "-configuration"));
        }

        if (destinations != null && destinations.size() > 0)
        {
            List<String> args = new ArrayList<String>();
            for (String destination: destinations)
            {
                args.add("-destination");
                args.add(destination);
            }

            result.add(new NamedArgument("destinations", StringUtils.unsplit(args), args));
        }

        if (StringUtils.stringSet(arch))
        {
            result.add(new NamedArgument("architecture", arch, "-arch"));
        }

        if (StringUtils.stringSet(sdk))
        {
            result.add(new NamedArgument("sdk", sdk, "-sdk"));
        }

        if (StringUtils.stringSet(buildaction))
        {
            result.add(new NamedArgument("build action", buildaction));
        }

        if (settings != null && settings.size() > 0)
        {
            result.add(new NamedArgument("settings", StringUtils.unsplit(settings), settings));
        }

        return result;
    }

    public String getWorkspace()
    {
        return workspace;
    }

    public void setWorkspace(String workspace)
    {
        this.workspace = workspace;
    }

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getConfig()
    {
        return config;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public List<String> getDestinations()
    {
        return destinations;
    }

    public void setDestinations(List<String> destinations)
    {
        this.destinations = destinations;
    }

    public String getArch()
    {
        return arch;
    }

    public void setArch(String arch)
    {
        this.arch = arch;
    }

    public String getSdk()
    {
        return sdk;
    }

    public void setSdk(String sdk)
    {
        this.sdk = sdk;
    }

    public String getBuildaction()
    {
        return buildaction;
    }

    public void setBuildaction(String buildaction)
    {
        this.buildaction = buildaction;
    }

    public List<String> getSettings()
    {
        return settings;
    }

    public void setSettings(List<String> settings)
    {
        this.settings = settings;
    }
}
