package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.util.List;

/**
 */
public class ProcessSupport
{
    public static void postProcess(List<ProcessArtifact> processes, File outputFileDir, File outputFile, CommandResult cmdResult, CommandContext context)
    {
        String path = FileSystemUtils.composeFilename(outputFileDir.getName(), outputFile.getName());
        StoredFileArtifact fileArtifact = new StoredFileArtifact(path, "text/plain");
        StoredArtifact artifact = new StoredArtifact(Command.OUTPUT_ARTIFACT_NAME, fileArtifact);
        for (ProcessArtifact p : processes)
        {
            p.getProcessor().process(fileArtifact, cmdResult, context);
        }
        cmdResult.addArtifact(artifact);
    }


}
