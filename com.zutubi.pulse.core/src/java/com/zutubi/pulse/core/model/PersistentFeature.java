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

package com.zutubi.pulse.core.model;

import com.google.common.base.Objects;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.util.StringUtils;

public class PersistentFeature extends Entity
{
    private static final int MAX_SUMMARY_LENGTH = 4095;
    private static final String SUMMARY_TRIM_MESSAGE = "... [trimmed]";

    private Feature.Level level;
    /**
     * A simple textual summary of the feature for display to users.
     */
    private String summary;

    public PersistentFeature()
    {

    }

    public PersistentFeature(Feature.Level level, String summary)
    {
        this.level = level;
        if (summary != null)
        {
            this.summary = StringUtils.trimmedString(summary, MAX_SUMMARY_LENGTH, SUMMARY_TRIM_MESSAGE);
        }
    }

    public Feature.Level getLevel()
    {
        return level;
    }

    public String getSummary()
    {
        return summary;
    }

    private void setSummary(String summary)
    {
        this.summary = summary;
    }

    public void appendToSummary(String s)
    {
        this.summary = StringUtils.trimmedString(summary == null ? s : summary + s, MAX_SUMMARY_LENGTH, SUMMARY_TRIM_MESSAGE);
    }

    private String getLevelName()
    {
        return level.name();
    }

    private void setLevelName(String name)
    {
        level = Feature.Level.valueOf(name);
    }

    public boolean isPlain()
    {
        return false;
    }

    public boolean equals(Object o)
    {
        if (o instanceof PersistentFeature)
        {
            PersistentFeature other = (PersistentFeature) o;
            return other.level == level && Objects.equal(other.summary, summary);
        }

        return false;
    }

    public int hashCode()
    {
        return summary.hashCode();
    }
}
