package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.ReferenceMap;
import com.zutubi.util.UnaryFunction;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Methods for analysing and replacing references within strings.
 */
public class ReferenceResolver
{
    private static final Map<String, UnaryFunction<String, String>> FILTER_FUNCTIONS = new HashMap<String, UnaryFunction<String, String>>();
    static
    {
        FILTER_FUNCTIONS.put("trim", new UnaryFunction<String, String>()
        {
            public String process(String s)
            {
                return s.trim();
            }
        });

        FILTER_FUNCTIONS.put("lower", new UnaryFunction<String, String>()
        {
            public String process(String s)
            {
                return s.toLowerCase();
            }
        });

        FILTER_FUNCTIONS.put("upper", new UnaryFunction<String, String>()
        {
            public String process(String s)
            {
                return s.toUpperCase();
            }
        });

        FILTER_FUNCTIONS.put("name", new UnaryFunction<String, String>()
        {
            public String process(String s)
            {
                return s.trim().replaceAll("[\\\\/$]", ".");
            }
        });

        FILTER_FUNCTIONS.put("normalise", new UnaryFunction<String, String>()
        {
            public String process(String s)
            {
                return s.trim().replaceAll("[\\\\/]", File.separator);
            }
        });
    }

    public enum ResolutionStrategy
    {
        /**
         * Resolve all references, throwing an error for non-existant
         * references.
         */
        RESOLVE_STRICT(true),
        /**
         * Try to resolve all references but leave non-existant references
         * as they are.
         */
        RESOLVE_NON_STRICT(true),
        /**
         * Don't resolve any references, just process the input (e.g.
         * backslash escaping).
         */
        RESOLVE_NONE(false);

        private boolean resolve;

        private ResolutionStrategy(boolean resolve)
        {
            this.resolve = resolve;
        }

        public boolean resolve()
        {
            return resolve;
        }
    }

