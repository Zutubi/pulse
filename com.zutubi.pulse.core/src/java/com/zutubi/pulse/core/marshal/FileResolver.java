package com.zutubi.pulse.core.marshal;

import java.io.InputStream;

/**
 * Interface for resolution of a file path to an input stream yielding that
 * file's contents.
 */
public interface FileResolver
{
    /**
     * Resolves a path to a file to a stream that yields the file content.
     * The returned stream must be closed by the caller.
     *
     * @param path path of the file to resolve
     * @return an input stream open at the start of the file content
     * @throws Exception on any error
     */
    InputStream resolve(String path) throws Exception;
}
