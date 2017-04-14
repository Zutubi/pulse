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

package com.zutubi.pulse.master.rest.model.setup;

import com.zutubi.pulse.Version;

/**
 * Models a Pulse server version.
 */
public class VersionModel
{
    private final String buildDate;
    private final String buildNumber;
    private final String versionNumber;

    public VersionModel(Version version)
    {
        buildDate = version.getBuildDate();
        buildNumber = version.getBuildNumber();
        versionNumber = version.getVersionNumber();
    }

    public String getBuildDate()
    {
        return buildDate;
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public String getVersionNumber()
    {
        return versionNumber;
    }
}
