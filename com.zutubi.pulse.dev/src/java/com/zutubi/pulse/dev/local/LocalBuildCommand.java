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

package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class LocalBuildCommand implements Command
{
    public int execute(BootContext context)
    {
        LocalBuild.main(context.getCommandArgv());
        // LocalBuild will exit with an error if it detects one.
        return 0;
    }

    public String getHelp()
    {
        return "execute a local build";
    }

    public String getDetailedHelp()
    {
        return "Executes a local build in the current working directory.  A local build reads\n" +
               "a local pulse file (pulse.xml by default) and executes a specified recipe in\n" +
               "your development source tree.  This is useful for reproducing pulse builds\n" +
               "exactly and for debugging pulse files when making changes.";
    }

    public List<String> getUsages()
    {
        return Arrays.asList("");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("lo");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-r [--recipe] recipe",       "set recipe to execute [default: the default recipe]");
        options.put("-p [--pulse-file] file",     "use specified pulse file [default: pulse.xml]");
        options.put("-o [--output-dir] dir",      "write output to directory [default: pulse.out]");
        options.put("-q [--require] name[/ver]", "require specified resource with optional version");
        options.put("-e [--resources-file] file", "use specified resources file [default: <none>]");
        options.put("-l [--failure-limit] limit", "limit number of test failures shown [default: 50]");
        options.put("-v [--verbose]", "show verbose output");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }
}
