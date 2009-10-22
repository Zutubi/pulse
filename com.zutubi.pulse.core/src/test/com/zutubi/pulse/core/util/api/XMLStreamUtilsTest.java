package com.zutubi.pulse.core.util.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.IOUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Map;

public class XMLStreamUtilsTest extends PulseTestCase
{
    private XMLStreamReader reader;
    private InputStream input;

    @Override
    protected void tearDown() throws Exception
    {
        XMLStreamUtils.close(reader);
        IOUtils.close(input);

        super.tearDown();
    }

    public void testGetAttributes() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testGetAttributes");
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());

        Map<String, String> attributes = XMLStreamUtils.getAttributes(reader);
        assertEquals(3, attributes.size());
        assertEquals("1", attributes.get("a"));
        assertEquals("2", attributes.get("b"));
        assertEquals("3", attributes.get("c"));
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.nextTag());

        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());
        assertEquals("no-attributes", reader.getLocalName());
        
        attributes = XMLStreamUtils.getAttributes(reader);
        assertEquals(0, attributes.size());
    }

    public void testSkipElement() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testSkipElement");
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());

        // fast forward test on the <single/> element
        assertEquals("single", reader.getLocalName());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.getEventType());

        XMLStreamUtils.skipElement(reader);

        // fast forward test on the <text>text</text> element
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.getEventType());
        assertEquals("text", reader.getLocalName());

        XMLStreamUtils.skipElement(reader);

        // fast forward test on the <nested> element
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.getEventType());
        assertEquals("nested", reader.getLocalName());
        assertEquals("c", XMLStreamUtils.getAttributes(reader).get("name"));

        XMLStreamUtils.skipElement(reader, false);

        assertEquals("nested", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.getEventType());

        assertEquals(XMLStreamConstants.END_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getLocalName());
    }

    public void testSkipElementToEndOfDocument() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testSkipElement");
        assertEquals("root", reader.getLocalName());
        // fast forward to </root> and then attempt to consume it.
        XMLStreamUtils.skipElement(reader, true);
        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.getEventType());
    }

    public void testSkipElementDoesNotConsumeExcessEndTags() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testSkipElement");
        assertEquals("root", reader.getLocalName());

        XMLStreamUtils.findNextStartTag(reader, "nested");
        reader.nextTag(); // second nested.
        assertEquals("d", XMLStreamUtils.getAttributes(reader).get("name"));

        XMLStreamUtils.skipElement(reader, true);

        assertEquals(XMLStreamConstants.END_ELEMENT, reader.getEventType());
    }

    public void testFindNext() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testSkipElement");
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());

        assertTrue(XMLStreamUtils.findNextStartTag(reader, "nested"));
        assertEquals("c", XMLStreamUtils.getAttributes(reader).get("name"));
        // find next itself does not consume the tag.
        assertTrue(XMLStreamUtils.findNextStartTag(reader, "nested"));
        assertEquals("c", XMLStreamUtils.getAttributes(reader).get("name"));
        reader.nextTag();

        assertTrue(XMLStreamUtils.findNextStartTag(reader, "nested"));
        assertEquals("d", XMLStreamUtils.getAttributes(reader).get("name"));
        reader.nextTag();
        
        assertTrue(XMLStreamUtils.findNextStartTag(reader, "nested"));
        assertEquals("e", XMLStreamUtils.getAttributes(reader).get("name"));
        reader.nextTag();
        assertFalse(XMLStreamUtils.findNextStartTag(reader, "nested"));
    }

    public void testToString() throws IllegalAccessException
    {
        assertEquals("START_ELEMENT", XMLStreamUtils.toString(XMLStreamConstants.START_ELEMENT));
        assertEquals("ATTRIBUTE", XMLStreamUtils.toString(XMLStreamConstants.ATTRIBUTE));
        assertEquals("DTD", XMLStreamUtils.toString(XMLStreamConstants.DTD));
        assertEquals("NAMESPACE", XMLStreamUtils.toString(XMLStreamConstants.NAMESPACE));
    }

    public void testNextElementTraversesSiblings() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");
        assertEquals("root", reader.getLocalName());
        reader.next(); // ensure that we are 'inside' the root element.
        assertEquals(XMLStreamConstants.CHARACTERS, reader.getEventType());

        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("a", reader.getLocalName());
        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("b", reader.getLocalName());
        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("c", reader.getLocalName());
        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("d", reader.getLocalName());
        assertFalse(XMLStreamUtils.nextElement(reader));
    }

    public void testNextElementFromStartElement() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");
        assertEquals("root", reader.getLocalName());
        reader.next(); // ensure that we are 'inside' the root element.

        reader.next();
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.getEventType());

        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("b", reader.getLocalName());
    }

    public void testNextElementFromTextWithinElement() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");
        assertEquals("root", reader.getLocalName());
        reader.next(); // ensure that we are 'inside' the root element.

        reader.next();
        reader.next();
        assertEquals(XMLStreamConstants.CHARACTERS, reader.getEventType());

        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("b", reader.getLocalName());
    }

    public void testNextElementFromEndElement() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");
        assertEquals("root", reader.getLocalName());
        reader.next(); // ensure that we are 'inside' the root element.

        reader.next();
        reader.next();
        reader.next();
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.getEventType());

        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("b", reader.getLocalName());
    }

    public void testNextElementFromTextBetweenElements() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");
        assertEquals("root", reader.getLocalName());
        reader.next(); // ensure that we are 'inside' the root element.

        reader.next();
        reader.next();
        reader.next();
        reader.next();
        assertEquals(XMLStreamConstants.CHARACTERS, reader.getEventType());

        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("b", reader.getLocalName());
    }

    public void testNextElementFromTextStaysWithSiblings() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");
        assertEquals("root", reader.getLocalName());
        reader.next(); // ensure that we are 'inside' the root element.

        assertTrue(XMLStreamUtils.nextElement(reader));
        assertTrue(XMLStreamUtils.nextElement(reader));
        assertTrue(XMLStreamUtils.nextElement(reader));
        assertTrue(XMLStreamUtils.nextElement(reader));
        assertEquals("d", reader.getLocalName());
        reader.next();
        assertEquals(XMLStreamConstants.CHARACTERS, reader.getEventType());

        assertFalse(XMLStreamUtils.nextElement(reader));
    }

    private XMLStreamReader getXMLStreamReader(String testName) throws XMLStreamException
    {
        input = getInput(testName, "xml");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        reader = inputFactory.createXMLStreamReader(input);

        // start at the root node.
        reader.nextTag();
        
        return reader;
    }
}
