package com.zutubi.pulse.core.util.api;

import com.zutubi.util.logging.Logger;

import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
     * Move the xml stream readers cursor to the first element after the end tag that matches the
     * current start tag (ie: effectively skip the current element).
     *
     * This is equivalent to calling {@link #skipElement(javax.xml.stream.XMLStreamReader, boolean)}
     * with consumeEndTag set to true.
     *
     * @param reader    the reader
     * @throws javax.xml.stream.XMLStreamException on error
     */
    public static void skipElement(XMLStreamReader reader) throws XMLStreamException
    {
        skipElement(reader, true);
    }

    /**
     * Move the xml stream readers cursor to the end tag that matches the
     * current start tag (ie: effectively skip the current element), optionally
     * consuming the end tag.
     *
     * @param reader            the reader
     * @param consumeEndTag     indicates whether or not the state of the reader should be
     *                          moved past the end tag or not.
     * @throws javax.xml.stream.XMLStreamException on error
     */
    public static void skipElement(XMLStreamReader reader, boolean consumeEndTag) throws XMLStreamException
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

    /**
     * Move the readers cursor to the start tag of the next element, returning true if
     * a new element is located, false otherwise.
     *
     * Note that this operation will skip over any nested elements, but not 'skip out of'
     * the current element depth within the xml document.
     *
     * SPECIAL CASE: If we are within the text of an element, this method will step outside
     * of the current element and then on to the next element.
     *
     * SPECIAL CASE 2: If we are at the start of the document, this method will step to
     * the first element in the document.
     *
     * @param reader    the reader being shifted.
     * @return true if a new element is encountered, false otherwise.
     *
     * @throws XMLStreamException on error.
     */
    public static boolean nextElement(XMLStreamReader reader) throws XMLStreamException
    {
        if (reader.isStartElement())
        {
            skipElement(reader, false);

            reader.nextTag();
        }
        else if (reader.isEndElement())
        {
            reader.nextTag();
        }
        else if (reader.isCharacters())
        {
            reader.nextTag();

            if (reader.isEndElement())
            {
                reader.nextTag();
            }
        }
        else if (reader.getEventType() == XMLStreamConstants.PROCESSING_INSTRUCTION ||
                reader.getEventType() == XMLStreamConstants.START_DOCUMENT ||
                reader.getEventType() == XMLStreamConstants.DTD)
        {
            while (!reader.isStartElement())
            {
                reader.next();
            }
        }

        return (reader.isStartElement());
    }

    /**
     * Throws an XMLStreamException if the element at the current cursor location
     * of the stream reader is not a start element and if the name does not match
     * the specified element name.
     *
     * @param elementName   the expected element name
     * @param reader        the xml stream reader whose cursor location is to be checked.
     *
     * @throws XMLStreamException if either of the expected conditions are not satisfied.
     */
    public static void expectStartElement(String elementName, XMLStreamReader reader) throws XMLStreamException
    {
        if (!reader.getLocalName().equals(elementName) || !reader.isStartElement())
        {
            throw new XMLStreamException("Expected " + toString(XMLStreamConstants.START_ELEMENT) + ":" +
                    elementName + ", instead found " + toString(reader.getEventType()) + ":" +
                    reader.getLocalName() + " at " + reader.getLocation());
        }
    }

    /**
     * Throws an XMLStreamException if the element at the current cursor location
     * of the stream reader is not an end element and if the name does not match
     * the specified element name.
     *
     * @param elementName   the expected element name
     * @param reader        the xml stream reader whose cursor location is to be checked.
     *
     * @throws XMLStreamException if either of the expected conditions are not satisfied.
     */
    public static void expectEndElement(String elementName, XMLStreamReader reader) throws XMLStreamException
    {
        if (!isElement(elementName, reader) || !reader.isEndElement())
        {
            throw new XMLStreamException("Expected " + toString(XMLStreamConstants.END_ELEMENT) + ":" +
                    elementName + ", instead found " + toString(reader.getEventType()) + ":" +
                    reader.getLocalName() + " at " + reader.getLocation());
        }
    }

    /**
     *
     * @param reader    the xml stream reader that provides the element data.
     * @return a map of keys that represent the element names, and values that represent
     * the elements text value.
     *
     * @throws XMLStreamException on error or if a nested element is encountered.
     */
    public static Map<String, String> readElements(XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> elements = new HashMap<String, String>();

        while (reader.isStartElement())
        {
            String name = reader.getLocalName();
            String text = reader.getElementText();

            elements.put(name, text);

            reader.nextTag();
        }

        return elements;
    }

    /**
     * Returns true if the name of the element at the readers cursor matches the
     * specified name.
     *
     * @param elementName   the element name to match
     * @param reader        the reader whose cursor location is being evaluated.
     * @return  true if the specified element name matches the current cursor location
     * in the reader, false otherwise.
     */
    public static boolean isElement(String elementName, XMLStreamReader reader)
    {
        return reader.getLocalName().equals(elementName);
    }

    /**
     * Returns the trimmed element text, or the default value if no text is available.
     *
     * @param reader        the reader from which we are reading the element text.
     * @param defaultValue  the default value to be returned if no element text is available.
     * @return the element text, or the default value if no text is available.
     * @throws XMLStreamException on error
     * @see javax.xml.stream.XMLStreamReader#getElementText()
     */
    public static String getElementText(XMLStreamReader reader, String defaultValue) throws XMLStreamException
    {
        String text = reader.getElementText();
        if (text == null)
        {
            return defaultValue;
        }
        return text.trim();
    }
}
