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

/**
 * A handler that accepts output as raw bytes.
 * 
 * @see ProcessWrapper
 */
public interface ByteHandler
{
    /**
     * Called with a chunk of output.  Note this method may be called from different threads.
     * 
     * @param buffer array holding the output data
     * @param n number of bytes available in the array (starting from the beginning)
     * @param error if false, the data is from standard output, if true it is from standard error
     * @throws Exception on error
     */
    void handle(byte[] buffer, int n, boolean error) throws Exception;
}
