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

package com.zutubi.tove.security;

import java.util.Set;

/**
 * Used to determine which authorities are allowed to perform actions on
 * resources.
 */
public interface AuthorityProvider<T>
{
    /**
     * Returns the set of authorities that are allowed to perform the given
     * action on the given object.
     *
     * @param action   the action requested
     * @param resource the object the action is requested on
     * @return the set of authorities that are allowed to perform the action
     */
    Set<String> getAllowedAuthorities(String action, T resource);
}
