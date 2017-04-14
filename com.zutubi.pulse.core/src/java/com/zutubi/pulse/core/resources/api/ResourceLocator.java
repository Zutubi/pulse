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

package com.zutubi.pulse.core.resources.api;

import java.util.List;

/**
 * <p>
 * Interface for classes that can automatically discover installed resources.
 * A locator may, for example, find an installation of Apache Ant and create a
 * matching resource which will be picked up automatically by Ant projects.
 * </p>
 * <p>
 * Resource discovery occurs on each agent when the agent comes online.  The
 * process should therefore be reasonably fast -- e.g. it is not acceptable to
 * search large areas of the file system looking for resources.  As a general
 * rule, no locator should take more than a few seconds to run.
 * </p>
 * <p>
 * Resources located in this way will not overwrite those configured by the
 * user.  Instead, non-conflicting parts will be merged in to existing
 * configuration where possible, and other parts will be ignored.
 * </p>
 * <p>
 * Instead of implementing this class directly, consider subclassing one of the
 * support classes.
 * </p>
 */
public interface ResourceLocator
{
    /**
     * Locates and returns information about resources on the local machine.
     * Implementations of this method should limit their running time to at
     * most a few seconds.  It is not advisable, for example, to scan large
     * areas of the file system.  Rather, searches should be restricted to
     * probable install locations.
     * 
     * @return a list of all resources located
     */
    List<ResourceConfiguration> locate();
}
