package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.ReferenceMap;

import java.util.LinkedList;
import java.util.List;

/**
 * Methods for analysing and replacing references within strings.
 */
public class ReferenceResolver
{
    public enum ResolutionStrategy
    {
        /**
         * Resolve all references, throwing an error for non-existant
         * references.
         */
        RESOLVE_STRICT
        {
            public boolean resolve()
            {
                return true;
            }
        },
        /**
         * Try to resolve all references but leave non-existant references
         * as they are.
         */
        RESOLVE_NON_STRICT
        {
            public boolean resolve()
            {
                return true;
            }
        },
        /**
         * Don't resolve any references, just process the input (e.g.
         * backslash escaping).
         */
        RESOLVE_NONE
        {
            public boolean resolve()
            {
                return false;
            }
        },
        ;

        public abstract boolean resolve();
    }

    /**
     * Tokens identified by lexer.
     */
    private enum TokenType
    {
        SPACE,
        TEXT,
        REFERENCE
    }

    /**
     * The lexer is a hand-written state machine with the below states (plus
     * modifiers).
     */
    private enum LexerState
    {
        INITIAL,
        ESCAPED,
        DOLLAR,
        REFERENCE_NAME
    }

    /**
     * A token produced by the lexer.
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

    private static List<Token> tokenise(String input, boolean split) throws ResolutionException
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
                            state = LexerState.REFERENCE_NAME;
                            break;
                        }
                        default:
                        {
                            throw new ResolutionException("Syntax error: expecting '{', got '" + inputChar + "'");
                        }
                    }
                    break;
                }
                case REFERENCE_NAME:
                {
                    switch (inputChar)
                    {
                        case '}':
                        {
                            if (current.length() == 0)
                            {
                                throw new ResolutionException("Syntax error: empty reference");
                            }

                            result.add(new Token(TokenType.REFERENCE, current.toString()));
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
                    throw new ResolutionException("Syntax error: unexpected end of input looking for closing quotes (\")");
                }
                
                addCurrent(current, haveData, result);
                break;
            }
            case ESCAPED:
            {
                throw new ResolutionException("Syntax error: unexpected end of input in escape sequence (\\)");
            }
            case DOLLAR:
            {
                throw new ResolutionException("Syntax error: unexpected end of input looking for '{'");
            }
            case REFERENCE_NAME:
            {
                throw new ResolutionException("Syntax error: unexpected end of input looking for '}'");
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

    public static boolean containsReference(String input) throws ResolutionException
    {
        List<Token> tokens = tokenise(input, false);
        for (Token token : tokens)
        {
            switch (token.type)
            {
                case REFERENCE:
                    return true;
            }
        }
        return false;
    }

    public static Object resolveReference(String input, ReferenceMap references) throws ResolutionException
    {
        List<Token> tokens = tokenise(input, false);
        if (tokens.size() != 1 || tokens.get(0).type != TokenType.REFERENCE)
        {
            throw new ResolutionException("Expected single reference. Instead found '" + input + "'");
        }
        Token token = tokens.get(0);
        Reference ref = references.getReference(token.value);
        if (ref != null)
        {
            return ref.getValue();
        }
        throw new ResolutionException("Unknown reference '" + token.value + "'");
    }

    public static String resolveReferences(String input, ReferenceMap references) throws ResolutionException
    {
        return resolveReferences(input, references, ResolutionStrategy.RESOLVE_STRICT);
    }

    public static String resolveReferences(String input, ReferenceMap references, ResolutionStrategy resolutionStrategy) throws ResolutionException
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
                case REFERENCE:
                {
                    result.append(resolveReference(references, token, resolutionStrategy));
                    break;
                }
            }
        }
        return result.toString();
    }

    public static List<String> splitAndResolveReferences(String input, ReferenceMap references, ResolutionStrategy resolutionStrategy) throws ResolutionException
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
                case REFERENCE:
                {
                    String value = resolveReference(references, token, resolutionStrategy);
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

    private static String resolveReference(ReferenceMap references, Token token, ResolutionStrategy resolutionStrategy) throws ResolutionException
    {
        if(resolutionStrategy.resolve())
        {
            Reference reference = references.getReference(token.value);
            if (reference != null && reference.getValue() != null)
            {
                return reference.getValue().toString();
            }
            else if(resolutionStrategy == ResolutionStrategy.RESOLVE_STRICT)
            {
                throw new ResolutionException("Unknown reference '" + token.value + "'");
            }
        }

        return "${" + token.value + "}";
    }

}
