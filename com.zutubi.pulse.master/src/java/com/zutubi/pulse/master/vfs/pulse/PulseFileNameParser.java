package com.zutubi.pulse.master.vfs.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileNameParser;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponentContext;

/**
 * <class comment/>
 */
public class PulseFileNameParser extends AbstractFileNameParser
{
    private static final PulseFileNameParser INSTANCE = new PulseFileNameParser();

    public static PulseFileNameParser getInstance()
    {
        return INSTANCE;
    }

    public PulseFileNameParser()
    {
    }

    public FileName parseUri(final VfsComponentContext context, final FileName base, final String filename) throws FileSystemException
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

        // extract the instance and port details.

        // Decode and normalise the file name
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new PulseFileName(scheme, path, fileType);
    }
}
