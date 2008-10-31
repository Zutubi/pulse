package com.zutubi.pulse.master.vfs.provider.agent;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileNameParser;
import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponentContext;

/**
 * <class comment/>
 */
public class AgentFileNameParser extends AbstractFileNameParser
{
    private static final FileNameParser INSTANCE = new AgentFileNameParser();

    public static FileNameParser getInstance()
    {
        return INSTANCE;
    }

    public AgentFileName parseUri(final VfsComponentContext context, final FileName base, final String filename) throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();
        name.append(filename);

        final String scheme = UriParser.extractScheme(filename, name);

        // Expecting "//"
        if (name.length() < 2 || name.charAt(0) != '/' || name.charAt(1) != '/')
        {
            throw new FileSystemException("vfs.provider/missing-double-slashes.error", filename);
        }
        name.delete(0, 2);

        // extract agent name.
        final String address = extractRemoteAddress(name);

        // Decode and normalise the file name
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new AgentFileName(scheme, address, path, fileType);
    }

    private String extractRemoteAddress(StringBuffer name)
    {
        final int maxlen = name.length();
        for (int pos = 0; pos < maxlen; pos++)
        {
            final char ch = name.charAt(pos);
            if (ch == '/')
            {
                // Found the end of the user info
                String address = name.substring(0, pos);
                name.delete(0, pos);
                return address;
            }
        }

        // if no / is present, then take the entire name.
        String address = name.toString();
        name.delete(0, name.length());
        return address;
    }
}
