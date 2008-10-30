package com.zutubi.util.io;

import com.zutubi.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation for efficiently reading lines from the end of a file, similar
 * to the unix tail utility.
 */
public class Tail
{
    static final int DEFAULT_LINE_COUNT = 10;
    static final int ESTIMATED_BYTES_PER_LINE = 500;

    private File file;
    private int maxLines;

    /**
     * Create tailer that will return a default number of lines from the end of
     * the given file.
     *
     * @param file the file to tail
     */
    public Tail(File file)
    {
        this(file, DEFAULT_LINE_COUNT);
    }

    /**
     * Create tailer that will return up to the given number of lines from the
     * end of the given file.
     *
     * @param file     the file to tail
     * @param maxLines the maximum number of lines to read
     */
    public Tail(File file, int maxLines)
    {
        this.file = file;
        this.maxLines = maxLines;
    }

    /**
     * Reads the tail of the file, up to our line limit, and returns the result
     * as a single string.  Line separators are normalised to newlines.
     * <p/>
     * This method may be called multiple times, each call will independently
     * open the file to read the lines.
     *
     * @return up to our maximum lines from the tail of our file
     * @throws IOException if there is an error reading the file
     */
    public String getTail() throws IOException
    {
        if (maxLines == 0 || file.length() == 0)
        {
            return "";
        }
        
        final int CHUNK_SIZE = ESTIMATED_BYTES_PER_LINE * maxLines;

        RandomAccessFile raf = null;
        byte[] buffer = new byte[CHUNK_SIZE];

        try
        {
            raf = new RandomAccessFile(file, "r");
            long lastSeek = raf.length();
            List<String> accumulatedLines = new LinkedList<String>();
            boolean lastChunkStartedWithLF = false;

            // Grab one more line than we need, as the first line in
            // accumulatedLines is partial and will be discarded.
            while (lastSeek > 0 && accumulatedLines.size() <= maxLines)
            {
                long seek = Math.max(0, lastSeek - CHUNK_SIZE);
                int chunkLength = (int) (lastSeek - seek);

                raf.seek(seek);
                read(raf, buffer, chunkLength);
                if (lastChunkStartedWithLF && buffer[chunkLength - 1] == '\r')
                {
                    chunkLength = chunkLength - 1;
                }

                List<String> extractedLines = extractLines(buffer, chunkLength);
                addLines(accumulatedLines, extractedLines);

                lastSeek = seek;
                lastChunkStartedWithLF = buffer[0] == '\n';
            }

            if (accumulatedLines.size() > maxLines)
            {
                accumulatedLines = accumulatedLines.subList(accumulatedLines.size() - maxLines, accumulatedLines.size());
            }

            return StringUtils.join("\n", accumulatedLines) + "\n";
        }
        finally
        {
            IOUtils.close(raf);
        }
    }

    private void addLines(List<String> accumulatedLines, List<String> extractedLines)
    {
        if (extractedLines.size() > 0)
        {
            if (accumulatedLines.size() > 0)
            {
                // Join the last extracted line with the first accumulated.
                accumulatedLines.set(0, extractedLines.get(extractedLines.size() - 1) + accumulatedLines.get(0));
            }
            
            extractedLines = extractedLines.subList(0, extractedLines.size() - 1);
            accumulatedLines.addAll(0, extractedLines);
        }
    }

    private void read(RandomAccessFile raf, byte[] buffer, int length) throws IOException
    {
        int offset = 0;
        while (offset < length)
        {
            offset += raf.read(buffer, offset, length - offset);
        }
    }

    public List<String> extractLines(byte[] chunk, int length)
    {
        List<String> lines = new LinkedList<String>();
        int offset = 0;
        for (int i = 0; i < length; i++)
        {
            byte b = chunk[i];
            if (isEOLByte(b))
            {
                if (i > offset)
                {
                    lines.add(new String(chunk, offset, i - offset));
                }
                else
                {
                    lines.add("");
                }

                if (i < length - 1 && b == '\r' && chunk[i + 1] == '\n')
                {
                    i++;
                }

                offset = i + 1;
            }
        }

        if (offset < length)
        {
            lines.add(new String(chunk, offset, length - offset));
        }
        else
        {
            lines.add("");
        }

        return lines;
    }

    private boolean isEOLByte(byte b)
    {
        switch (b)
        {
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }
}
