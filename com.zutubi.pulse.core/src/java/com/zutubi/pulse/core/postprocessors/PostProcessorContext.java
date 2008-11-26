package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;

/**
 * Context in which a post-processor executes.  Provides a high-level
 * interface for adding discovered information to the build.
 *
 * @see PostProcessorSupport
 */
public interface PostProcessorContext
{
    /**
     * Retrieves the current state for the command which produced the
     * artifact being processed.
     *
     * @return the current state of the executed command
     */
    ResultState getResultState();

    void addTestSuite(TestSuiteResult suite, NameConflictResolution conflictResolution);

    /**
     * Adds a new feature to the artifact being processed.  Any error,
     * warning or information messages discovered in the artifact should be
     * added this way.  If the build state should also be updated, use
     * {@link #failCommand(String)} or {@link #errorCommand(String)} instead.
     *
     * @see LineBasedPostProcessorSupport
     * @see #failCommand(String)
     * @see #errorCommand(String)
     * @see #addFeatureToCommand(com.zutubi.pulse.core.postprocessors.api.Feature)
     *
     * @param feature the discovered feature to add
     */
    void addFeature(Feature feature);

    /**
     * Fails the command and adds the given message as an error feature.  Use
     * when an error is found that should fail the build.
     *
     * @see #addFeature(com.zutubi.pulse.core.postprocessors.api.Feature)
     * @see #errorCommand(String)
     *
     * @param message the error message discovered
     */
    void failCommand(String message);

    /**
     * Sets the command state to 'error' and adds the given message as an
     * error feature.  Use when an error is found that should mark the build
     * as an error.
     *
     * @see #addFeature(com.zutubi.pulse.core.postprocessors.api.Feature)
     * @see #failCommand(String)
     *
     * @param message the error message discovered
     */
    void errorCommand(String message);

    /**
     * Adds a feature directly to the command result, rather than the
     * artifact.  Use when a feature is encountered that is not specific to
     * the artifact - for example when there is an error trying to read the
     * artifact file.
     *
     * @see #addFeature(com.zutubi.pulse.core.postprocessors.api.Feature)
     * 
     * @param feature feature to add to the command result
     */
    void addFeatureToCommand(Feature feature);
}
