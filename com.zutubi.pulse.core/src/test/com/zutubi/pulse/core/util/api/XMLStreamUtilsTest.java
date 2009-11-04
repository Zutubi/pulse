package com.zutubi.pulse.core.util.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.getAttributes;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.skipElement;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.nextElement;
import com.zutubi.util.io.IOUtils;

import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.*;
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
        assertEquals(START_ELEMENT, reader.getEventType());

        Map<String, String> attributes = getAttributes(reader);
        assertEquals(3, attributes.size());
        assertEquals("1", attributes.get("a"));
        assertEquals("2", attributes.get("b"));
        assertEquals("3", attributes.get("c"));
        assertEquals(END_ELEMENT, reader.nextTag());

        assertEquals(START_ELEMENT, reader.nextTag());
        assertEquals("no-attributes", reader.getLocalName());

        attributes = getAttributes(reader);
        assertEquals(0, attributes.size());
    }

    public void testSkipElement() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testSkipElement");

        assertEquals("single", reader.getLocalName());
        assertEquals(START_ELEMENT, reader.getEventType());

        skipElement(reader);

        assertEquals(END_ELEMENT, reader.getEventType());
        assertEquals(START_ELEMENT, reader.nextTag());
        assertEquals("text", reader.getLocalName());

        skipElement(reader);

        assertEquals(END_ELEMENT, reader.getEventType());
        assertEquals(START_ELEMENT, reader.nextTag());
        assertEquals("nested", reader.getLocalName());
        assertEquals("c", getAttributes(reader).get("name"));

        skipElement(reader);

        assertEquals("nested", reader.getLocalName());
        assertEquals(END_ELEMENT, reader.getEventType());

        assertEquals(END_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getLocalName());
    }

    public void testToString() throws IllegalAccessException
    {
        assertEquals("START_ELEMENT", XMLStreamUtils.toString(START_ELEMENT));
        assertEquals("ATTRIBUTE", XMLStreamUtils.toString(ATTRIBUTE));
        assertEquals("DTD", XMLStreamUtils.toString(DTD));
        assertEquals("NAMESPACE", XMLStreamUtils.toString(NAMESPACE));
    }

    public void testNextElement() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextElement");

        assertEquals(START_ELEMENT, reader.getEventType());
        assertEquals("a", reader.getLocalName());
        assertTrue(nextElement(reader));
        assertEquals("b", reader.getLocalName());
        assertTrue(nextElement(reader));
        assertEquals("c", reader.getLocalName());
        assertTrue(nextElement(reader));
        assertEquals("d", reader.getLocalName());
        assertFalse(nextElement(reader));
    }

    public void testExpectTag() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testExpect");

        assertEquals(START_ELEMENT, reader.getEventType());
        XMLStreamUtils.expectStartTag("a", reader);

        skipElement(reader);

        assertEquals(END_ELEMENT, reader.getEventType());
        XMLStreamUtils.expectEndTag("a", reader);

        assertEquals(START_ELEMENT, reader.nextTag());
        XMLStreamUtils.expectStartTag("b", reader);

        skipElement(reader);

        assertEquals(END_ELEMENT, reader.getEventType());
        XMLStreamUtils.expectEndTag("b", reader);
    }

    public void testReadElements() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testReadElements");

        Map<String, String> elements = XMLStreamUtils.readElements(reader);

        assertEquals(3, elements.size());
        assertTrue(elements.containsKey("a"));
        assertEquals("", elements.get("a"));
        assertTrue(elements.containsKey("b"));
        assertEquals("b", elements.get("b"));
        assertTrue(elements.containsKey("c"));
        assertEquals("", elements.get("c"));
    }

    public void testGetElementText() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testGetElementText");

        assertEquals("", XMLStreamUtils.getElementText(reader));
        reader.nextTag();
        assertEquals("blah", XMLStreamUtils.getElementText(reader));
        reader.nextTag();
        assertEquals("<element> blah </element>", XMLStreamUtils.getElementText(reader));
    }

    public void testNextSiblingTag() throws XMLStreamException
    {
        XMLStreamReader reader = getXMLStreamReader("testNextSiblingTag");
        assertFalse(XMLStreamUtils.nextSiblingTag(reader));
        assertTrue(reader.isEndElement());

        reader = getXMLStreamReader("testNextSiblingTag");
        assertFalse(XMLStreamUtils.nextSiblingTag(reader, "unknown", "tag"));
        assertTrue(reader.isEndElement());

        reader = getXMLStreamReader("testNextSiblingTag");
        assertTrue(XMLStreamUtils.nextSiblingTag(reader, "a"));
        assertTrue(XMLStreamUtils.isElement("a", reader));

        // need to manually move the cursor forwards now (simulating the processing of the loop)
        XMLStreamUtils.nextElement(reader);

        assertTrue(reader.isStartElement());
        assertFalse(XMLStreamUtils.nextSiblingTag(reader, "a"));
        assertTrue(reader.isEndElement());
    }

    private XMLStreamReader getXMLStreamReader(String testName) throws XMLStreamException
    {
        // close open readers before overwriting.
        XMLStreamUtils.close(reader);
        IOUtils.close(input);

        input = getInput(testName, "xml");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        reader = inputFactory.createXMLStreamReader(input);

        // start at the root node.
        reader.nextTag();
        assertEquals("root", reader.getLocalName());
        reader.nextTag();

        return reader;
    }
}
