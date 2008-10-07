package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public enum EOLStyle
{
    /**
     * Treat the file as binary: i.e. do no translation.
     */
    BINARY
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            // Do nothing.
        }
    },
    /**
     * Force carriage return (Old Macintosh style) endings.
     */
    CARRIAGE_RETURN
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.CR_BYTES);
        }
    },
    /**
     * Force carriage return linefeed (Windows style) endings.
     */
    CARRIAGE_RETURN_LINEFEED
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.CRLF_BYTES);
        }
    },
    /**
     * Force linefeed (Unix style) endings.
     */
    LINEFEED
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.LF_BYTES);
        }
    },
    /**
     * Force the file line endings to match the target platform endings.
     */
    NATIVE
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            translateEOLs(file, SystemUtils.LINE_SEPARATOR_BYTES);
        }
    },
    /**
     * Use the target side configuration for text files to determine what
     * to do (e.g. use Perforce client setting for text files on the
     * target side).
     */
    TEXT
    {
        public void apply(File file, EOLStyle localEOL) throws IOException
        {
            localEOL.apply(file, localEOL);
        }
    };

    public void translateEOLs(File file, byte[] lineSeparatorBytes) throws IOException
    {
        if(file.exists())
        {
            FileSystemUtils.translateEOLs(file, lineSeparatorBytes, true);
        }
    }

    public abstract void apply(File file, EOLStyle localEOL) throws IOException;
}
