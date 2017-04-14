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

package com.zutubi.pulse.core.plugins.osgi;

/**
 * This interface defines some of the properties that are defined by OSGI.
 */
public interface OSGiFramework
{
    /**
     * The configuration location for the platform. The configuration determines what plug-ins
     * will run as well as various other system settings.
     */
    static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
}
