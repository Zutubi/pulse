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

package com.zutubi.pulse.core.util.process;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link LineHandler} that collects all handled lines into lists in memory for later access.
 */
public class CollectingLineHandler extends LineHandlerSupport
{
    private List<String> stdout = new LinkedList<String>();
    private List<String> stderr = new LinkedList<String>();

    /**
     * Creates a handler with the default character set.
     */
    protected CollectingLineHandler()
    {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a handler with the given character set.
     *
     * @param charset character set to be used to convert output bytes to characters before passing
     *                to this handler
     */
    protected CollectingLineHandler(Charset charset)
    {
        super(charset);
    }

    public void handle(String line, boolean error)
    {
        if(error)
        {
            stderr.add(line);
        }
        else
        {
            stdout.add(line);
        }
    }

    /**
     * @return all collected lines of standard output, from the first line collected to the last
     */
    public List<String> getStdout()
    {
        return stdout;
    }

    /**
     * @return all collected lines of standard error, from the first line collected to the last
     */
    public List<String> getStderr()
    {
        return stderr;
    }
}
