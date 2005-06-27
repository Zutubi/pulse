package com.cinnamonbob.core2.config;

import com.cinnamonbob.BobException;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 * 
 *
 */
public class VariableHelper
{

    // Handle the lexical analysis of variable references.

    /**
     *
     */
    private enum TokenType
    {
        TEXT,
        VARIABLE_REFERENCE
    }

    /**
     *
     */
    private enum LexerState
    {
        INITIAL,
        ESCAPED,
        DOLLAR,
        VARIABLE
    }

    /**
     *
     */
    private static class Token
    {
        public TokenType type;
        public String value;

        public Token(TokenType type, String value)
        {
            this.type = type;
            this.value = value;
        }
    }

    /**
     *
     * @param input
     * @return
     * @throws com.cinnamonbob.BobException
     */
    private static List<Token> tokenise(String input) throws BobException
    {
        List<Token> result = new LinkedList<Token>();
        LexerState state = LexerState.INITIAL;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++)
        {
            char inputChar = input.charAt(i);

            switch (state)
            {
                case INITIAL:
                    {
                        switch (inputChar)
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
                        switch (inputChar)
                        {
                            case '{':
                                {
                                    state = LexerState.VARIABLE;
                                    break;
                                }
                            default:
                                {
                                    // TODO give some context
                                    throw new BobException("Syntax error: expecting '{', got '" + inputChar + "'");
                                }
                        }
                        break;
                    }
                case VARIABLE:
                    {
                        switch (inputChar)
                        {
                            case '}':
                                {
                                    if (current.length() == 0)
                                    {
                                        throw new BobException("Syntax error: empty variable reference");
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

        switch (state)
        {
            case INITIAL:
                {
                    result.add(new Token(TokenType.TEXT, current.toString()));
                    break;
                }
            case ESCAPED:
                {
                    throw new BobException("Syntax error: unexpected end of input in escape sequence (\\)");
                }
            case DOLLAR:
                {
                    throw new BobException("Syntax error: unexpected end of input looking for '{'");
                }
            case VARIABLE:
                {
                    throw new BobException("Syntax error: unexpected end of input looking for '}'");
                }
        }

        return result;
    }

    public static boolean containsVariables(String input) throws BobException
    {
        List<Token> tokens = tokenise(input);
        for (Token token: tokens)
        {
            switch (token.type)
            {
                case VARIABLE_REFERENCE:
                    return true;
                
            }
        }
        return false;
    }
    
    /**
     * 
     * @param input
     * @param properties
     * @return
     * @throws com.cinnamonbob.BobException
     */ 
    public static String replaceVariables(String input, Map<String, String> properties) throws BobException
    {
        try {
            return replaceVariables(input, properties, 0);
        }
        catch (StackOverflowError e)
        {
            throw new BobException("Variable error: could not resolve variables in input.");   
        }
    }

    private static String replaceVariables(String input, Map<String, String> properties, int depth)
            throws BobException
    {
        if (depth > 15) // if we get to a depth of 15, then we can assume that we have a circular reference.
        {   
            throw new StackOverflowError();
        }
        
        StringBuilder result = new StringBuilder();

        List<Token> tokens = tokenise(input);

        for (Token token : tokens)
        {
            switch (token.type)
            {
                case TEXT:
                    {
                        result.append(token.value);
                        break;
                    }
                case VARIABLE_REFERENCE:
                    {
                        if (properties.containsKey(token.value))
                        {
                            String value = properties.get(token.value);
                            if (containsVariables(value))
                            {
                                value = replaceVariables(value, properties, depth + 1);    
                            }
                            result.append(value);
                        } 
                        else
                        {
                            throw new BobException("Reference to unknown variable '" + token.value + "'");    
                        }
                        break;
                    }
            }
        }
        return result.toString();
    }


}
