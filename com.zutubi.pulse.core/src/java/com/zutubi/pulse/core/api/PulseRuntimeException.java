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

package com.zutubi.pulse.core.api;

/**
 * Base class for unchecked exceptions specific to Pulse.
 */
public class PulseRuntimeException extends RuntimeException
{
    /**
     * Creates a new exception with no further information.
     */
    public PulseRuntimeException()
    {
        super();
    }

    /**
     * Creates a new exception with a detail message.
     *
     * @param errorMessage the exception detail message
     */
    public PulseRuntimeException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause what caused this exception
     */
    public PulseRuntimeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception with the given detail message and cause.
     *
     * @param errorMessage the exception detail message
     * @param cause what caused this exception
     */
    public PulseRuntimeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
