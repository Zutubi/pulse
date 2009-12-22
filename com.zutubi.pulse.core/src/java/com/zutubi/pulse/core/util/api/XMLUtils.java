package com.zutubi.pulse.core.util.api;

import com.opensymphony.util.TextUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.*;

/**
 * Static utility methods for working with XOM XML APIs.
 */
public class XMLUtils
{
    /**
     * Parses an XML document from an input stream.
     *
     * @param in the input stream, which will be closed by this method
     * @return the parsed document
     * @throws IOException if there is an error reading from the stream
     * @throws ParsingException if the document is not valid
     */
    public static Document streamToDoc(InputStream in) throws IOException, ParsingException
    {
        try
        {
            Builder builder = new Builder();
            return builder.build(in);
        }
        finally
        {
            IOUtils.close(in);
        }
    }

    /**
     * Parses an XML document from a file.
     *
     * @param file the file to parse
     * @return the parsed document
     * @throws IOException if there is an error reading from the file
     * @throws ParsingException if the document is not valid
     */
    public static Document readDocument(File file) throws IOException, ParsingException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(file);
            return streamToDoc(is);
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    /**
     * Writes the given document out to the given file.
     *
     * @param file file to write the document to
     * @param doc  document to write
     * @throws IOException if there is an error writing to the file
     */
    public static void writeDocument(File file, Document doc) throws IOException
    {
        writeDocument(file, doc, false);
    }

