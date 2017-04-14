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

import com.zutubi.pulse.core.engine.api.ExecutionContext;

/**
 * The context in which SCM operations are executed on the master.  This
 * consists of two parts:
 * <ul>
 *     <li>
 *         A persistent context that is shared among all clients for the same
 *         project.  This context may be null in cases where no project is
 *         defined.  Such cases are documented in the {@link ScmClient}
 *         interface.
 *     </li>
 *     <li>
 *         An environment context that is not shared, and differs in the level
 *         of detail it provides.  It will always contain the global master
 *         environment.  In most cases it will also include environment from
 *         the project, and sometimes even context from a build (e.g. when
 *         run under a hook).
 *     </li>
 * </ul>
 */
public interface ScmContext
{
    /**
     * @return A persistent context shared among clients for the same project,
     *         may be null where no project is defined.
     */
    PersistentContext getPersistentContext();
    /**
     * @return A context containing environmental details from the server, and
     * (where available) the project the SCM is defined on.  Project
     * information is available in most cases, with possible exceptions noted
     * in the {@link ScmClient} interface.  A simple test would be to try to
     * access the "project" property.
     */
    ExecutionContext getEnvironmentContext();
}
