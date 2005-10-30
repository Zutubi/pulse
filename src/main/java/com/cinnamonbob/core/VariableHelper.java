package com.cinnamonbob.core;

import java.util.LinkedList;
import java.util.List;

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
     * @param input
     * @return
     * @throws FileLoadException
     *
     */
    private static List<Token> tokenise(String input) throws FileLoadException
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
                            // only add a token is there is something to add.
                            String str = current.toString();
                            if (str.length() > 0)
                            {
                                result.add(new Token(TokenType.TEXT, str));
                            }
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
                            throw new FileLoadException("Syntax error: expecting '{', got '" + inputChar + "'");
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
                                throw new FileLoadException("Syntax error: empty variable reference");
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
                String str = current.toString();
                if (str.length() > 0)
                {
                    result.add(new Token(TokenType.TEXT, current.toString()));
                }
                break;
            }
            case ESCAPED:
            {
                throw new FileLoadException("Syntax error: unexpected end of input in escape sequence (\\)");
            }
            case DOLLAR:
            {
                throw new FileLoadException("Syntax error: unexpected end of input looking for '{'");
            }
            case VARIABLE:
            {
                throw new FileLoadException("Syntax error: unexpected end of input looking for '}'");
            }
        }

        return result;
    }

    public static boolean containsVariables(String input) throws FileLoadException
    {
        List<Token> tokens = tokenise(input);
        for (Token token : tokens)
        {
            switch (token.type)
            {
                case VARIABLE_REFERENCE:
                    return true;

            }
        }
        return false;
    }

    public static Object replaceVariable(String input, Scope properties) throws FileLoadException
    {
        List<Token> tokens = tokenise(input);
        if (tokens.size() != 1 || tokens.get(0).type != TokenType.VARIABLE_REFERENCE)
        {
            throw new FileLoadException("Expected single variable reference. Instead found '"+input+"'"); //TODO
        }
        Token token = tokens.get(0);
        Reference ref = properties.getReference(token.value);
        if (ref != null)
        {
            return ref.getValue();
        }
        throw new FileLoadException("Unknown variable reference '" + token.value + "'");
    }

    /**
     * @param input
     * @param properties
     * @return
     * @throws FileLoadException
     *
     */
    public static String replaceVariables(String input, Scope properties)
            throws FileLoadException
    {
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
                    if (properties.containsReference(token.value))
                    {
                        Object obj = properties.getReference(token.value).getValue();
                        if (!(obj instanceof String))
                        {
                            throw new FileLoadException("Reference to non string variable '" + token.value + "'");
                        }
                        result.append(obj.toString());
                    }
                    else
                    {
                        throw new FileLoadException("Reference to unknown variable '" + token.value + "'");
                    }
                    break;
                }
            }
        }
        return result.toString();
    }

}
