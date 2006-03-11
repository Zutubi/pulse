package com.cinnamonbob.core;

import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**
 * 
 *
 */
public class RegexPostProcessor implements PostProcessor
{
    private static final Logger LOG = Logger.getLogger(RegexPostProcessor.class.getName());

    private String name;
    private List<RegexPattern> patterns;
    /**
     * If true, any errors detected during post-processing will trigger
     * command failure.
     */
    private boolean failOnError = true;
    /**
     * If true, any warnings detected during post-processing will trigger
     * command failure.
     */
    private boolean failOnWarning = false;


    public RegexPostProcessor()
    {
        patterns = new LinkedList<RegexPattern>();
    }

    public RegexPostProcessor(String name)
    {
        this.name = name;
        patterns = new LinkedList<RegexPattern>();
    }

    public void process(File outputDir, StoredFileArtifact artifact, CommandResult result)
    {
        BufferedReader reader = null;
        try
        {
            File file = new File(outputDir, artifact.getPath());
            reader = new BufferedReader(new FileReader(file));
            String line;
            long lineNumber = 0;

            while ((line = reader.readLine()) != null)
            {
                lineNumber++;
                processLine(artifact, result, line, lineNumber);
            }
        }
        catch (IOException e)
        {
            LOG.warning("I/O error post-processing artifact '" + artifact.getPath() + "': " + e.getMessage());
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    private void processLine(StoredFileArtifact artifact, CommandResult result, String line, long lineNumber)
    {
        for (RegexPattern p : patterns)
        {
            String summary = p.match(line);
            if (summary != null)
            {
                if (p.getCategory() == Feature.Level.ERROR && failOnError)
                {
                    result.failure("Error features detected");
                }
                else if (p.getCategory() == Feature.Level.WARNING && failOnWarning)
                {
                    result.failure("Warning features detected");
                }

                artifact.addFeature(new PlainFeature(p.getCategory(), summary, lineNumber));
            }
        }
    }

    public RegexPattern createPattern()
    {
        RegexPattern pattern = new RegexPattern();
        addRegexPattern(pattern);
        return pattern;
    }

    /* Hrm, if we call this addPattern it gets magically picked up by FileLoader */
    public void addRegexPattern(RegexPattern pattern)
    {
        patterns.add(pattern);
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<RegexPattern> getPatterns()
    {
        return patterns;
    }

    public boolean getFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public boolean getFailOnWarning()
    {
        return failOnWarning;
    }

    public void setFailOnWarning(boolean failOnWarning)
    {
        this.failOnWarning = failOnWarning;
    }

}