    /**
     * Writes the given document out to the given file, optionally indenting it
     * to show its structure with four-space indents.
     *
     * @param file        file to write the document to
     * @param doc         document to write
     * @param prettyPrint if true, apply four-space indenting to the XML to
     *                    show its structure
     * @throws IOException if there is an error writing to the file
     */
    public static void writeDocument(File file, Document doc, boolean prettyPrint) throws IOException
    {
        BufferedOutputStream bos = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            if(prettyPrint)
            {
                serializer.setIndent(4);
            }
            serializer.write(doc);
        }
        finally
        {
            IOUtils.close(bos);
        }
    }

    /**
     * Attempt to produce a human-friendly version of the given XML string.
     * The string should contain a complete XML file.  The pretty version will
     * have indentation (four spaces deep) showing the structure of the XML.
     * If an error occurs during processing, the input string will be returned
     * unchanged.
     *
     * @param xmlFile a complete XML file to pretty print
     * @return a pretty-printed version of the input XML, indented to
     *         illustrate the XML structure
     */
    public static String prettyPrint(String xmlFile)
    {
        Builder builder = new Builder();
        ByteArrayOutputStream os = null;

        try
        {
            Document doc = builder.build(new StringReader(xmlFile));
            os = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(os);
            serializer.setIndent(4);
            serializer.write(doc);
            serializer.flush();
            return os.toString();
        }
        catch (Exception e)
        {
            // Try our best to pretty print, but not fatal if we can't
            return xmlFile;
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    /**
     * Gets all text content nested within the given element, by finding all
     * text nodes, trimming their values of leading and trailing whitespace,
     * and concatentating the result.
     *
     * @param element      element to get the text content from
     * @return all text content nested directly within the given element, or
     *         null if the given element has no children
     */
    public static String getText(Element element)
    {
        return getText(element, null);
    }

    /**
     * Gets all text content nested within the given element, by finding all
     * text nodes, trimming their values of leading and trailing whitespace,
     * and concatentating the result.  If the node has no children, the default
     * value is returned.
     *
     * @param element      element to get the text content from
     * @param defaultValue string to return if the given element has no child
     *                     nodes
     * @return all text content nested directly within the given element
     */
    public static String getText(Element element, String defaultValue)
    {
        return getText(element, defaultValue, true);   
    }

    /**
     * Gets all text content nested within the given element, by finding all
     * text nodes and concatenating their values.  If the node has no children,
     * the default value is returned.  The content of each text node may
     * optionally be trimmed of leading and tailing whitespace.
     *
     * @param element      element to get the text content from
     * @param defaultValue string to return if the given element has no child
     *                     nodes
     * @param trim         if true, the content from each text node will be
     *                     trimmed before it is added to the result
     * @return all text content nested directly within the given element
     */
    public static String getText(Element element, String defaultValue, boolean trim)
    {
        if (element.getChildCount() > 0)
        {
            Node child = element.getChild(0);
            if(child != null && child instanceof Text)
            {
                String value = child.getValue();
                if(trim)
                {
                    value = value.trim();
                }

                return value;
            }
        }

        return defaultValue;
    }

    /**
     * Gets text content for the given element using {@link #getText(nu.xom.Element, String, boolean)},
     * and ensures that a non-null, non-empty string is returned.
     *
     * @param element element to get the text content from
     * @param trim    if true, the content from each text node will be trimmed
     *                before it is added to the result
     * @return all text content nested directly within the given element
     * @throws XMLException if no text is found nested in the element
     */
    public static String getRequiredText(Element element, boolean trim)
    {
        String text = getText(element, null, trim);
        if(!TextUtils.stringSet(text))
        {
            throw new XMLException("Required text missing from element '" + element.getLocalName() + "'");
        }

        return text;
    }

    /**
     * Finds the first child element of the given element with the given name
     * and gets text content for the child using {@link #getText(nu.xom.Element, String)}.
     * If the child is not found or has no text content, the default value is
     * returned.
     *
     * @param element      element to find the child element within
     * @param childName    name of the child element to find
     * @param defaultValue value to return if the child element is not found or
     *                     has no text content
     * @return all text content nested directly within the first child with the
     *         given name under the given element
     */
    public static String getChildText(Element element, String childName, String defaultValue)
    {
        Element child = element.getFirstChildElement(childName);
        if(child == null)
        {
            return defaultValue;
        }

        return getText(child, defaultValue);
    }

    /**
     * Finds the first child element of the given element with the given name
     * and gets text content for the child using {@link #getRequiredText(nu.xom.Element, boolean)}.
     * If the child is not found or has no text content, an error is raised.
     *
     * @param element      element to find the child element within
     * @param childName    name of the child element to find
     * @return all text content nested directly within the first child with the
     *         given name under the given element
     * @throws XMLException if no child is found with the given name or the
     *         child has no text content
     */
    public static String getRequiredChildText(Element element, String childName, boolean trim)
    {
        Element child = getRequiredChild(element, childName);
        return getRequiredText(child, trim);
    }

    /**
     * Gets the value of the given attribute from the given element, throwing
     * an error if the attribute is not found.
     *
     * @param element   element to get the attribute from
     * @param attribute name of the attribute to get the value of
     * @return the value of the given attribute
     * @throws XMLException if the given attribute is not found
     */
    public static String getRequiredAttributeValue(Element element, String attribute)
    {
        String result = element.getAttributeValue(attribute);
        if (result == null)
        {
            throw new XMLException("Invalid " + element.getLocalName() + ": missing required attribute '" + attribute + "'");
        }

        return result;
    }

    /**
     * Gets the value of the given attribute from the given element, returning
     * a default value if the attribute is not found.
     *
     * @param element      element to get the attribute from
     * @param attribute    name of the attribute to get the value of
     * @param defaultValue value to reutrn if the attribute is not found
     * @return the value of the given attribute, or the default if the
     *         attribute is not found
     */
    public static String getAttributeValue(Element element, String attribute, String defaultValue)
    {
        String result = element.getAttributeValue(attribute);
        if (result == null)
        {
            return defaultValue;
        }

        return result;
    }

    /**
     * Gets the first child of the given element with the given name, raising
     * an error if no such child exists.
     *
     * @param element   the element to find the child of
     * @param childName name of the child element to find
     * @return the first child element of the given name
     * @throws XMLException if no child element with the given name is found
     */
    public static Element getRequiredChild(Element element, String childName)
    {
        Element child = element.getFirstChildElement(childName);
        if(child == null)
        {
            throw new XMLException("Required child element '" + childName + "' not found for element '" + element.getLocalName() + "'");
        }

        return child;
    }

    /**
     * Runs a procedure over every child element of the given element with the
     * given name.
     *
     * @param element   element to iterate over the children of
     * @param childName name of the child elements to process
     * @param fn        procedure to run on every child element
     */
    public static void forEachChild(Element element, String childName, UnaryProcedure<Element> fn)
    {
        Elements elements = element.getChildElements(childName);
        for(int i = 0; i < elements.size(); i++)
        {
            fn.run(elements.get(i));
        }
    }

    /**
     * Removes any occurences of illegal characters (i.e. characters for which
     * {@link #isXMLCharacter(int)} returns false) from the given string and
     * returns the result.
     *
     * @param s the string to remove illegal characters from
     * @return the input string with any illegal characters removed
     */
    public static String removeIllegalCharacters(String s)
    {
        StringBuilder builder = null;
        for(int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if(!isXMLCharacter(c))
            {
                if(builder == null)
                {
                    builder = new StringBuilder(s.length());
                    builder.append(s, 0, i);
                }
            }
            else if(builder != null)
            {
                builder.append(c);
            }
        }

        return builder == null ? s : builder.toString();
    }

    /**
     * Tests if the given character can validly appear in an XML document.
     * Certain control characters, for example, cannot legally appear in XML.
     *
     * @param c the character to test
     * @return true if the given character can appear in XML content, false
     *         otherwise
     */
    public static boolean isXMLCharacter(int c)
    {
        if (c < 0x20)
        {
            return c == '\n' || c == '\r' || c == '\t';
        }

        return c <= 0xD7FF || c >= 0xE000 && (c <= 0xFFFD || c >= 0x10000 && c <= 0x10FFFF);
    }

    /**
     * Escapes XML special characters in the given string, converting it to a
     * form suitable for direct inclusion in an XML document.  For example,
     * &lt; will be replaced with &amp;lt;.
     *
     * @param s the string to escape
     * @return the string with all XML special characters escaped
     * @throws IllegalCharacterDataException if the input string contains
     *         characters that cannot be represented in XML
     */
    public static String escape(String s)
    {
        return new Text(s).toXML();
    }
}
