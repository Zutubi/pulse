package com.zutubi.util.io;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import java.io.*;
import java.util.Arrays;

/**
 * An input stream implementation that reads a seamless stream from
 * multiple files.
 *
 * NOTE: skipping and marking are not presently supported.
 */
public class MultipleFileInputStream extends InputStream
{
    private File[] files = null;
    private long[] sizes = null;

    /**
     * The index of the file currently being read.
     */
    private int index = 0;

    /**
     * The open file input stream for the current file being read.
     */
    private FileInputStream fis;

    /**
     * Create a new multiple file inputstream based on the specified files.  The files will be read
     * in the order in which they appear in the array.
     *
     * @param files the array of files to be read.
     *
     * @throws IOException if any of the files are not readable files.
     */
    public MultipleFileInputStream(File... files) throws IOException
    {
        // filter nulls.
        this.files = Iterables.toArray(Iterables.filter(Arrays.asList(files), Predicates.notNull()), File.class);

        this.sizes = new long[this.files.length];
        for (int i = 0; i < this.files.length; i++)
        {
            File f = this.files[i];
            if (!f.isFile())
            {
                throw new FileNotFoundException(f.getCanonicalPath() + " is not a file.");
            }
            sizes[i] = f.length();
        }

        if (this.files.length > 0)
        {
            fis = new FileInputStream(this.files[index]);
        }
    }

    public int read() throws IOException
    {
        if (fis == null)
        {
            return -1;
        }
        
        int c = fis.read();
        while (c == -1)
        {
            if (!openNextFile())
            {
                break;
            }

            c = fis.read();
        }
        
        return c;
    }

    public int read(byte b[]) throws IOException
    {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException
    {
        // already past the end of the files.
        if (fis == null)
        {
            return -1;
        }
        
        int read = fis.read(b, off, len);
        while (read == -1)
        {
            if (!openNextFile())
            {
                break;
            }

            read = fis.read(b, off, len);
        }

        if (read == -1)
        {
            return read;
        }

        while (read != len)
        {
            // open next file and read some more until the buffer is full.
            int newOff = off + read;
            int newLen = len - read;

            int newread = fis.read(b, newOff, newLen);
            if (newread != -1)
            {
                read = read + newread;
            }
            else
            {
                if (!openNextFile())
                {
                    return read;
                }
            }
        }

        return read;
    }

    public long skip(long n) throws IOException
    {
        return 0;
    }

    public int available() throws IOException
    {
        if (fis == null)
        {
            return -1;
        }

        long remaining = 0;
        for (int i = index + 1; i < sizes.length; i++)
        {
            remaining = remaining + sizes[i];
        }

        remaining = remaining + fis.available();
        if (remaining > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }

        return (int)remaining;
    }

    public void close() throws IOException
    {
        IOUtils.close(fis);
    }

    public synchronized void mark(int readlimit)
    {
    }

    public synchronized void reset() throws IOException
    {
    }

    public boolean markSupported()
    {
        return false;
    }

    private boolean openNextFile() throws FileNotFoundException
    {
        IOUtils.close(fis);
        fis = null;

        index++;

        if (index < files.length)
        {
            fis = new FileInputStream(files[index]);
            return true;
        }
        else
        {
            return false;
        }
    }
}
