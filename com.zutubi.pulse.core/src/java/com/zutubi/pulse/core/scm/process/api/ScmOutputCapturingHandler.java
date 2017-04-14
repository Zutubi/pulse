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

package com.zutubi.pulse.core.scm.process.api;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * An SCM output handler that captures stdout and stderr into buffers.
 */
public class ScmOutputCapturingHandler extends ScmOutputHandlerSupport implements ScmByteHandler
{
    private StringBuilder stdout = new StringBuilder();
    private StringBuilder stderr = new StringBuilder();

    private Charset charset;

    public ScmOutputCapturingHandler(Charset charset)
    {
        this.charset = charset;
    }

    public void handleStdout(byte[] buffer, int n)
    {
        stdout.append(convert(buffer, n));
    }

    public void handleStderr(byte[] buffer, int n)
    {
        stderr.append(convert(buffer, n));
    }

    private String convert(byte[] buffer, int n)
    {
        try
        {
            return new String(buffer, 0, n, charset.name());
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }

    /**
     * Returns the standard output captured from the process.
     *
     * @return the output as a single string
     */
    public String getOutput()
    {
        return stdout.toString();
    }

    /**
     * Returns the standard error captured from the process.
     *
     * @return the error output as a single string
     */
    public String getError()
    {
        return stderr.toString();
    }
}
