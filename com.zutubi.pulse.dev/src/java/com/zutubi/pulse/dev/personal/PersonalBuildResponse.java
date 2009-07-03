package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.util.XMLUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Models a response to a personal build from the server.
 */
public class PersonalBuildResponse
{
    private static final String ATTRIBUTE_NUMBER = "number";
    private static final String ELEMENT_ERROR = "error";
    private static final String ELEMENT_WARNING = "warning";

    private long number;
    private List<String> errors = new LinkedList<String>();
    private List<String> warnings = new LinkedList<String>();


    public static PersonalBuildResponse parse(InputStream in) throws IOException, ParsingException
    {
        Document document = XMLUtils.streamToDoc(in);
        Element root = document.getRootElement();

        PersonalBuildResponse response = new PersonalBuildResponse();
        String numberString = XMLUtils.getRequiredAttribute(root, ATTRIBUTE_NUMBER);
        try
        {
            response.number = Long.parseLong(numberString);
        }
        catch (NumberFormatException e)
        {
            throw new ParsingException("Build number returned '" + numberString + "' is invalid");
        }

        readMessages(root, ELEMENT_ERROR, response.getErrors());
        readMessages(root, ELEMENT_WARNING, response.getWarnings());
        return response;
    }

    private static void readMessages(Element element, String childName, List<String> messages)
    {
        Elements elements = element.getChildElements(childName);
        for (int i = 0; i < elements.size(); i++)
        {
            String s = XMLUtils.getText(elements.get(i), "", true);
            if (s.length() > 0)
            {
                messages.add(s);
            }
        }
    }

    public boolean isSuccess()
    {
        return number > 0;
    }

    public long getNumber()
    {
        return number;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public List<String> getWarnings()
    {
        return warnings;
    }
}
