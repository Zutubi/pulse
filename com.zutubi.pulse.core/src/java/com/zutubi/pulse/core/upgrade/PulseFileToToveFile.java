package com.zutubi.pulse.core.upgrade;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

/**
 * Helper class for converting 2.0 pulse files to 2.1 ones.  Separated out
 * for use in a standalone tool for converting versioned files.
 */
public class PulseFileToToveFile
{
    private static final String ELEMENT_COMMAND = "command";
    private static final String ELEMENT_RESOURCE = "resource";
    private static final String ELEMENT_VERSION = "version";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_VERSION = "version";

    private static final Set<String> COMMAND_ELEMENTS = CollectionUtils.asSet(
            "ant",
            "bjam",
            "executable",
            "print",
            "sleep",
            "make",
            "maven",
            "maven2",
            "msbuild",
            "xcodebuild"
    );

    public static String convert(String pulseFile) throws IOException, ParsingException
    {
        Builder builder = new Builder();
        Document document = builder.build(new StringReader(pulseFile));
        return convert(document);
    }

    public static String convert(InputStream pulseFile) throws IOException, ParsingException
    {
        try
        {
            Builder builder = new Builder();
            Document document = builder.build(pulseFile);
            return convert(document);
        }
        finally
        {
            IOUtils.close(pulseFile);
        }
    }

    private static String convert(Document document) throws IOException
    {
        boolean[] warningIssued = new boolean[]{false};
        processElement(null, document.getRootElement(), warningIssued);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer serializer = new Serializer(baos);
        serializer.setIndent(4);
        serializer.write(document);

        return new String(baos.toByteArray());
    }

    private static void processElement(Element parentElement, Element element, boolean[] warningIssued)
    {
        String localName = element.getLocalName();
        if (localName.equals(ELEMENT_COMMAND))
        {
            String name = element.getAttributeValue(ATTRIBUTE_NAME);
            Element childCommand = findChildCommand(element);
            if (childCommand != null)
            {
                if (childCommand.getAttributeValue(ATTRIBUTE_NAME) == null && name != null)
                {
                    childCommand.addAttribute(new Attribute(ATTRIBUTE_NAME, name));
                }

                Elements childElements = element.getChildElements();
                for (int i = 0; i < childElements.size(); i++)
                {
                    Element child = childElements.get(i);
                    if (child != childCommand)
                    {
                        element.removeChild(child);
                        childCommand.appendChild(child);
                    }
                }

                element.removeChild(childCommand);
                parentElement.removeChild(element);
                parentElement.appendChild(childCommand);
                processChildren(childCommand, warningIssued);
            }
        }
        else if (localName.equals(ELEMENT_RESOURCE))
        {
            if (!warningIssued[0])
            {
                System.out.println("WARNING: Removing <resource> tag as importing resources in the pulse file is no longer supported.");
                System.out.println("WARNING: Use resource requirements in the project/build stage configuration instead.");
                warningIssued[0] = true;
            }
            parentElement.removeChild(element);
        }
        else if (localName.equals(ELEMENT_VERSION))
        {
            String value = element.getAttributeValue(ATTRIBUTE_VALUE);
            if (StringUtils.stringSet(value))
            {
                parentElement.addAttribute(new Attribute(ATTRIBUTE_VERSION, value));
            }
            parentElement.removeChild(element);
        }
        else
        {
            processChildren(element, warningIssued);
        }
    }

    private static void processChildren(Element element, boolean[] warningIssued)
    {
        Elements childElements = element.getChildElements();
        for (int i = 0; i < childElements.size(); i++)
        {
            processElement(element, childElements.get(i), warningIssued);
        }
    }

    private static Element findChildCommand(Element element)
    {
        Elements elements = element.getChildElements();
        for (int i = 0; i < elements.size(); i++)
        {
            Element child = elements.get(i);
            if (COMMAND_ELEMENTS.contains(child.getLocalName()))
            {
                return child;
            }
        }

        return null;
    }
}
