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

package com.zutubi.tove.config;

/**
 * Used for fatal configuration subsystem errors.
 */
public class ToveRuntimeException extends RuntimeException
{
    public ToveRuntimeException()
    {
    }

    public ToveRuntimeException(String message)
    {
        super(message);
    }

    public ToveRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ToveRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
