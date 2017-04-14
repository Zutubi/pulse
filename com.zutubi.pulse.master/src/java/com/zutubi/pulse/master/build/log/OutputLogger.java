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

package com.zutubi.pulse.master.build.log;

import java.io.Closeable;

/**
 * The output logger defines a sink for output messages generated during the
 * various stages of a build.
 */
public interface OutputLogger extends Closeable
{
    /**
     * Initialise any required resources.  This method will be called before any logging
     * requestes are made.
     */
    void prepare();

    /**
     * Log the data to the output.
     *
     * @param output  the data to be logged.
     */
    void log(byte[] output);

    /**
     * Log the specified portion of the data array to the output
     *
     * @param source the raw data array.
     * @param offset the starting offset for the data that will be logged.
     * @param length the length of the data that will be logged.
     */
    void log (byte[] source, int offset, int length);
}
