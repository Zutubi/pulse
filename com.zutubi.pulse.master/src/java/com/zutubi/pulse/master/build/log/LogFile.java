package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.servercore.cleanup.FileDeletionService;
import com.zutubi.util.CircularBuffer;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.ZipUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.Tail;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

/**
 * Abstraction around the build/recipe log files that provides:
 *
 * <ul>
 *   <li>the ability to tail the file</li>
 *   <li>access to the full file</li>
 *   <li>compression of complete logs</li>
 * </ul>
 *
 * Multiple readers and writers are allowed.  How the file content is affected/
 * seen by concurrent readers and writers depends on the underlying file
 * system.  This implementation will ensure that compression/uncompression is
 * thread safe, however.
 * <p/>
 * The implementation will check for compression opportunities whenever the
 * count of readers/writers drops to zero, and a writer has been active since
 * we last checked.  If there is enough data in the underlying file, it will be
 * compressed to a new file with a .gz extension.  The original file is
 * removed.  For efficient tailing, when compressing the implementation keeps a
 * .tail file with the last 100 lines of the file in uncompressed form.
 */
public class LogFile
{
    private static final Logger LOG = Logger.getLogger(LogFile.class);

    public static final String EXTENSION_ZIP = ".gz";
    public static final String EXTENSION_TAIL = ".tail";
    public static final String EXTENSION_NON_EXISTENT = ".non";

    private static final int DEFAULT_COMPRESS_THRESHOLD = 100 * 1024;
    private static final int DEFAULT_TAIL_LIMIT = 250;

    private static final Map<String, SharedData> canonicalPathToData = new HashMap<String, SharedData>();

    private SharedData data;
    private File file;
    private boolean compressionEnabled;
    private int compressThreshold = DEFAULT_COMPRESS_THRESHOLD;
    private int tailLimit = DEFAULT_TAIL_LIMIT;

    /**
     * Creates a new log file with the given id and file.  The id should be
     * unique for each different file, but the same for LogFiles that wrap the
     * same underlying file system file.
     *
     * @param file               the file to read/write
     * @param compressionEnabled if true the underlying file may be compressed
     */
    public LogFile(File file, boolean compressionEnabled)
    {
        this(file, DEFAULT_COMPRESS_THRESHOLD, DEFAULT_TAIL_LIMIT, compressionEnabled);
    }

    LogFile(File file, int compressThreshold, int tailLimit, boolean compressionEnabled)
    {
        this.file = file;
        this.compressThreshold = compressThreshold;
        this.tailLimit = tailLimit;
        this.compressionEnabled = compressionEnabled;

        String canonicalPath = getCanonicalPath(file);
        synchronized (canonicalPathToData)
        {
            data = canonicalPathToData.get(canonicalPath);
            if (data == null)
            {
                data = new SharedData();
                canonicalPathToData.put(canonicalPath, data);
            }
        }
    }

    private String getCanonicalPath(File file)
    {
        // We use a unique path that never exists as canonical paths may change
        // when the referred-to path is created/deleted.
        File nonExistent = new File(file.getAbsolutePath() + EXTENSION_NON_EXISTENT);
        try
        {
            return nonExistent.getCanonicalPath();
        }
        catch (IOException e)
        {
            LOG.warning(e);
            return nonExistent.getAbsolutePath();
        }
    }

    /**
     * Indicates if the file exists.  It may still be empty.
     *
     * @return true if the underlying file exists
     */
    public boolean exists()
    {
        data.lock.lock();
        try
        {
            return file.exists() || getZipFile().exists();
        }
        finally
        {
            data.lock.unlock();
        }
    }

    /**
     * Opens a writer which will append to the underlying file.  When the
     * writer is closed the file may be compressed.
     *
     * @return a writer that may be used to append to the underlying file
     * @throws IOException on error
     */
    public Writer openWriter() throws IOException
    {
        final FileWriter delegate = prepareWriter();
        return new Writer()
        {
            @Override
            public void write(char[] buffer, int offset, int length) throws IOException
            {
                delegate.write(buffer, offset, length);
            }

            @Override
            public void flush() throws IOException
            {
                delegate.flush();
            }

            @Override
            public void close() throws IOException
            {
                try
                {
                    delegate.close();
                }
                finally
                {
                    onClose();
                }
            }
        };
    }

    private FileWriter prepareWriter() throws IOException
    {
       data.lock.lock();
        try
        {
            if (isCompressed())
            {
                uncompress();
            }

            FileWriter writer = new FileWriter(file, true);
            data.openCount++;
            data.dirty = true;
            return writer;
        }
        finally
        {
            data.lock.unlock();
        }
    }

    /**
     * Indicates if the underlying file is compressed.
     *
     * @return true if the underlying file is compressed
     */
    public boolean isCompressed()
    {
        data.lock.lock();
        try
        {
            return !file.exists() && getZipFile().exists();
        }
        finally
        {
            data.lock.unlock();
        }
    }

