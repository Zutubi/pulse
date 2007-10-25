package com.zutubi.prototype.type.record.store;

import java.io.File;
import java.io.IOException;

/**
 * A simple file system interface used by the file system record store to allow the
 * underlying file system to be replaced during testing. 
 *
 */
public interface FS
{
    boolean exists(File file);

    boolean createNewFile(File file) throws IOException;

    boolean mkdirs(File file);

    boolean delete(File file);

    boolean renameTo(File source, File destination);
}
