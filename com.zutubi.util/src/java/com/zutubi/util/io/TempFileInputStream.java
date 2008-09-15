package com.zutubi.util.io;

import java.io.*;

/**
 * A file input stream that deletes the file when it is closed.
 */
public class TempFileInputStream extends FileInputStream
{
    private File file;

    public TempFileInputStream(String name) throws FileNotFoundException
    {
        super(name);
        file = new File(name);
    }

    public TempFileInputStream(File file) throws FileNotFoundException
    {
        super(file);
        this.file = file;
    }

    public TempFileInputStream(FileDescriptor fdObj)
    {
        super(fdObj);
        throw new UnsupportedOperationException("File name required for cleanup");
    }


    public void close() throws IOException
    {
        super.close();
        file.delete();
    }
}
