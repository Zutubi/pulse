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

package com.zutubi.pulse.core.dependency;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * Raised when the repository attributes encounter a problem with reading or
 * writing the attributes to disk.
 */
public class RepositoryAttributesException extends PulseRuntimeException
{
    public RepositoryAttributesException()
    {
    }

    public RepositoryAttributesException(String message)
    {
        super(message);
    }

    public RepositoryAttributesException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RepositoryAttributesException(Throwable cause)
    {
        super(cause);
    }
}
