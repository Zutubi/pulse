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

package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.List;
import java.util.Map;

/**
 * Action resolvers are used to piece together a URL scheme.  Each resolver
 * handles a specific point in the URL path.  Resolvers specify:
 * <ul>
 *   <li>the action the URL maps to, if any</li>
 *   <li>what parameters should be added to the request</li>
 *   <li>what paths may nest under the URL</li>
 * </ul>
 */
public interface ActionResolver
{
    /**
     * @return the name of the XWork action the URL maps to, or null if this
     *         URL does not correspond to an action
     */
    String getAction();

    /**
     * Returns the parameters that this resolver wishes to add to the request.
     * These parameters may be picked up by the action when processing the
     * request.  Note that parameters from all resolvers along a path are
     * accumulated into the request, but parameters from later resolvers may
     * shadow those from earlier ones.
     *
     * @return a name-value mapping of parameters to add to the request
     */
    Map<String, String> getParameters();

    /**
     * Supplies a human-readable description of the paths that may nest under
     * this resolver.  Note that some paths may not be concrete, in this case
     * the unknown parameters should be wrapped in angle brackets.  Note that
     * chasing these children may lead to an infinite loop.
     * <p/>
     * The result of this method is intended to be documentary: in particular
     * it is not intended to be processed by machines.
     *
     * @return a representation of the paths that may nest under this URL
     */
    List<String> listChildren();

    /**
     * Returns a resolver to handle the specified child URL, if one exists.
     *
     * @param name the next element of the URL being resolved
     * @return the child resolver for the element, or null if that URL is
     *         invalid
     */
    ActionResolver getChild(String name);
}
