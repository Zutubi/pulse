package com.zutubi.pulse.servercore.servlet;

import com.google.common.io.ByteStreams;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Utilities for implementing servlets.
 */
public class ServletUtils
{
    private static final Logger LOG = Logger.getLogger(ServletUtils.class);

    /**
     * Writes the contents of a file to a servlet response.
     *
     * @param file the file to write
     * @param response the response to write to
     */
    public static void sendFile(File file, HttpServletResponse response)
    {
        try
        {
            try
            {
                response.setContentType("application/x-octet-stream");
                response.setContentLength((int) file.length());

                FileInputStream input = null;
                try
                {
                    input = new FileInputStream(file);
                    ByteStreams.copy(input, response.getOutputStream());
                }
                finally
                {
                    IOUtils.close(input);
                }

                response.getOutputStream().flush();
            }
            catch (FileNotFoundException e)
            {
                LOG.warning(e);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                LOG.warning(e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O error: " + e.getMessage());
            }
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }
    }
}
