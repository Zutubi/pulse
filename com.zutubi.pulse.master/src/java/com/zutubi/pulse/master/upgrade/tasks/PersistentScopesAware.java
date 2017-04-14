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

package com.zutubi.pulse.master.upgrade.tasks;

/**
 * Interface for instances that are aware of the persistent scopes in which
 * they are operating, usually because they need templating information to
 * perform their job.
 */
public interface PersistentScopesAware
{
    /**
     * Used to wire in information about the scopes in which the upgrade is
     * taking place.  The wiring happens once before the instance is used for
     * its primary purpose.
     *
     * @param persistentScopes information about the scopes in which the
     *                         upgrade is taking place
     */
    void setPersistentScopes(PersistentScopes persistentScopes);
}
