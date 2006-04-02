package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.model.StoredFileArtifact;

import java.io.File;
import java.util.regex.Pattern;

/**
 * A post processor that looks for error messages from GNU-compatible make
 * programs.
 */
public class MakePostProcessor extends ReferenceSupport implements PostProcessor
{
    private RegexPostProcessor regex;

    public MakePostProcessor()
    {
        regex = new RegexPostProcessor();
        RegexPattern pattern = regex.createPattern();
        pattern.setCategory(Feature.Level.ERROR);
        pattern.setPattern(Pattern.compile("^make(\\[[0-9]+\\])?: \\*\\*\\*"));
        regex.setFailOnError(false);
        regex.setLeadingContext(3);
        regex.setTrailingContext(3);
    }

    public void process(File outputDir, StoredFileArtifact artifact, CommandResult result)
    {
        regex.process(outputDir, artifact, result);
    }

    public boolean getFailOnError()
    {
        return regex.getFailOnError();
    }

    public void setFailOnError(boolean fail)
    {
        regex.setFailOnError(fail);
    }

    public int getLeadingContext()
    {
        return regex.getLeadingContext();
    }

    public void setLeadingContext(int context)
    {
        regex.setLeadingContext(context);
    }

    public int getTrailingContext()
    {
        return regex.getTrailingContext();
    }

    public void setTrailingContext(int context)
    {
        regex.setTrailingContext(context);
    }

}
