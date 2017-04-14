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

package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.api.PulseException;

/**
 * Exception to raise when an error occurs setting the property of a Pulse file
 * element.  This causes Pulse file loading to fail with an error message.
 */
public class FileLoadException extends PulseException
{
    /**
     * Creates a file load exception with no details.
     */
    public FileLoadException()
    {
        super();
    }

    /**
     * Creates a file load exception with the given message.
     *
     * @param errorMessage human-readble description of the failure
     */
    public FileLoadException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Creates a file load exception with the given root cause.
     *
     * @param cause exception that is the root cause of the failure
     */
    public FileLoadException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a file load exception with the given message and root cause.
     *
     * @param errorMessage human-readble description of the failure
     * @param cause        exception that is the root cause of the failure
     */
    public FileLoadException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
