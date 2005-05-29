package com.cinnamonbob.core;

import nu.xom.*;

import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A utility class to help parse XML configuration files.
 */
public class XMLConfigUtils
{
    public static final String CONFIG_ELEMENT_PROPERTY = "property";
    
    private static final String CONFIG_ATTR_NAME        = "name";
    private static final String CONFIG_ATTR_VALUE       = "value";


    private enum TokenType
    {
        TEXT,
        VARIABLE_REFERENCE
    }
    
    
    private enum LexerState
    {
        INITIAL,
        ESCAPED,
        DOLLAR,
        VARIABLE
    }
    
    private static class Token
    {
        public TokenType type;
        public String    value;
        
        public Token(TokenType type, String value)
        {
            this.type  = type;
            this.value = value;
        }
    }
    
    
    private static List<Token> tokenise(ConfigContext context, String input) throws ConfigException
    {
        List<Token>   result  = new LinkedList<Token>();
        LexerState    state   = LexerState.INITIAL;
        StringBuilder current = new StringBuilder();
        
        for(int i = 0; i < input.length(); i++)
        {
            char inputChar = input.charAt(i);
            
            switch(state)
            {
                case INITIAL:
                {
                    switch(inputChar)
                    {
                        case '\\':
                        {
                            state = LexerState.ESCAPED;
                            break;
                        }
                        case '$':
                        {
                            state = LexerState.DOLLAR;
                            result.add(new Token(TokenType.TEXT, current.toString()));
                            current = new StringBuilder();
                            break;
                        }
                        default:
                        {
                            current.append(inputChar);
                            break;
                        }
                    }
                    break;
                }
                case ESCAPED:
                {
                    current.append(inputChar);
                    state = LexerState.INITIAL;
                    break;
                }
                case DOLLAR:
                {
                    switch(inputChar)
                    {
                        case '{':
                        {
                            state = LexerState.VARIABLE;
                            break;
                        }
                        default:
                        {
                            // TODO give more context
                            throw new ConfigException(context.getFilename(), "Syntax error: expecting '{', got '" + inputChar + "'");
                        }
                    }
                    break;
                }
                case VARIABLE:
                {
                    switch(inputChar)
                    {
                        case '}':
                        {
                            if(current.length() == 0)
                            {
                                throw new ConfigException(context.getFilename(), "Syntax error: empty variable reference");
                            }
                            
                            result.add(new Token(TokenType.VARIABLE_REFERENCE, current.toString()));
                            state = LexerState.INITIAL;
                            current = new StringBuilder();
                            break;
                        }
                        default:
                        {
                            current.append(inputChar);
                            break;
                        }
                    }
                }
                break;
            }
        }
        
        switch(state)
        {
            case INITIAL:
            {
                result.add(new Token(TokenType.TEXT, current.toString()));                
                break;
            }
            case ESCAPED:
            {
                throw new ConfigException(context.getFilename(), "Syntax error: unexpected end of input in escape sequence (\\)");
            }
            case DOLLAR:
            {
                throw new ConfigException(context.getFilename(), "Syntax error: unexpected end of input looking for '{'");
            }
            case VARIABLE:
            {
                throw new ConfigException(context.getFilename(), "Syntax error: unexpected end of input looking for '}'");
            }
        }

        return result;
    }
    
    
    public static String replaceVariables(ConfigContext context, String input) throws ConfigException
    {
        StringBuilder result = new StringBuilder();
        
        List<Token> tokens = tokenise(context, input);
        
        for(Token token: tokens)
        {
            switch(token.type)
            {
                case TEXT:
                {
                    result.append(token.value);
                    break;
                }
                case VARIABLE_REFERENCE:
                {
                    if(context.hasVariable(token.value))
                    {
                        result.append(context.getVariableValue(token.value));
                    }
                    else
                    {
                        throw new ConfigException(context.getFilename(), "Reference to unknown variable '" + token.value + "'");
                    }
                    break;
                }
            }
        }
        
        return result.toString();
    }

    
    public static void extractProperties(ConfigContext context, List<Element> elements) throws ConfigException
    {
        for(Iterator<Element> it = elements.iterator(); it.hasNext(); )
        {
            Element element = it.next();
            
            if(element.getLocalName().equals(CONFIG_ELEMENT_PROPERTY))
            {
                String name  = getAttributeValue(context, element, CONFIG_ATTR_NAME);
                String value = getAttributeValue(context, element, CONFIG_ATTR_VALUE);
                
                context.setVariable(name, value);
                it.remove();
            }
        }
    }
    
