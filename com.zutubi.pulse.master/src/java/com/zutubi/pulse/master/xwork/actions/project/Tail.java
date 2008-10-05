package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.CircularBuffer;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 *
 */
public class Tail
{
    private static final int DEFAULT_LINE_COUNT = 10;

    private int maxLines = DEFAULT_LINE_COUNT;

    private File file;

    public void setFile(File file)
    {
        this.file = file;
    }

    public void setMaxLines(int maxLines)
    {
        this.maxLines = maxLines;
    }

    public String getTail() throws IOException
    {
        final int MAX_BYTES = 500 * maxLines;

        RandomAccessFile raf = null;

        try
        {
            raf = new RandomAccessFile(file, "r");
            long length = raf.length();
            if (length > 0)
            {
                if (length > MAX_BYTES)
                {
                    raf.seek(length - MAX_BYTES);
                    length = MAX_BYTES;

                    // Discard the next (possibly partial) line
                    raf.readLine();
                }

                CircularBuffer<String> buffer = new CircularBuffer<String>(maxLines);
                String line = raf.readLine();
                while (line != null)
                {
                    buffer.append(line);
                    line = raf.readLine();
                }

                StringBuilder builder = new StringBuilder((int) length);
                for (String l : buffer)
                {
                    builder.append(l);
                    builder.append('\n');
                }

                return builder.toString();
            }
            
            return "";
        }
        finally
        {
            IOUtils.close(raf);
        }
    }

}
