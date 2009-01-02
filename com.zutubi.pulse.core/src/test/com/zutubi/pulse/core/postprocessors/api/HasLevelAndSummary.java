package com.zutubi.pulse.core.postprocessors.api;

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
