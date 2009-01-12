package com.zutubi.pulse.master.project;

import com.zutubi.pulse.master.project.events.ProjectEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCommencedEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCompletedEvent;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.MultipleFileInputStream;
import com.zutubi.util.io.Tail;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manages the log for a single project.  To prevent the log from growing
 * indefinitely, log files are rotated out when they hit a limit, and only two
 * files (including the current one) are kept.
 */
public class ProjectLogger implements Closeable
{
    private static final Logger LOG = Logger.getLogger(ProjectLogger.class);

    // A static DateTime is safe as loggers are thread safe via mutual
    // exclusion.
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    static final String NAME_PATTERN = "project.%d.log";

    private int sizeLimit;
    private PrintWriter logWriter;
    private File lastFile;
    private File currentFile;
    private boolean closed = false;

    /**
     * Create a new project logger that will write to the given directory, and
     * apply the given size limit to written files.  Note that the total space
     * taken can be up to twice the limit in bytes (plus a small overlap for
     * the message that takes each file over the limit).
     *
     * @param dir       project directory to log to
     * @param sizeLimit soft size limit in bytes of the files created, up to
     *                  two full files may be created before removing the
     *                  oldest messages to free space
     */
    public ProjectLogger(File dir, int sizeLimit)
    {
        this.sizeLimit = sizeLimit;
        currentFile = new File(dir, String.format(NAME_PATTERN, 0));
        lastFile = new File(dir, String.format(NAME_PATTERN, 1));
    }

    /**
     * Writes the given event to the log.
     *
     * @param event event to be logged, must relate to our project (this
     *              implementation does not check)
     * @throws IllegalStateException if this logger has been closed
     */
    public synchronized void log(ProjectEvent event)
    {
        if (closed)
        {
            throw new IllegalStateException("Attempt to write to a closed log");
        }

        if (event instanceof ProjectInitialisationCommencedEvent)
        {
            write("Project initialisation commenced...");
        }
        else if (event instanceof ProjectInitialisationCompletedEvent)
        {
            ProjectInitialisationCompletedEvent pice = (ProjectInitialisationCompletedEvent) event;
            if (pice.isSuccessful())
            {
                write("Project initialisation succeeded.");
            }
            else
            {
                write("Project initialisation failed" + (pice.getError() == null ? "" : (": " + pice.getError())));
            }
        }
        else if (event instanceof ScmChangeEvent)
        {
            ScmChangeEvent sce = (ScmChangeEvent) event;
            write("SCM change detected, new revision: " + sce.getNewRevision().getRevisionString());
        }
        else if (event instanceof ProjectStatusEvent)
        {
            write(((ProjectStatusEvent) event).getMessage());
        }
    }

    /**
     * Return the tail lines from the log, up to the line limit specified.  All
     * returned lines will be terminated with newlines.
     *
     * @param maxLines the limit of lines to return
     * @return the tail of this log
     * @throws IOException if there is a problem reading the log files
     * @throws IllegalStateException if this logger has been closed
     */
    public synchronized String tail(int maxLines) throws IOException
    {
        if (closed)
        {
            throw new IllegalStateException("Attempt to tail a closed log");
        }

        Tail tail = new Tail(maxLines, lastFile, currentFile);
        return tail.getTail();
    }

    /**
     * Return an inputstream that provides access to the raw data of the project log.
     * <p/>
     * Note: the input stream will need to be closed by the caller.
     *
     * @return input stream that provides access to the project log.
     *
     * @throws IOException if there is a problem reading the log files.
     */
    public synchronized InputStream getRawInputStream() throws IOException
    {
        if (closed)
        {
            throw new IllegalStateException("The logger is closed.");
        }

        File[] files = CollectionUtils.filterToArray(new File[]{lastFile, currentFile}, new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return file.isFile();
            }
        });

        return new MultipleFileInputStream(files);
    }

    /**
     * Close the logger, closing underlying files.  Must be called when
     * disposing of a logger.  After calling, the logger can no longer be used.
     */
    public synchronized void close()
    {
        IOUtils.close(logWriter);
        closed = true;
    }

    private void write(String message)
    {
        rotateIfRequired();

        // On error we have no writer.
        if (logWriter != null)
        {
            logWriter.print(DATE_FORMAT.format(new Date()));
            logWriter.print(": ");
            logWriter.println(message);
        }
    }

    private void rotateIfRequired()
    {
        try
        {
            if (currentFile.length() >= sizeLimit)
            {
                IOUtils.close(logWriter);
                logWriter = null;

                if (lastFile.exists() && !FileSystemUtils.robustDelete(lastFile))
                {
                    LOG.warning("Unable to remove old file '" + lastFile.getAbsolutePath() + "'");
                }

                if (!currentFile.renameTo(lastFile))
                {
                    LOG.warning("Unable to rename log file '" + currentFile.getAbsolutePath() + "' to '" + lastFile.getAbsolutePath() + "'");
                }
            }

            if (logWriter == null)
            {
                logWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentFile, true))), true);
            }
        }
        catch (IOException e)
        {
            LOG.severe(e);
        }
    }
}
