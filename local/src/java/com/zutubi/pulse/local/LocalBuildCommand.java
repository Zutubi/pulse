package com.zutubi.pulse.local;

import com.zutubi.pulse.command.Command;

/**
 */
public class LocalBuildCommand implements Command
{
    private String argv[];

    public void parse(String... argv) throws Exception
    {
        this.argv = argv;
    }

    public int execute()
    {
        LocalBuild.main(argv);
        // LocalBuild will exit with an error if it detects one.
        return 0;
    }
}