    public static Document loadFile(String filename) throws ConfigException
    {
        try
        {
            Builder builder = new Builder(new NodeFactory());
            Document doc = builder.build(new File(filename));
            
            return doc;
        }
        catch(ParsingException e)
        {
            throw new ConfigException(filename, e.getLineNumber(), e.getColumnNumber(), e.getMessage());
        }
        catch(IOException e)
        {
            throw new ConfigException(filename, e.getMessage());
        }
    }
    
    
    public static String getElementText(ConfigContext context, Element element, boolean trim) throws ConfigException
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
                throw new ConfigException(context.getFilename(), "Unexpected child element '" + ((Element)current).getLocalName() + "' nested in element '" + element.getLocalName() + "'");
            }
        }
        
        result = replaceVariables(context, result);
        
        if(trim)
        {
            result = result.trim();
        }
        
        if(result.length() == 0)
        {
            throw new ConfigException(context.getFilename(), "Element '" + element.getLocalName() + "' requires text content");
        }
        
        return result;
    }
    
    
    public static String getElementText(ConfigContext context, Element element) throws ConfigException
    {
        return getElementText(context, element, true);
    }
    
    
    public static int getElementInt(ConfigContext context, Element element, int min, int max) throws ConfigException
    {
        String text = getElementText(context, element);
        int result;
        
        try
        {
            result = Integer.parseInt(text);
        }
        catch(NumberFormatException e)
        {
            throw new ConfigException(context.getFilename(), "Element '" + element.getLocalName() + "' requires an integer as content (found '" + text + "')");
        }
        
        if(result < min || result > max)
        {
            throw new ConfigException(context.getFilename(), "Element '" + element.getLocalName() + "' requires an integer in the range [" + Integer.toString(min) + "," + Integer.toString(max) + "] (found " + text + ")");
        }
        
        return result;
    }
    
    
    public static List<Element> getElements(ConfigContext context, Element parent)
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
    
    
    public static List<Element> getElements(ConfigContext context, Element parent, List<String> expectedNames) throws ConfigException
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
                    throw new ConfigException(context.getFilename(), "Unexpected child element '" + currentElement.getLocalName() + "' nested in element '" + parent.getLocalName() + "'");
                }
            }
        }
        
        return results;
    }


    public static String getAttributeValue(ConfigContext context, Element element, String name) throws ConfigException
    {
        String value = element.getAttributeValue(name);
        if(value == null)
        {
            throw new ConfigException(context.getFilename(), "Element '" + element.getLocalName() + "' missing required attribute '" + name + "'");
        }
        
        return replaceVariables(context, value);
    }
    
    
    public static String getAttributeValue(ConfigContext context, Element element, String name, String defaultValue) throws ConfigException
    {
        if(defaultValue == null)
        {
            return getAttributeValue(context, element, name);
        }
        else
        {
            return getOptionalAttributeValue(context, element, name, defaultValue);
        }
    }    

    
    public static String getOptionalAttributeValue(ConfigContext context, Element element, String name, String defaultValue) throws ConfigException
    {
        String value = element.getAttributeValue(name);
        if(value == null)
        {
            value = defaultValue;
        }
        else
        {
            value = replaceVariables(context, value);
        }
                
        return value;
    }    
}
