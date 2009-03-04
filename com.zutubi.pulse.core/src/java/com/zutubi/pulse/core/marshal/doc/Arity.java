package com.zutubi.pulse.core.marshal.doc;

/**
 * Indicates what number of child elements may appear in a specific parent
 * element.
 */
public enum Arity
{
    ZERO_OR_ONE
    {
        public String shortForm()
        {
            return "0 or 1";
        }
    },

    EXACTLY_ONE
    {
        public String shortForm()
        {
            return "1";
        }
    },

    ONE_OR_MORE
    {
        public String shortForm()
        {
            return "1 or more";
        }
    },

    ZERO_OR_MORE
    {
        public String shortForm()
        {
            return "0 or more";
        }
    };
    
    public abstract String shortForm();
}
