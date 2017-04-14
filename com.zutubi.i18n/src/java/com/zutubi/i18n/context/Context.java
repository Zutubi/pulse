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

package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * A Context provides two functions.  It holds the information necessary
 * for a ContextResolver to resolve the paths that will be used to lookup
 * the resource bundles associated with the context. Secondly, it defines
 * how the resolved resources are then loaded.
 */
public interface Context
{
    /**
     * Retrieve the named resource from within this context.  This method
     * is used by the default context loader implementation to load a
     * resource.
     *
     * @param name the name of the resource
     *
     * @return the input stream attached to the named resource, or
     * null if the resource could not be located.
     */
    InputStream getResourceAsStream(String name);
}
