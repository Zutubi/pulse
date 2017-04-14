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

import com.zutubi.pulse.master.cleanup.config.CleanupWhat;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * A configuration object that allows you to fine tune exactly which portion of
 * a build is cleaned up.
 *
 * This object implements its own value based equals and hashcode functions so that
 * two options objects with the same settings will be considered as equal. 
 */
public class BuildCleanupOptions
{
    private boolean cleanupAll;

    private List<CleanupWhat> whats = new LinkedList<CleanupWhat>();

    public BuildCleanupOptions(boolean cleanupAll)
    {
        this.cleanupAll = cleanupAll;
    }

    public BuildCleanupOptions(CleanupWhat... what)
    {
        this(Arrays.asList(what));
    }

    public BuildCleanupOptions(List<CleanupWhat> what)
    {
        this.cleanupAll = false;
        whats.addAll(what);
    }

    /**
     * Indicates whether or not a builds captured artifacts should be cleaned up.  This
     * includes artifacts such as the environment listing as well as anything captured
     * during the build.
     *
     * @return true if the builds artifacts will be cleaned up.
     */
    public boolean isCleanBuildArtifacts()
    {
        return isCleanup(CleanupWhat.BUILD_ARTIFACTS);
    }

    /**
     * Indicates whether or not artifacts that have been published to the internal artifact
     * repository should be cleaned up.  These artifacts are distinct from the regular build
     * artifacts in that they are made available to other project builds.
     *
     * @return true if the repository artifacts will be cleaned up.
     */
    public boolean isCleanRepositoryArtifacts()
    {
        return isCleanup(CleanupWhat.REPOSITORY_ARTIFACTS);
    }

    /**
     * Indicates whether or not the log files associated with a build should be cleaned up.
     *
     * @return true if the logs should be cleaned up, false otherwise.
     */
    public boolean isCleanupLogs()
    {
        return isCleanup(CleanupWhat.LOGS);
    }

    public boolean isCleanup(CleanupWhat what)
    {
        return this.cleanupAll || whats.contains(what);
    }

    public boolean isCleanupAll()
    {
        return cleanupAll;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildCleanupOptions that = (BuildCleanupOptions)o;

        if (cleanupAll != that.cleanupAll) return false;
        if (whats != null ? !whats.equals(that.whats) : that.whats != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (cleanupAll ? 1 : 0);
        result = 31 * result + (whats != null ? whats.hashCode() : 0);
        return result;
    }
}
