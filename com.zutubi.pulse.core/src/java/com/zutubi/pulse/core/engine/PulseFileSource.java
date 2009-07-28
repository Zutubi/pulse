package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;

/**
 * The source of a pulse file, primarily the file contents but possibly also
 * other information.
 */
public interface PulseFileSource
{
    String getPath();
    String getFileContent(FileResolver resolver) throws Exception;
}
