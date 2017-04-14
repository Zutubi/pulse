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
 * An actor is some entity that is granted authorities to perform certain
 * actions, e.g. a user.
 */
public interface Actor
{
    String getUsername();
    Set<String> getGrantedAuthorities();
    /**
     * Indicates if this actor represents an anonymous user.
     *
     * @return true if the actor is anonymous, false if they have an identity
     */
    boolean isAnonymous();
}
