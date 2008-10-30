package com.zutubi.pulse.master.project;

import com.zutubi.pulse.master.project.events.ProjectEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCommencedEvent;
import com.zutubi.pulse.master.project.events.ProjectInitialisationCompletedEvent;
import com.zutubi.pulse.master.project.events.ProjectStatusEvent;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.Tail;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.text.DateFormat;
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
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
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
                write("Project initialisation failed" + pice.getError() == null ? "" : (": " + pice.getError()));
            }
        }
        else
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
