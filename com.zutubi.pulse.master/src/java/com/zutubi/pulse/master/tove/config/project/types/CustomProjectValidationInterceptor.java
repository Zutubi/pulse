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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.pulse.core.marshal.ToveFileLoadInterceptor;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * A predicate used when validating the pulse file for a custom project.  We
 * allow unresolved references.
 */
public class CustomProjectValidationInterceptor implements ToveFileLoadInterceptor
{
    public boolean loadInstance(Configuration instance, Element element, Scope scope)
    {
        return true;
    }

    public boolean allowUnresolved(Configuration instance, Element element)
    {
        return true;
    }

    public boolean validate(Configuration instance, Element element)
    {
        return true;
    }
}
