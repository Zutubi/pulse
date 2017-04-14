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

package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * Raised when the web UI hits an entity or record that does not exist.  This
 * is a situation that should never happen when using links in the UI (except
 * perhaps if there is a concurrent edit).  The handling is done in a single
 * place, the LookupErrorInterceptor, and the user will see the error
 * message.
 *
 * @see com.zutubi.pulse.master.xwork.interceptor.LookupErrorInterceptor
 */
public class LookupErrorException extends PulseRuntimeException
{
    public LookupErrorException(String errorMessage)
    {
        super(errorMessage);
    }
}
