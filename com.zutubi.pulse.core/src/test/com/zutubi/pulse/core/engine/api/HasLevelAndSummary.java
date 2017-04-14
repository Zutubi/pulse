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

package com.zutubi.pulse.core.engine.api;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A Hamcrest matcher for features that just tests their level and summary,
 * ignoring other properties (in particular the line numbers).
 */
public class HasLevelAndSummary extends TypeSafeMatcher<Feature>
{
    private Feature.Level level;
    private String summary;

    /**
     * Creates a new matcher that checks for the given level and summary.
     *
     * @param level   the level to test for
     * @param summary the summary to test for
     */
    public HasLevelAndSummary(Feature.Level level, String summary)
    {
        this.level = level;
        this.summary = summary;
    }

    public boolean matchesSafely(Feature feature)
    {
        return feature.getLevel() == level && feature.getSummary().equals(summary);
    }

    public void describeTo(Description description)
    {
        description.appendText("has level ");
        description.appendValue(level);
        description.appendText(" and summary '");
        description.appendValue(summary);
        description.appendText("'");
    }
}
