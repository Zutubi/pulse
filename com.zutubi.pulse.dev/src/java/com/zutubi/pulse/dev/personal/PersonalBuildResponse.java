/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.util.api.XMLUtils;
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
        String numberString = XMLUtils.getRequiredAttributeValue(root, ATTRIBUTE_NUMBER);
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
