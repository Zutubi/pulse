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

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.model.Entity;

/**
 * Links builds with a dependency relationship.
 */
public class BuildDependencyLink extends Entity
{
    private long upstreamBuildId;
    private long downstreamBuildId;

    public BuildDependencyLink()
    {
    }

    public BuildDependencyLink(long upstreamBuildId, long downstreamBuildId)
    {
        this.upstreamBuildId = upstreamBuildId;
        this.downstreamBuildId = downstreamBuildId;
    }

    public long getUpstreamBuildId()
    {
        return upstreamBuildId;
    }

    public void setUpstreamBuildId(long upstreamBuildId)
    {
        this.upstreamBuildId = upstreamBuildId;
    }

    public long getDownstreamBuildId()
    {
        return downstreamBuildId;
    }

    public void setDownstreamBuildId(long downstreamBuildId)
    {
        this.downstreamBuildId = downstreamBuildId;
    }

    public static class HasDownstreamId implements Predicate<BuildDependencyLink>
    {
        private long buildId;

        public HasDownstreamId(long buildId)
        {
            this.buildId = buildId;
        }

        public boolean apply(BuildDependencyLink buildDependencyLink)
        {
            return buildDependencyLink.getDownstreamBuildId() == buildId;
        }
    }

    public static class HasUpstreamId implements Predicate<BuildDependencyLink>
    {
        private long buildId;

        public HasUpstreamId(long buildId)
        {
            this.buildId = buildId;
        }

        public boolean apply(BuildDependencyLink buildDependencyLink)
        {
            return buildDependencyLink.getUpstreamBuildId() == buildId;
        }
    }
}
