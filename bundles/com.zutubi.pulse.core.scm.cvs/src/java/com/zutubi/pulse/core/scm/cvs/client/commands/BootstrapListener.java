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

package com.zutubi.pulse.core.scm.cvs.client.commands;

import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;
import org.netbeans.lib.cvsclient.event.CVSAdapter;

import java.io.File;
import java.io.IOException;

/**
 * Common implementation of the checkout and update listeners.
 */
public class BootstrapListener extends CVSAdapter
{
    private static final Logger LOG = Logger.getLogger(BootstrapListener.class);

    protected final ScmFeedbackHandler handler;
    protected final String workingDirectory;

    public BootstrapListener(ScmFeedbackHandler handler, File workingDirectory)
    {
        if (handler == null)
        {
            throw new NullPointerException("handler is a required argument.");
        }
        if (workingDirectory == null)
        {
            throw new NullPointerException("working directory is a required argument.");
        }

        this.handler = handler;
        try
        {
            this.workingDirectory = workingDirectory.getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void reportStatus(String type, File file)
    {
        try
        {
            handler.status(type + " " + relativePath(file));
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }
    }

    /**
     * Return the string that represents the path of the file relative to the working directory, with separators
     * normalised.
     *
     * @param file for which we want the relative path.
     *
     * @return the relative path
     *
     * @throws java.io.IOException in case of error.
     */
    private String relativePath(File file) throws IOException
    {
        String path = file.getCanonicalPath();
        if (path.startsWith(workingDirectory))
        {
            path = path.substring(workingDirectory.length());
        }
        path = FileSystemUtils.normaliseSeparators(path);
        if (path.startsWith(FileSystemUtils.NORMAL_SEPARATOR))
        {
            path = path.substring(1);
        }
        if (path.endsWith(FileSystemUtils.NORMAL_SEPARATOR))
        {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
