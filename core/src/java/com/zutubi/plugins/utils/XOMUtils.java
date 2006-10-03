package com.zutubi.plugins.utils;

import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class XOMUtils
{
    public static Document parseText(String xmlFragment) throws ParsingException
    {
        Builder builder = new Builder();

        try
        {
            return builder.build(new ByteArrayInputStream(xmlFragment.getBytes()));
        }
        catch (IOException ioe)
        {
            // noop.
            return null;
        }
    }
}