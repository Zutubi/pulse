package com.cinnamonbob.core;

import com.cinnamonbob.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPostProcessor implements PostProcessor
{
    private static final Logger LOG = Logger.getLogger(RegexPostProcessor.class.getName());
    
    public final List<Pair<String, Pattern>> patterns = new LinkedList<Pair<String, Pattern>>();
    public PostProcessorCommon common;
        
    private void processLine(Artifact artifact, String line, long lineNumber)
    {
        for(Pair<String, Pattern> pair: patterns)
        {
            Matcher matcher = pair.second.matcher(line);
            if(matcher.matches())
            {
                artifact.addFeature(new PlainFeature(pair.first, line, lineNumber));
            }
        }
    }

    
    public RegexPostProcessor() 
    {
    }
    
    public void setPostProcessorCommon(PostProcessorCommon c)
    {
        this.common = c;
    }
    
    public void process(Artifact artifact)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(artifact.getFile()));
            String         line;
            long           lineNumber = 0;
            
            while((line = reader.readLine()) != null)
            {
                lineNumber++;
                processLine(artifact, line, lineNumber);
            }
        }
        catch(FileNotFoundException e)
        {
            LOG.warning("Artifact file '" + artifact.getFile().getName() + "' not found during post-processing by '" + common.getName() + "'");
        }
        catch(IOException e)
        {
            LOG.warning("I/O error post-processing artifact '" + artifact.getName() + "': " + e.getMessage());
        }
    }


    public boolean understandsType(String type)
    {
        return type.equals(Artifact.TYPE_PLAIN);
    }
}
