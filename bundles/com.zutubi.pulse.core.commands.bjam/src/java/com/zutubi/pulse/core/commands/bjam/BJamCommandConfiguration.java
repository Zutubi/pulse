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

package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for instances of {@link BJamCommand}.
 */
@SymbolicName("zutubi.bjamCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "jamfile", "targets", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class BJamCommandConfiguration extends NamedArgumentCommandConfiguration
{
    @BrowseScmFileAction(baseDirField = "workingDir")
    private String jamfile;
    private String targets;

    public BJamCommandConfiguration()
    {
        super(BJamCommand.class, "bjam.bin", "bjam");
    }

    public String getJamfile()
    {
        return jamfile;
    }

    public void setJamfile(String jamfile)
    {
        this.jamfile = jamfile;
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
        List<NamedArgument> result = new LinkedList<NamedArgument>();

        if (StringUtils.stringSet(jamfile))
        {
            result.add(new NamedArgument("jamfile", jamfile, "-f"));
        }

        if (StringUtils.stringSet(targets))
        {
            result.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }

        return result;
    }
}
