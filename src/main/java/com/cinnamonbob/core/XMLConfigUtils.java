package com.cinnamonbob.core;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;

import java.io.IOException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A utility class to help parse XML configuration files.
 */
public class XMLConfigUtils
{
    public static Document loadFile(String filename) throws ConfigException
    {
        try
        {
            Builder builder = new Builder();
            Document doc = builder.build(new File(filename));
            
            return doc;
        }
        catch(ParsingException e)
        {
            e.printStackTrace();
            throw new ConfigException(filename, e.getLineNumber(), e.getColumnNumber(), e.getMessage());
        }
        catch(IOException e)
        {
            throw new ConfigException(filename, e.getMessage());
        }
    }
    
    
    public static String getElementText(String filename, Element element, boolean trim) throws ConfigException
    {
        String result = "";
        
        for(int i = 0; i < element.getChildCount(); i++)
        {
            Node current = element.getChild(i);
            if(current instanceof Text)
            {
                result += ((Text)current).getValue();
            }
            else if(current instanceof Element)
            {
                throw new ConfigException(filename, "Unexpected child element '" + ((Element)current).getLocalName() + "' nested in element '" + element.getLocalName() + "'");
            }
        }
        
        if(trim)
        {
            result = result.trim();
        }
        
        if(result.length() == 0)
        {
            throw new ConfigException(filename, "Element '" + element.getLocalName() + "' requires text content");
        }
        
        return result;
    }
    
    
    public static String getElementText(String filename, Element element) throws ConfigException
    {
        return getElementText(filename, element, true);
    }
    
    
    public static int getElementInt(String filename, Element element, int min, int max) throws ConfigException
    {
        String text = getElementText(filename, element);
        int result;
        
        try
        {
            result = Integer.parseInt(text);
        }
        catch(NumberFormatException e)
        {
            throw new ConfigException(filename, "Element '" + element.getLocalName() + "' requires an integer as content (found '" + text + "')");
        }
        
        if(result < min || result > max)
        {
            throw new ConfigException(filename, "Element '" + element.getLocalName() + "' requires an integer in the range [" + Integer.toString(min) + "," + Integer.toString(max) + "] (found " + text + ")");
        }
        
        return result;
    }
    
    
    public static List<Element> getElements(String filename, Element parent)
    {
        LinkedList<Element> results = new LinkedList<Element>();
        
        for(int i = 0; i < parent.getChildCount(); i++)
        {
            Node current = parent.getChild(i);
            
            if(current instanceof Element)
            {
                results.add((Element)current);
            }
        }
        
        return results;
    }
    
    
    public static List<Element> getElements(String filename, Element parent, List<String> expectedNames) throws ConfigException
    {
        LinkedList<Element> results = new LinkedList<Element>();
        
        for(int i = 0; i < parent.getChildCount(); i++)
        {
            Node current = parent.getChild(i);
            
            if(current instanceof Element)
            {
                Element currentElement = (Element)current;
                
                if(expectedNames.contains(currentElement.getLocalName()))
                {
                    results.add(currentElement);
                }
                else
                {
                    throw new ConfigException(filename, "Unexpected child element '" + currentElement.getLocalName() + "' nested in element '" + parent.getLocalName() + "'");
                }
            }
        }
        
        return results;
    }


    public static String getAttributeValue(String filename, Element element, String name) throws ConfigException
    {
        String value = element.getAttributeValue(name);
        if(value == null)
        {
            throw new ConfigException(filename, "Element '" + element.getLocalName() + "' missing required attribute '" + name + "'");
        }
        
        return value;
    }
}
