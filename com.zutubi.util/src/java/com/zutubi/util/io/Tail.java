package com.zutubi.util.io;

import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
import static java.util.Arrays.asList;

/**
 * Implementation for efficiently reading lines from the end of a file set,
 * similar to the unix tail utility.  A file set is conceptually a single file
 * formed by the concatenation of the files in the order given (so the tail is
 * read starting from the last file given and working backwards through the
 * set).
 */
public class Tail
{
    static final int DEFAULT_LINE_COUNT = 10;
    static final int ESTIMATED_BYTES_PER_LINE = 500;

    private List<File> files;
    private int maxLines;

    /**
     * Create tailer that will return a default number of lines from the end of
     * the given file set.
     *
     * @param files file set to tail, read in the reverse of the order given
     */
    public Tail(File... files)
    {
        this(DEFAULT_LINE_COUNT, files);
    }

    /**
     * Create tailer that will return up to the given number of lines from the
     * end of the given file set.
     *
     * @param files    file set to tail, read in the reverse of the order given
     * @param maxLines the maximum number of lines to read
     */
    public Tail(int maxLines, File... files)
    {
        this.maxLines = maxLines;
        this.files = newArrayList(reverse(asList(files)));
    }

    /**
     * Reads the tail of the file set, up to our line limit, and returns the
     * result as a single string.  Line separators are normalised to newlines.
     * <p/>
     * This method may be called multiple times, each call will independently
     * open the file set to read the lines.
     *
     * @return up to our maximum lines from the tail of our file set
     * @throws IOException if there is an error reading the files
     */
    public String getTail() throws IOException
    {
        int totalLines = 0;
        String result = "";
        for (File f: files)
        {
            if (totalLines == maxLines)
            {
                break;
            }

            Pair<Integer, String> countAndTail = getTail(f, maxLines - totalLines);
            if (countAndTail.first > 0)
            {
                result = countAndTail.second + result;
                totalLines += countAndTail.first;
            }
        }

        return result;
    }

    private Pair<Integer, String> getTail(File file, int maxLines) throws IOException
    {
        if (maxLines == 0 || file.length() == 0)
        {
            return new Pair<Integer, String>(0, "");
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

            return new Pair<Integer, String>(accumulatedLines.size(), StringUtils.join("\n", accumulatedLines) + "\n");
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

    private List<String> extractLines(byte[] chunk, int length)
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
