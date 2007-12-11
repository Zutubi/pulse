package com.zutubi.pulse.core;

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
        SPACE,
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

    private static List<Token> tokenise(String input, boolean split) throws FileLoadException
    {
        List<Token> result = new LinkedList<Token>();
        LexerState state = LexerState.INITIAL;
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean haveData = false;

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
                        case '"':
                        {
                            if(split)
                            {
                                if(quoted)
                                {
                                    quoted = false;
                                }
                                else
                                {
                                    quoted = true;
                                    haveData = true;
                                }
                            }
                            else
                            {
                                current.append(inputChar);
                                haveData = true;
                            }
                            break;
                        }
                        case ' ':
                        {
                            if(split)
                            {
                                if(quoted)
                                {
                                    current.append(inputChar);
                                    haveData = true;
                                }
                                else
                                {
                                    addCurrent(current, haveData, result);
                                    haveData = false;
                                    result.add(new Token(TokenType.SPACE, " "));
                                }
                            }
                            else
                            {
                                current.append(inputChar);
                                haveData = true;
                            }
                            break;
                        }
                        case '$':
                        {
                            state = LexerState.DOLLAR;
                            // only add a token if there is something to add.
                            addCurrent(current, haveData, result);
                            haveData = false;
                            break;
                        }
                        default:
                        {
                            current.append(inputChar);
                            haveData = true;
                            break;
                        }
                    }
                    break;
                }
                case ESCAPED:
                {
                    current.append(inputChar);
                    haveData = true;
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
                            current.delete(0, current.length());
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
                if(quoted)
                {
                    throw new FileLoadException("Syntax error: unexpected end of input looking for closing quotes (\")");
                }
                
                addCurrent(current, haveData, result);
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

    private static void addCurrent(StringBuilder current, boolean haveData, List<Token> result)
    {
        if (haveData)
        {
            result.add(new Token(TokenType.TEXT, current.toString()));
            current.delete(0, current.length());
        }
    }

    public static boolean containsVariables(String input) throws FileLoadException
    {
        List<Token> tokens = tokenise(input, false);
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

    public static Object replaceVariable(String input, ReferenceMap properties) throws FileLoadException
    {
        List<Token> tokens = tokenise(input, false);
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

    public static String replaceVariables(String input, ReferenceMap properties) throws FileLoadException
    {
        return replaceVariables(input, properties, false);
    }

    public static String replaceVariables(String input, ReferenceMap properties, boolean allowUnresolved) throws FileLoadException
    {
        StringBuilder result = new StringBuilder();

        List<Token> tokens = tokenise(input, false);

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
                    result.append(resolveReference(properties, token, allowUnresolved));
                    break;
                }
            }
        }
        return result.toString();
    }

    public static List<String> splitAndReplaceVariables(String input, ReferenceMap properties, boolean allowUnresolved) throws FileLoadException
    {
        List<String> result = new LinkedList<String>();
        StringBuilder current = new StringBuilder();
        boolean haveData = false;

        List<Token> tokens = tokenise(input, true);

        for (Token token : tokens)
        {
            switch (token.type)
            {
                case SPACE:
                {
                    if(haveData)
                    {
                        result.add(current.toString());
                        current.delete(0, current.length());
                        haveData = false;
                    }
                    break;
                }
                case TEXT:
                {
                    current.append(token.value);
                    haveData = true;
                    break;
                }
                case VARIABLE_REFERENCE:
                {
                    String value = resolveReference(properties, token, allowUnresolved);
                    if(value.length() > 0)
                    {
                        current.append(value);
                        haveData = true;
                    }
                    break;
                }
            }
        }

        if(haveData)
        {
            result.add(current.toString());
        }

        return result;
    }

    private static String resolveReference(ReferenceMap properties, Token token, boolean allowUnresolved) throws FileLoadException
    {
        Reference reference = properties.getReference(token.value);
        if (reference != null && reference.getValue() != null)
        {
            Object obj = reference.getValue();
            if (!(obj instanceof String))
            {
                throw new FileLoadException("Reference to non string variable '" + token.value + "'");
            }
            return obj.toString();
        }
        else if(allowUnresolved)
        {
            return "${" + token.value + "}";
        }
        else
        {
            throw new FileLoadException("Reference to unknown variable '" + token.value + "'");
        }
    }

}
