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

/**
 * The build controller interface is designed to provide an abstraction between
 * the build management and the build processing system.
 *
 * The primary purpose is to allow the management system to be tested in isolation
 * of the actual building.
 */
public interface BuildController
{
    /**
     * Get the build result id associated with this build or -1 if no result
     * is available.
     * 
     * @return build result id.
     */
    long getBuildResultId();

    /**
     * Start the build.
     *
     * Note that the actual build execution should not occur on the callers
     * thread else the scheduling will be blocked whilst the build is running.
     *
     * @return the build number for the controlled build
     */
    long start();
}
