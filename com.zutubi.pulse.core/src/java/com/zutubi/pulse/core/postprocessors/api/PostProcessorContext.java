package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;

/**
 * Context in which a post-processor executes.  Provides a high-level
 * interface for adding discovered information to the build.
 *
 * @see OutputPostProcessorSupport
 */
public interface PostProcessorContext
{
    /**
     * Returns the execution context for the build in which this processor is
     * running.
     *
     * @return the current build's context
     */
    ExecutionContext getExecutionContext();

    /**
     * Retrieves the current state for the command which produced the
     * artifact being processed.
     *
     * @return the current state of the executed command
     */
    ResultState getResultState();

    /**
     * Adds the suites and cases within the given suite directly to the test
     * results for the running recipe.  Note that the passed suite itself is
     * <strong>not</strong> added - the nested suites and cases are pulled
     * out of it.
     *
     * @param suite              suite holding the suites and cases to add
     * @param conflictResolution specifies how test case name conflicts should
     *                           be resolved
     */
    void addTests(TestSuiteResult suite, NameConflictResolution conflictResolution);

    /**
     * Adds a new feature to the artifact being processed.  Any error,
     * warning or information messages discovered in the artifact should be
     * added this way.  If the build state should also be updated, use
     * {@link #failCommand(String)} or {@link #errorCommand(String)} instead.
     *
     * @see LineBasedPostProcessorSupport
     * @see #failCommand(String)
     * @see #errorCommand(String)
     * @see #addFeatureToCommand(com.zutubi.pulse.core.engine.api.Feature)
     *
     * @param feature the discovered feature to add
     */
    void addFeature(Feature feature);

    /**
     * Fails the command and adds the given message as an error feature.  Use
     * when an error is found that should fail the build.
     *
     * @see #addFeature(com.zutubi.pulse.core.engine.api.Feature)
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
     * @see #addFeature(com.zutubi.pulse.core.engine.api.Feature)
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
     * @see #addFeature(com.zutubi.pulse.core.engine.api.Feature)
     * 
     * @param feature feature to add to the command result
     */
    void addFeatureToCommand(Feature feature);

    void addCustomField(String name, String value);
}
