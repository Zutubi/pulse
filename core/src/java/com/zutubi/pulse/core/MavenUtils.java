package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import nu.xom.*;

/**
 * Utilities shared between Maven 1 and Maven 2 code.
 */
public class MavenUtils
{
    public static void extractVersion(CommandContext context, CommandResult result, File pom, String versionElementName)
    {
        BuildContext buildContext = context.getBuildContext();
        if(buildContext != null)
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
                            buildContext.setBuildVersion(child.getValue().trim());
                            break;
                        }
                    }
                }
            }
            catch (ParsingException pex)
            {
                String message = "Unable to parse extract Maven POM file '" + pom.getAbsolutePath() + "' to extract version";
                if(pex.getMessage() != null)
                {
                    message += ": " + pex.getMessage();
                }

                result.warning(message);
            }
            catch (IOException e)
            {
                String message = "I/O error extracting version from Maven POM file '" + pom.getAbsolutePath() + "'";
                if(e.getMessage() != null)
                {
                    message += ": " + e.getMessage();
                }
                result.warning(message);
            }
            finally
            {
                IOUtils.close(input);
            }
        }
    }
}