    private void uncompress() throws IOException
    {
        File zipFile = getZipFile();
        ZipUtils.uncompressFile(zipFile, file);
    }

    private void onClose()
    {
        data.lock.lock();
        try
        {
            data.openCount--;
            if (data.openCount == 0 && data.dirty)
            {
                compress();
                data.dirty = false;
            }
        }
        finally
        {
            data.lock.unlock();
        }
    }

    private void compress()
    {
        if (compressionEnabled && file.length() > compressThreshold)
        {
            File zipFile = getZipFile();
            File tailFile = getTailFile();

            cleanUp(zipFile);
            cleanUp(tailFile);

            try
            {
                Tail tail = new Tail(tailLimit, file);
                FileSystemUtils.createFile(tailFile, tail.getTail());
                ZipUtils.compressFile(file, zipFile);
                cleanUp(file);
            }
            catch (IOException e)
            {
                // This is not fatal, as we can live without the zip.  The user
                // should be notified, though.
                cleanUp(tailFile);
                cleanUp(zipFile);
                LOG.severe(e);
            }
        }
    }

    private File getTailFile()
    {
        return new File(file.getAbsolutePath() + EXTENSION_TAIL);
    }

    private File getZipFile()
    {
        return new File(file.getAbsolutePath() + EXTENSION_ZIP);
    }

    private void cleanUp(File file)
    {
        if (file != null && file.exists() && !file.delete())
        {
            LOG.warning("Unable to clean up file '" + file.getAbsolutePath() + "'.");
        }
    }

    /**
     * Opens a stream that can be used to read the contents of the underlying
     * file.
     *
     * @return a stream that may be used to read the file contents
     * @throws IOException on error
     */
    public InputStream openInputStream() throws IOException
    {
        final InputStream delegate = prepareInputStream();
        return new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                return delegate.read();
            }

            @Override
            public void close() throws IOException
            {
                try
                {
                    delegate.close();
                }
                finally
                {
                    onClose();
                }
            }
        };
    }

    private InputStream prepareInputStream() throws IOException
    {
        data.lock.lock();
        try
        {
            InputStream inputStream;
            if (isCompressed())
            {
                inputStream = new GZIPInputStream(new FileInputStream(getZipFile()));
            }
            else
            {
                 inputStream = new FileInputStream(file);
            }

            data.openCount++;
            return inputStream;
        }
        finally
        {
            data.lock.unlock();
        }
    }

    /**
     * Returns up to given limit of lines from the end of the file.  Lines are
     * returned in the order found in the file.  If the file contains less than
     * the maximum specified, the entire file content is returned.
     *
     * @param maxLines the maximum number of lines to return
     * @return the last lines from the file, up to the given maximum
     * @throws IOException on a read error
     */
    public String getTail(int maxLines) throws IOException
    {
        Tail tailer = null;
        InputStream is = null;
        data.lock.lock();
        try
        {
            if (isCompressed())
            {
                if (maxLines <= tailLimit)
                {
                    tailer = new Tail(maxLines, getTailFile());
                }
                else
                {
                    is = new GZIPInputStream(new FileInputStream(getZipFile()));
                }
            }
            else
            {
                tailer = new Tail(maxLines, file);
            }

            data.openCount++;
        }
        finally
        {
            data.lock.unlock();
        }

        try
        {
            if (tailer == null)
            {
                return tailStream(maxLines, is);
            }
            else
            {
                return tailer.getTail();
            }
        }
        finally
        {
            onClose();
        }
    }

    private String tailStream(int maxLines, InputStream is) throws IOException
    {
        try
        {
            // We don't have random access to the stream.  So we need to scroll
            // through it to get the lines at the end.
            CircularBuffer<String> buffer = new CircularBuffer<String>(maxLines);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            int size = 0;
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line);
                size += line.length() + 1;
            }

            StringBuilder result = new StringBuilder(size);
            for (String tailLine: buffer)
            {
                result.append(tailLine);
                result.append('\n');
            }

            return result.toString();
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    /**
     * Schedules deletion of all files associated with this log using the given
     * service.
     *
     * @param deletionService service used to schedule deletion
     */
    public void scheduleCleanup(FileDeletionService deletionService)
    {
        data.lock.lock();
        try
        {
            deletionService.delete(file, false);
            deletionService.delete(getZipFile(), false);
            deletionService.delete(getTailFile(), false);
        }
        finally
        {
            data.lock.unlock();
        }
    }

    /**
     * Holds data shared by multiple instances which work on the same
     * underlying files.  This data is protected by the contained lock.
     */
    private static class SharedData
    {
        Lock lock = new ReentrantLock();
        boolean dirty;
        int openCount;
    }
}
