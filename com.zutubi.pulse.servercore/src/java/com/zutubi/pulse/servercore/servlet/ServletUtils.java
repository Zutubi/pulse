/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
