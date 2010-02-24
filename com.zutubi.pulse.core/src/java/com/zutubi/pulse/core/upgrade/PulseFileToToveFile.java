package com.zutubi.pulse.core.upgrade;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Helper class for converting 2.0 pulse files to 2.1 ones.  Separated out
 * for use in a standalone tool for converting versioned files.
 */
public class PulseFileToToveFile
{
    private static final String ELEMENT_COMMAND = "command";
    private static final String ELEMENT_CUSTOM_FIELDS = "custom-fields";
    private static final String ELEMENT_FIELD = "field";
    private static final String ELEMENT_RESOURCE = "resource";
    private static final String ELEMENT_VERSION = "version";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_SCOPE = "scope";
    private static final String ATTRIBUTE_VALUE = "value";

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

                List<Node> childElementsCopy = new LinkedList<Node>();
                boolean hasSignificantChild = false;
                for (int i = 0; i < element.getChildCount(); i++)
                {
                    Node child = element.getChild(i);
                    hasSignificantChild |= isChildSignificant(child, childCommand);
                    childElementsCopy.add(child);
                }

                // If the wrapping <command> contains only whitespace (aside
                // from the child command), there is no need to move its
                // contents into the child command.
                if (hasSignificantChild)
                {
                    int childCommandChildCount = childCommand.getChildCount();
                    int insertIndex = 0;
                    for (Node child: childElementsCopy)
                    {
                        if (child == childCommand)
                        {
                            insertIndex += childCommandChildCount;
                        }
                        else
                        {
                            element.removeChild(child);
                            childCommand.insertChild(child, insertIndex);
                            insertIndex++;
                        }
                    }
                }

                element.removeChild(childCommand);
                parentElement.replaceChild(element, childCommand);
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
            System.out.println("WARNING: Replacing <version> element from recipe '" + parentElement.getAttributeValue("name") + "' with a custom field.");
            System.out.println("WARNING: Consider using the new version field in project dependencies configuration instead.");

            element.setLocalName(ELEMENT_CUSTOM_FIELDS);
            element.addAttribute(new Attribute(ATTRIBUTE_NAME, "record-version"));
            Attribute valueAttribute = element.getAttribute(ATTRIBUTE_VALUE);
            element.removeAttribute(valueAttribute);

            Element fieldElement = new Element(ELEMENT_FIELD);
            fieldElement.addAttribute(new Attribute(ATTRIBUTE_NAME, "version"));
            fieldElement.addAttribute(new Attribute(ATTRIBUTE_SCOPE, "build"));
            fieldElement.addAttribute(new Attribute(ATTRIBUTE_VALUE, valueAttribute.getValue()));
            element.appendChild(fieldElement);
        }
        else
        {
            processChildren(element, warningIssued);
        }
    }

    private static boolean isChildSignificant(Node child, Element ignoreElement)
    {
        if (child instanceof Text)
        {
            Text text = (Text) child;
            return StringUtils.trimmedStringSet(text.getValue());
        }
        else
        {
            return child != ignoreElement;
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
