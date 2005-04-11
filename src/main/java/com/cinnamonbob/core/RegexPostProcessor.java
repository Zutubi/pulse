package com.cinnamonbob.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.cinnamonbob.util.Pair;

import nu.xom.Element;

public class RegexPostProcessor implements PostProcessor
{
    private static final Logger LOG = Logger.getLogger(RegexPostProcessor.class.getName());
    
    private static final String CONFIG_ELEMENT_PATTERN = "pattern";
    private static final String CONFIG_ATTR_CATEGORY   = "category";
    private static final String CONFIG_ATTR_EXPRESSION = "expression";
       
    private List<Pair<String, Pattern>> patterns;
    private PostProcessorCommon common;
    private Project project;
    
    private void loadPattern(String filename, Element element) throws ConfigException
    {
        String category   = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_CATEGORY);
        String expression = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_ATTR_EXPRESSION);
        
        if(!project.getCategoryRegistry().hasCategory(category))
        {
            throw new ConfigException(filename, "Post processor '" + common.getName() + "' refers to unknown category '" + category +"'");
        }
        
        try
        {
            Pattern pattern = Pattern.compile(expression);
            patterns.add(new Pair<String, Pattern>(category, pattern));
        }
        catch(PatternSyntaxException e)
        {
            throw new ConfigException(filename, "Post processor '" + common.getName() + "' contains invalid expression: " + e.getMessage());
        }
    }


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

    
    public RegexPostProcessor(String filename, Element element, PostProcessorCommon common, Project project) throws ConfigException
    {
        this.common  = common;
        this.project = project;
        patterns     = new LinkedList<Pair<String, Pattern>>();
        
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList(CONFIG_ELEMENT_PATTERN));
        
        for(Element current: elements)
        {
            loadPattern(filename, current);
        }
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
