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

/**
 * A handler that accepts output line-by-line.
 *
 * @see ProcessWrapper
 */
public interface LineHandler
{
    /**
     * Indicates the character set to use for byte to character conversion.
     * 
     * @return the character set used to convert bytes to characters
     */
    Charset getCharset();

    /**
     * Called with a line of output.  Note this method may be called from different threads.
     *
     * @param line the line of output (does not include the line terminator)
     * @param error if false, the line is from standard output, if true it is from standard error
     * @throws Exception on error
     */
    void handle(String line, boolean error) throws Exception;
}
