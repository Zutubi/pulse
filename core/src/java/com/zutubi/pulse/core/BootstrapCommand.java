package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * An adaptation between the command and Bootstrap interfaces that allows
 * any bootstrapper to be run like a command.  This further allows the result
 * of bootstrapping to be stored as part of the recipe result.
 *
 */
public class BootstrapCommand implements Command
{
    public static final String OUTPUT_NAME = "bootstrap output";
    public static final String FILES_FILE = "files.txt";

    private Bootstrapper bootstrapper;

    public BootstrapCommand(Bootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
    }

    public void execute(CommandContext context, CommandResult result)
    {
        bootstrapper.bootstrap(context);

        File artifactDir = new File(context.getOutputDir(), OUTPUT_NAME);
        if(artifactDir.isDirectory())
        {
            String[] files = artifactDir.list();
            if(files.length > 0)
            {
                StoredArtifact artifact = new StoredArtifact(OUTPUT_NAME);
                for(String file: files)
                {
                    artifact.add(new StoredFileArtifact(FileSystemUtils.composeFilename(OUTPUT_NAME, file)));
                }
                result.addArtifact(artifact);
            }
        }
    }

    public List<String> getArtifactNames()
    {
        return Arrays.asList(new String[]{OUTPUT_NAME});
    }

    public String getName()
    {
        return "bootstrap";
    }

    public void setName(String name)
    {
        // Ignored
    }

    public void terminate()
    {
        // Ignored (at present we have no way to cancel scm operations).
    }
}
