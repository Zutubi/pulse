package com.cinnamonbob.core;

import com.cinnamonbob.core.model.PlainFeature;
import com.cinnamonbob.core.model.StoredArtifact;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;


/**
 * 
 *
 */
public class RegexPostProcessor implements PostProcessor
{
    private static final Logger LOG = Logger.getLogger(RegexPostProcessor.class.getName());

    private String name;

    private List<RegexPattern> patterns = new LinkedList<RegexPattern>();

    public void process(StoredArtifact artifact)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(artifact.getFile()));
            String line;
            long lineNumber = 0;

            while((line = reader.readLine()) != null)
            {
                lineNumber++;
                processLine(artifact, line, lineNumber);
            }
        }
        catch(IOException e)
        {
            LOG.warning("I/O error post-processing artifact '" + artifact.getName() + "': " + e.getMessage());
        }

    }

    private void processLine(StoredArtifact artifact, String line, long lineNumber)
    {
        for(RegexPattern p: patterns)
        {
            Matcher matcher = p.getPattern().matcher(line);
            if(matcher.matches())
            {
                artifact.addFeature(new PlainFeature(p.getCategory(), line, lineNumber));
            }
        }
    }

    public RegexPattern createPattern()
    {
        RegexPattern pattern = new RegexPattern();
        patterns.add(pattern);
        return pattern;
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
}
