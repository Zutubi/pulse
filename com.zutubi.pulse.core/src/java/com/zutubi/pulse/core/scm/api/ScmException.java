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

package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.api.PulseException;

/**
 * An error raised during interaction with an SCM server.
 */
public class ScmException extends PulseException
{
    private boolean reinitialiseRequired = false;

    /**
     * Create a new SCM exception.
     */
    public ScmException()
    {
        super();
    }

    /**
     * Create a new SCM exception.
     *
     * @param message human-readable error message
     */
    public ScmException(String message)
    {
        super(message);
    }

    /**
     * Create a new SCM exception.
     *
     * @param cause root cause of the error, or null if there is none
     */
    public ScmException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a new SCM exception.
     *
     * @param message human-readable error message
     * @param cause   root cause of the error, or null if there is none
     */
    public ScmException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Indicates if this error means that the project requires
     * reinitialisation.
     *
     * @return true if the project should be reinitialised to overcome this
     *         error
     */
    public boolean isReinitialiseRequired()
    {
        return reinitialiseRequired;
    }

    /**
     * Used to flag this error as one requiring project reinitialisation.  When
     * errors with this flag set are seen by some internal Pulse managers,
     * those managers will request the initialisation.
     */
    public void setReinitialiseRequired()
    {
        this.reinitialiseRequired = true;
    }
}
