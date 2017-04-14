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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.util.io.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Utilities shared between Maven 1 and Maven 2 code.
 */
public class MavenUtils
{
    public static String extractVersion(File pom, String versionElementName) throws PulseException
    {
        // Load the pom and look for a top-level version element.
        FileInputStream input = null;
        try
        {
            input = new FileInputStream(pom);
            Builder builder = new Builder();
            Document doc = builder.build(input);

            Elements elements = doc.getRootElement().getChildElements();
            for(int i = 0; i < elements.size(); i++)
            {
                Element element = elements.get(i);
                if(element.getLocalName().equals(versionElementName) && element.getChildCount() > 0)
                {
                    Node child = element.getChild(0);
                    if(child != null && child instanceof Text)
                    {
                        // Finally, the version
                        return (child.getValue().trim());
                    }
                }
            }
        }
        catch (ParsingException pex)
        {
            String message = "Unable to parse Maven POM file '" + pom.getAbsolutePath() + "' to extract version";
            if(pex.getMessage() != null)
            {
                message += ": " + pex.getMessage();
            }
            throw new PulseException(message);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        catch (IOException e)
        {
            String message = "I/O error extracting version from Maven POM file '" + pom.getAbsolutePath() + "'";
            if(e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }
            throw new PulseException(message);
        }
        finally
        {
            IOUtils.close(input);
        }
        return null;
    }
}
