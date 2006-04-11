package com.zutubi.pulse.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class LogDirectoryBuilder implements Builder
{
    private static final String LOGGING_DIR = ": Logging ";

    private List<String> listing = new LinkedList<String>();

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (line.indexOf(LOGGING_DIR) >= 0)
        {
            listing.add(line.substring(line.indexOf(LOGGING_DIR) + LOGGING_DIR.length()).trim());
        }
    }

    public void parseEnhancedMessage(String key, Object value)
    {

    }

    public void outputDone()
    {

    }

    public List<String> getDirectories()
    {
        return listing;
    }
}
