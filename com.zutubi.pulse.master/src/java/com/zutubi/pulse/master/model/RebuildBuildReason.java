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

package com.zutubi.pulse.master.model;

/**
 * The build reason for any upstream build that is triggered in response to
 * a project dependency relationship.
 */
public class RebuildBuildReason extends AbstractBuildReason
{
    /**
     * The name of the downstream project that is the source of this build request.
     */
    private String source;

    public RebuildBuildReason()
    {
    }

    public RebuildBuildReason(String source)
    {
        this.source = source;
    }

    public String getSummary()
    {
        return "build with dependencies of " + source;
    }

    // for hibernate only.
    private String getSource()
    {
        return source;
    }

    // for hibernate only.
    private void setSource(String source)
    {
        this.source = source;
    }
}
