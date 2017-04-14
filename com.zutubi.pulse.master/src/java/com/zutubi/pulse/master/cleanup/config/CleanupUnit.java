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
 * The CleanupUnit represents the unit for of the cleanup configurations
 * retain field.  
 */
public enum CleanupUnit
{
    /**
     * Indicates that builds can be retained for a specified number of days.
     */
    DAYS,
    
    /**
     * Indicates that builds can be retained for a specified number of builds.
     */
    BUILDS
}