    /**
     * Tokens identified by lexer.
     */
    private enum TokenType
    {
        SPACE,
        TEXT,
        REFERENCE,
        DEFAULT_VALUE,
        FILTER
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
        REFERENCE_NAME,
        EXTENDED_REFERENCE_NAME,
        DEFAULT_VALUE,
        FILTER_NAME
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
                        case '(':
                        {
                            state = LexerState.EXTENDED_REFERENCE_NAME;
                            break;
                        }
                        default:
                        {
                            throw new ResolutionException("Syntax error: expecting '{' or '(', got '" + inputChar + "'");
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
                    break;
                }
                case EXTENDED_REFERENCE_NAME:
                {
                    switch (inputChar)
                    {
                        case ')':
                        case '?':
                        case '|':
                        {
                            if (current.length() == 0)
                            {
                                throw new ResolutionException("Syntax error: empty reference");
                            }

                            result.add(new Token(TokenType.REFERENCE, current.toString()));
                            current.delete(0, current.length());
                            state = chooseExtendedState(inputChar);
                            break;
                        }
                        case '!':
                        case '%':
                        case '#':
                        case '&':
                        case '/':
                        case ':':
                        case ';':
                        {
                            throw new ResolutionException("Syntax error: '" + inputChar + "' is reserved and may not be used in an extended reference name");
                        }
                        default:
                        {
                            current.append(inputChar);
                            break;
                        }
                    }
                    break;
                }
                case DEFAULT_VALUE:
                {
                    switch (inputChar)
                    {
                        case ')':
                        {
                            result.add(new Token(TokenType.DEFAULT_VALUE, current.toString()));
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
                    break;
                }
                case FILTER_NAME:
                {
                    switch (inputChar)
                    {
                        case '?':
                        case ')':
                        case '|':
                        {
                            result.add(new Token(TokenType.FILTER, current.toString()));
                            current.delete(0, current.length());
                            state = chooseExtendedState(inputChar);
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
                throw new ResolutionException("Syntax error: unexpected end of input looking for '{' or '('");
            }
            case REFERENCE_NAME:
            {
                throw new ResolutionException("Syntax error: unexpected end of input looking for '}'");
            }
            case EXTENDED_REFERENCE_NAME:
            case DEFAULT_VALUE:
            case FILTER_NAME:
            {
                throw new ResolutionException("Syntax error: unexpected end of input looking for ')'");
            }
        }

        return result;
    }

    private static LexerState chooseExtendedState(char inputChar)
    {
        switch (inputChar)
        {
            case ')':
            {
                return LexerState.INITIAL;
            }
            case '|':
            {
                return LexerState.FILTER_NAME;
            }
            default:
            {
                return LexerState.DEFAULT_VALUE;
            }
        }
    }

    private static void addCurrent(StringBuilder current, boolean haveData, List<Token> result)
    {
        if (haveData)
        {
            result.add(new Token(TokenType.TEXT, current.toString()));
            current.delete(0, current.length());
        }
    }

    private enum ParseElementType
    {
        TEXT,
        SPACE,
        REFERENCE
    }

    /**
     * Base for parse elements: we don't use a full-blown tree, we just compose
     * related tokens into single elements of specific types.
     */
    private static abstract class ParseElement
    {
        private ParseElementType type;

        protected ParseElement(ParseElementType type)
        {
            this.type = type;
        }
    }

    private static class SimpleElement extends ParseElement
    {
        private String value;

        private SimpleElement(ParseElementType type, String value)
        {
            super(type);
            this.value = value;
        }
    }

    private static class ReferenceElement extends ParseElement
    {
        private String name;
        private List<String> filters = new LinkedList<String>();
        private String defaultValue;

        private ReferenceElement(ParseElementType type, String name)
        {
            super(type);
            this.name = name;
        }

        public List<String> getFilters()
        {
            return filters;
        }

        public void addFilter(String name)
        {
            filters.add(name);
        }
    }

    private static List<ParseElement> parse(String input, boolean split) throws ResolutionException
    {
        List<ParseElement> parseElements = new LinkedList<ParseElement>();
        List<Token> tokens = tokenise(input, split);
        for (Token token: tokens)
        {
            switch (token.type)
            {
                case TEXT:
                {
                    parseElements.add(new SimpleElement(ParseElementType.TEXT, token.value));
                    break;
                }
                case SPACE:
                {
                    parseElements.add(new SimpleElement(ParseElementType.SPACE, token.value));
                    break;
                }
                case REFERENCE:
                {
                    parseElements.add(new ReferenceElement(ParseElementType.REFERENCE, token.value));
                    break;
                }
                case DEFAULT_VALUE:
                {
                    ReferenceElement reference = (ReferenceElement) parseElements.get(parseElements.size() - 1);
                    reference.defaultValue = token.value;
                    break;
                }
                case FILTER:
                {
                    ReferenceElement reference = (ReferenceElement) parseElements.get(parseElements.size() - 1);
                    reference.addFilter(token.value);
                    break;
                }
            }
        }

        return parseElements;
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
        List<ParseElement> elements = parse(input, false);
        if (elements.size() != 1 || elements.get(0).type != ParseElementType.REFERENCE)
        {
            throw new ResolutionException("Expected single reference. Instead found '" + input + "'");
        }
        ReferenceElement element = (ReferenceElement) elements.get(0);
        Reference ref = references.getReference(element.name);
        if (ref != null)
        {
            return ref.getValue();
        }
        else if (element.defaultValue != null)
        {
            return element.defaultValue;
        }

        throw new ResolutionException("Unknown reference '" + element.name + "'");
    }

    public static String resolveReferences(String input, ReferenceMap references) throws ResolutionException
    {
        return resolveReferences(input, references, ResolutionStrategy.RESOLVE_STRICT);
    }

    public static String resolveReferences(String input, ReferenceMap references, ResolutionStrategy resolutionStrategy) throws ResolutionException
    {
        StringBuilder result = new StringBuilder();

        List<ParseElement> elements = parse(input, false);
        for (ParseElement element : elements)
        {
            switch (element.type)
            {
                case TEXT:
                {
                    result.append(((SimpleElement) element).value);
                    break;
                }
                case REFERENCE:
                {
                    result.append(resolveReference(references, (ReferenceElement) element, resolutionStrategy));
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

        List<ParseElement> elements = parse(input, true);

        for (ParseElement element : elements)
        {
            switch (element.type)
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
                    current.append(((SimpleElement) element).value);
                    haveData = true;
                    break;
                }
                case REFERENCE:
                {
                    String value = resolveReference(references, (ReferenceElement) element, resolutionStrategy);
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

    private static String resolveReference(ReferenceMap references, ReferenceElement element, ResolutionStrategy resolutionStrategy) throws ResolutionException
    {
        if (resolutionStrategy.resolve())
        {
            Reference reference = references.getReference(element.name);
            if (reference != null && reference.getValue() != null)
            {
                return filter(reference.getValue().toString(), element.getFilters(), resolutionStrategy);
            }
            else if (element.defaultValue != null)
            {
                return element.defaultValue;
            }
            else if(resolutionStrategy == ResolutionStrategy.RESOLVE_STRICT)
            {
                throw new ResolutionException("Unknown reference '" + element.name + "'");
            }
        }

        return "${" + element.name + "}";
    }

    private static String filter(String value, List<String> filters, ResolutionStrategy resolutionStrategy) throws ResolutionException
    {
        for (String filter: filters)
        {
            UnaryFunction<String, String> fn = FILTER_FUNCTIONS.get(filter);
            if (fn == null)
            {
                if (resolutionStrategy == ResolutionStrategy.RESOLVE_STRICT)
                {
                    throw new ResolutionException("Unknown filter '" + filter + "'");
                }
            }
            else
            {
                value = fn.process(value);
            }
        }

        return value;
    }
}
