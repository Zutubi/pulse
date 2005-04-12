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
import java.util.Map;

/**
 * A utility class to help parse XML configuration files.
 */
public class XMLConfigUtils
{
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
    
    
    private static List<Token> tokenise(String filename, String input) throws ConfigException
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
                            throw new ConfigException(filename, "Syntax error: expecting '{', got '" + inputChar + "'");
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
                                throw new ConfigException(filename, "Syntax error: empty variable reference");
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
                throw new ConfigException(filename, "Syntax error: unexpected end of input in escape sequence (\\)");
            }
            case DOLLAR:
            {
                throw new ConfigException(filename, "Syntax error: unexpected end of input looking for '{'");
            }
            case VARIABLE:
            {
                throw new ConfigException(filename, "Syntax error: unexpected end of input looking for '}'");
            }
        }

        return result;
    }
    
    
    public static String replaceVariables(String filename, Map<String, String> variables, String input) throws ConfigException
    {
        StringBuilder result = new StringBuilder();
        
        List<Token> tokens = tokenise(filename, input);
        
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
                    if(variables.containsKey(token.value))
                    {
                        result.append(variables.get(token.value));
                    }
                    else
                    {
                        throw new ConfigException(filename, "Reference to unknown variable '" + token.value + "'");
                    }
                    break;
                }
            }
        }
        
        return result.toString();
    }

    
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
    
    
    public static String getAttributeValue(Element element, String name, String defaultValue)
    {
        String value = element.getAttributeValue(name);
        if(value == null)
        {
            value = defaultValue;
        }
        
        return value;        
    }    
}
