package com.zutubi.pulse.core.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.command.Builder;

import java.util.List;
import java.util.LinkedList;

/**
 * 
 */
public class RlsBuilder implements Builder
{
    private static final String LISTING_MODULE_TAG = "Listing module:";

    private String repositoryPath;

    private String[] paths;
    
    private List<RlsInfo> listing = new LinkedList<RlsInfo>();
    private static final String DIRECTORY_TAG = "D";
    private static final String FILE_TAG = "/";

    /**
     * The repository paths that are being listed.
     *
     * @param paths that are being listed for which this builder is collating the response
     */
    public void setPaths(String... paths)
    {
        this.paths = paths;
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (line.contains(LISTING_MODULE_TAG))
        {
            // Line is expected to be of the following format:
            // cvs rls: Listing module: `integration-test/project/src/com'
            for (String path : paths)
            {
                if (line.contains(path))
                {
                    repositoryPath = path;
                    break;
                }
            }
        }
        else
        {
            if (line.startsWith(DIRECTORY_TAG))
            {
                // Line is expected to be of the following format:
                // D/CVSROOT////
                String name = line.substring(2);
                name = name.substring(0, name.indexOf('/'));
                listing.add(new RlsInfo(repositoryPath, name, true));
            }
            else if (line.startsWith(FILE_TAG))
            {
                // Line is expected to be of the following format:
                // /Com.java/1.1/Sun Jul 29 12:35:08 2007//  - listing a directory
                // /integration-test/project/src/com/Com.java/1.1/Sun Jul 29 12:35:08 2007// - listing a file


                if (line.contains(repositoryPath))
                {
                    // The only time that the repository path for the listing is contained within
                    // the line is if we are listing a file.
                    String name = repositoryPath.substring(repositoryPath.lastIndexOf('/') + 1);
                    String path = repositoryPath.substring(0, repositoryPath.lastIndexOf('/'));
                    listing.add(new RlsInfo(path, name, false));
                }
                else
                {
                    // work our way in from the back.
                    // strip trailing '/'
                    line = line.substring(0, line.length() - 1);
                    int index = line.lastIndexOf('/');
                    String lastSegment = line.substring(index + 1);
                    line = line.substring(0, index);
                    index = line.lastIndexOf('/');
                    String dateSegment = line.substring(index + 1);
                    line = line.substring(0, index);
                    index = line.lastIndexOf('/');
                    String revisionSegment = line.substring(index + 1);
                    line = line.substring(1, index); // trim the leading '/' as well.
                    listing.add(new RlsInfo(repositoryPath, line, false));
                }
            }
        }
    }

    public void parseEnhancedMessage(String key, Object value)
    {
        // noop.
    }

    public void outputDone()
    {
        // noop.
    }

    public List<RlsInfo> getListing()
    {
        return listing;
    }
}
