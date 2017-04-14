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

package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The build controller factory is a simple factory interface responsible
 * for the construction of BuildController instances.
 */
public interface BuildControllerFactory
{
    /**
     * Create a new BuildController instance that is configured to process
     * the specified build request.
     *
     * @param request the build request details.
     * @return the new build controller instance.
     */
    BuildController create(BuildRequestEvent request);
}
