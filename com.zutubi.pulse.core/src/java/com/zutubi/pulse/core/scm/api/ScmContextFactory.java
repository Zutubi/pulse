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

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 * A service for creating {@link ScmContext} instances.  Note that SCM plugin
 * authors will rarely need to create contexts, they are provided by Pulse.  A
 * common exception is configuration check handlers, which may need to generate
 * a context via this factory.
 * <p/>
 * To get an instance of this factory, annotate your configuration check class
 * with {@link com.zutubi.tove.annotations.Wire} and declare a setter named
 * setScmContextFactory.
 */
public interface ScmContextFactory
{
    /**
     * Creates a context for the given configuration.
     *
     * @param scmConfiguration SCM configuration to create a context for
     * @param implicitResource the name of a resource to implicitly import into
     *        the context (may be null), see {@link com.zutubi.pulse.core.scm.api.ScmClient#getImplicitResource()}
     * @return the created context
     */
    ScmContext createContext(ScmConfiguration scmConfiguration, String implicitResource);
}
