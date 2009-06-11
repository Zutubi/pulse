package com.zutubi.pulse.core.postprocessors;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;

import java.util.Map;

/**
 * Holds contextual information for the post processing of a single artifact
 * file.
 */
public class DefaultPostProcessorContext implements PostProcessorContext
{
    private StoredFileArtifact artifact;
    private CommandResult commandResult;
    private ExecutionContext executionContext;

    public DefaultPostProcessorContext(StoredFileArtifact artifact, CommandResult commandResult, ExecutionContext executionContext)
    {
        this.artifact = artifact;
        this.commandResult = commandResult;
        this.executionContext = executionContext;
    }

    public StoredFileArtifact getArtifact()
    {
        return artifact;
    }

    public CommandResult getCommandResult()
    {
        return commandResult;
    }

    public ExecutionContext getExecutionContext()
    {
        return executionContext;
    }

    public ResultState getResultState()
    {
        return commandResult.getState();
    }

    public void addTests(TestSuiteResult suite, NameConflictResolution conflictResolution)
    {
        PersistentTestSuiteResult recipeSuite = executionContext.getValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, PersistentTestSuiteResult.class);
        PersistentTestSuiteResult persistentSuite = new PersistentTestSuiteResult(suite, conflictResolution);

        // Fold the suite directly into the recipe suite.
        for (PersistentTestSuiteResult nestedSuite: persistentSuite.getSuites())
        {
            recipeSuite.add(nestedSuite);
        }

        for (PersistentTestCaseResult nestedCase: persistentSuite.getCases())
        {
            recipeSuite.add(nestedCase);
        }
    }

    public void addFeature(Feature feature)
    {
        artifact.addFeature(convertFeature(feature));
    }

    public void failCommand(String message)
    {
        commandResult.failure(message);
    }

    public void errorCommand(String message)
    {
        commandResult.error(message);
    }

    public void addFeatureToCommand(Feature feature)
    {
        commandResult.addFeature(convertFeature(feature));
    }

    public void addCustomField(String name, String value)
    {
        @SuppressWarnings({"unchecked"})
        Map<String, String> fields = executionContext.getValue(NAMESPACE_INTERNAL, PROPERTY_CUSTOM_FIELDS, Map.class);
        fields.put(name, value);
    }

    private PersistentFeature convertFeature(Feature feature)
    {
        if (feature.getLineNumber() == Feature.LINE_UNKNOWN)
        {
            return new PersistentFeature(feature.getLevel(), feature.getSummary());
        }
        else
        {
            return new PersistentPlainFeature(feature.getLevel(), feature.getSummary(), feature.getFirstLine(), feature.getLastLine(), feature.getLineNumber());
        }
    }
}
