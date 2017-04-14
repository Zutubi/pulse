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

import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * A character handler that collects all output in memory for later access.
 */
public class CollectingCharHandler extends ForwardingCharHandler
{
    /**
     * Creates a new collecting handler with the default character set.
     */
    public CollectingCharHandler()
    {
        super(new StringWriter(), new StringWriter());
    }

    /**
     * Creates a new collecting handler with the given character set.
     * 
     * @param charset the character set to be used to convert output bytes to characters
     */
    public CollectingCharHandler(Charset charset)
    {
        super(charset, new StringWriter(), new StringWriter());
    }

    /**
     * @return all standard output that has been collected
     */
    public String getStdout()
    {
        return ((StringWriter)getOutWriter()).getBuffer().toString();
    }

    /**
     * @return all standard error that has been collected
     */
    public String getStderr()
    {
        return ((StringWriter)getErrorWriter()).getBuffer().toString();
    }
}
