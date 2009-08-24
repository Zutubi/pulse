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
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }
}
