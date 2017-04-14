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

/**
 * The context resolver is used to 'resolve' a context into a list
 * of resource names specific to that context.
 */
public interface ContextResolver<T extends Context>
{
    /**
     * Resolve the specified context into a list of resource names.
     *
     * @param context the context of interest.
     *
     * @return an array of strings representing the resolved resource
     * names.
     */
    String[] resolve(T context);

    /**
     * Get the context type supported by this context resolver.  This
     * is used by the bundle manager to identify the resolvers for a
     * specific context. 
     *
     * @return the context class.
     */
    Class<T> getContextType();
}
