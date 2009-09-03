package com.zutubi.pulse.core.util.api;

import com.zutubi.util.logging.Logger;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.lang.reflect.Field;

/**
 * XML Utilities used for working with a pull parser.
 */
public class XMLStreamUtils
{
    private static final Logger LOG = Logger.getLogger(XMLStreamUtils.class);

    /**
     * Get a map containing the attributes of the current tag.
     *
     * @param reader    the reader whose state references the tag for which the
     *                  attributes will be retrieved.
     * @return a map of attribute keys to attribute values.
     */
    public static Map<String, String> getAttributes(XMLStreamReader reader)
    {
        Map<String, String> attributes = new HashMap<String, String>();
        for (int i = 0; i < reader.getAttributeCount(); i++)
        {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            attributes.put(name, value);
        }
        return attributes;
    }

    /**
     * Free any resources currently held by the XMLStreamReader instance.
     *
     * Note: This does not close the underlying input source.
     *
     * @param reader    the reader being closed.
     *
     * @see javax.xml.stream.XMLStreamReader#close() 
     */
    public static void close(XMLStreamReader reader)
    {
        if (reader != null)
        {
            try
            {
                reader.close();
            }
            catch (XMLStreamException e)
            {
                LOG.finest(e);
            }
        }
    }

    /**
     * Fast forward the state of the xml stream reader to the first tag after the end tag
     * matching the current tag of the reader.
     *
     * This is equivalent to calling {@link #fastForwardToEndTag(javax.xml.stream.XMLStreamReader, boolean)}
     * with consumeEndTag set to true.
     *
     * @param reader    the reader
     * @throws javax.xml.stream.XMLStreamException on error
     */
    public static void fastForwardToEndTag(XMLStreamReader reader) throws XMLStreamException
    {
        fastForwardToEndTag(reader, true);
    }

    /**
     * Fast forward the state of the xml stream reader to the first tag after the end tag
     * matching the current tag of the reader.
     *
     * @param reader            the reader
     * @param consumeEndTag     indicates whether or not the state of the reader should be
     *                          moved past the end tag or not.
     * @throws javax.xml.stream.XMLStreamException on error
     */
    public static void fastForwardToEndTag(XMLStreamReader reader, boolean consumeEndTag) throws XMLStreamException
    {
        // special case - already at an end tag.
        int eventType = reader.getEventType();
        if (eventType == END_ELEMENT)
        {
            return;
        }
        if (eventType != START_ELEMENT)
        {
            throw new IllegalStateException("Expected reader to be at a start element, but instead found " + reader.getEventType());
        }

        Stack<String> tags = new Stack<String>();
        tags.push(reader.getLocalName());

        while (!tags.isEmpty())
        {
            eventType = reader.next();
            
            if (eventType == START_ELEMENT)
            {
                tags.push(reader.getLocalName());
            }
            else if (eventType == END_ELEMENT)
            {
                tags.pop();
            }
        }
        if (consumeEndTag && reader.getEventType() == END_ELEMENT)
        {
            // consume nodes until we reach the first tag after the end element.
            while (reader.hasNext())
            {
                reader.next();
                if (reader.isStartElement() || reader.isEndElement())
                {
                    break;
                }
            }
        }
    }

    /**
     * Find and move the cursor to the end element with the specified tag name.
     *
     * Note that for tags of the form <tagname/>, the cursor will remain on that tag, but
     * shift the eventType to be {@link javax.xml.stream.XMLStreamConstants#END_ELEMENT} 
     *
     * If the readers cursor is sitting on an end element of the required name, no change
     * in state is made.
     * 
     * @param reader    the reader being shifted
     * @param tagName   the name of the tag whose closing tag is being searched for
     * @return  true if the end tag is found, false otherwise.
     * 
     * @throws XMLStreamException on error.
     */
    public static boolean findNextEndTag(XMLStreamReader reader, String tagName) throws XMLStreamException
    {
        return findNextTag(reader, tagName, END_ELEMENT);
    }

    /**
     * Find and move the cursor to the start element with the specified tag name.
     *
     * If the readers cursor is sitting on a start element of the required name, no change
     * in state is made.
     *
     * @param reader    the reader being shifted
     * @param tagName   the name of the tag whose starting tag is being searched for
     * @return  true if the start tag is found, false otherwise.
     *
     * @throws XMLStreamException on error.
     */
    public static boolean findNextStartTag(XMLStreamReader reader, String tagName) throws XMLStreamException
    {
        return findNextTag(reader, tagName, START_ELEMENT);
    }

    private static boolean findNextTag(XMLStreamReader reader, String tagName, int tagType) throws XMLStreamException
    {
        int eventType = reader.getEventType();
        String localName = reader.getLocalName();
        if (eventType == tagType && localName.equals(tagName))
        {
            return true;
        }

        while (reader.hasNext())
        {
            eventType = reader.next();
            localName = reader.getLocalName();
            if (eventType == tagType && localName.equals(tagName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert the specified integer into the name of the associated XMLStreamConstants constant.
     *
     * @param xmlStreamConstant the integer to be mapped to the constants in the XMLStreamConstants object.
     * @return  the name of the constant, or "<unknown>" if it is not located.
     */
    public static String toString(int xmlStreamConstant)
    {
        try
        {
            Field[] fields = XMLStreamConstants.class.getDeclaredFields();
            for (Field field : fields)
            {
                if (xmlStreamConstant == (Integer)field.get(XMLStreamConstants.class))
                {
                    return field.getName();
                }
            }
        }
        catch (Exception e)
        {
            //
        }
        return "<unknown>";
    }
}
