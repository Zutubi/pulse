package com.zutubi.pulse.core.engine.api;

import com.google.common.base.Function;
import com.zutubi.pulse.core.test.api.IsOrderedIterable;
import com.zutubi.util.CollectionUtils;
import org.hamcrest.Matcher;

import static com.google.common.collect.Iterables.transform;
import static com.zutubi.pulse.core.test.api.Matchers.hasOrderedItems;
import static java.util.Arrays.asList;

/**
 * Helper class with static factories for building feature matchers.  These
 * matchers are intended to be used in post-processor tests.
 * <p/>
 * <b>Note</b> that these matchers only inspect the feature levels and
 * summaries, not their line numbers.  To compare line numbers, use
 * {@link junit.framework.Assert#assertEquals(Object, Object)} or an
 * {@link org.hamcrest.Matchers#equalTo(Object)} matcher.
 *
 * @see com.zutubi.pulse.core.postprocessors.api.PostProcessorTestCase
 */
public class FeatureMatchers
{
    /**
     * Creates a matcher that asserts a feature has the given level and summary.
     * Line numbers are ignored by this matcher.
     *
     * @see #hasLevelAndSummary(Feature)
     *
     * @param level   the level to match
     * @param summary the summary to match
     * @return a feature matcher that checks the summary and level
     */
    public static Matcher<Feature> hasLevelAndSummary(Feature.Level level, String summary)
    {
        return new HasLevelAndSummary(level, summary);
    }

    /**
     * Returns a matcher that asserts a feature has the same level and summary
     * as the given feature.  Line numbers are ignored by this matcher
     *
     * @see #hasLevelAndSummary(com.zutubi.pulse.core.engine.api.Feature.Level, String)
     *
     * @param feature feature to match against
     * @return a feature matcher that compares to the given feature
     */
    public static Matcher<Feature> hasLevelAndSummary(Feature feature)
    {
        return hasLevelAndSummary(feature.getLevel(), feature.getSummary());
    }

    /**
     * Returns a matcher that matches an iterable of features against the
     * levels and summaries of the given features.  Feature line numbers are
     * ignored.  The iterable must contain same number of features in the same
     * order.
     *
     * @see #hasOrderedMessages(com.zutubi.pulse.core.engine.api.Feature.Level, String[])
     *
     * @param features features to match against (levels and summaries only)
     * @return a matcher that tests an iterable of features
     */
    public static IsOrderedIterable<Feature> hasOrderedFeatures(Feature... features)
    {
        return hasOrderedItems(transform(asList(features), new Function<Feature, Matcher<? super Feature>>()
        {
            public Matcher<? super Feature> apply(Feature feature)
            {
                return hasLevelAndSummary(feature);
            }
        }));
    }

    /**
     * A convenience method to use in place of {@link #hasOrderedFeatures(Feature[])}
     * when all features have the same level.  Instead of creating many features,
     * just specify the level and each individual message.
     *
     * @see #hasOrderedFeatures (Feature[])
     * @see #hasOrderedErrors(String[])
     * @see #hasOrderedWarnings(String[])
     * @see #hasOrderedInfos(String[])
     *
     * @param level    the level for all features
     * @param messages the summaries of each feature, in the order that the
     *                 features themselves are expected
     * @return a matcher that tests an iterable for features with the given
     *         level
     */
    public static IsOrderedIterable<Feature> hasOrderedMessages(final Feature.Level level, String... messages)
    {
        return hasOrderedFeatures(CollectionUtils.mapToArray(messages, new Function<String, Feature>()
        {
            public Feature apply(String s)
            {
                return new Feature(level, s);
            }
        }, new Feature[messages.length]));
    }

    /**
     * A convenience method to use in place of {@link #hasOrderedFeatures(Feature[])}
     * when all features are errors.  Instead of creating many features, just
     * specify each individual summary.
     *
     * @see #hasOrderedMessages(com.zutubi.pulse.core.engine.api.Feature.Level, String[])
     * @see #hasOrderedWarnings(String[])
     * @see #hasOrderedInfos(String[])
     *
     * @param messages the summaries of each feature, in the order that the
     *                 features themselves are expected
     * @return a matcher that tests an iterable for error features
     */
    public static IsOrderedIterable<Feature> hasOrderedErrors(String... messages)
    {
        return hasOrderedMessages(Feature.Level.ERROR, messages);
    }

    /**
     * A convenience method to use in place of {@link #hasOrderedFeatures(Feature[])}
     * when all features are warnings.  Instead of creating many features, just
     * specify each individual summary.
     *
     * @see #hasOrderedMessages(com.zutubi.pulse.core.engine.api.Feature.Level, String[])
     * @see #hasOrderedErrors(String[])
     * @see #hasOrderedInfos(String[])
     *
     * @param messages the summaries of each feature, in the order that the
     *                 features themselves are expected
     * @return a matcher that tests an iterable for warning features
     */
    public static IsOrderedIterable<Feature> hasOrderedWarnings(String... messages)
    {
        return hasOrderedMessages(Feature.Level.WARNING, messages);
    }

    /**
     * A convenience method to use in place of {@link #hasOrderedFeatures(Feature[])}
     * when all features are informative.  Instead of creating many features,
     * just specify each individual summary.
     *
     * @see #hasOrderedMessages(com.zutubi.pulse.core.engine.api.Feature.Level, String[])
     * @see #hasOrderedErrors(String[])
     * @see #hasOrderedWarnings(String[])
     *
     * @param messages the summaries of each feature, in the order that the
     *                 features themselves are expected
     * @return a matcher that tests an iterable for informative features
     */
    public static IsOrderedIterable<Feature> hasOrderedInfos(String... messages)
    {
        return hasOrderedMessages(Feature.Level.INFO, messages);
    }
}
