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

package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * A config type that makes bad references from its properties, to test error
 * handling.
 */
@SymbolicName("badReferences")
public class BadReferencesConfiguration extends AbstractConfiguration
{
    @Reference
    private UnreferenceableConfiguration unreferenceable;
    @Reference
    private BadReferenceNameConfiguration badReferenceName;

    public UnreferenceableConfiguration getUnreferenceable()
    {
        return unreferenceable;
    }

    public void setUnreferenceable(UnreferenceableConfiguration unreferenceable)
    {
        this.unreferenceable = unreferenceable;
    }

    public BadReferenceNameConfiguration getBadReferenceName()
    {
        return badReferenceName;
    }

    public void setBadReferenceName(BadReferenceNameConfiguration badReferenceName)
    {
        this.badReferenceName = badReferenceName;
    }
}
