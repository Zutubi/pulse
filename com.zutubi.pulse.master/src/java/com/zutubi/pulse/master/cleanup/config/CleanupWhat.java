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

package com.zutubi.pulse.master.cleanup.config;

/**
 * The cleanup what defines the options of what part of a build
 * can be cleaned up separately.
 */
public enum CleanupWhat
{
    /**
     * Cleanup the build directories and captured artifacts.
     */
    BUILD_ARTIFACTS,

    /**
     * Cleanup the artifacts published to the repository.
     */
    REPOSITORY_ARTIFACTS,

    /**
     * Cleanup the build logs.
     */
    LOGS
}
