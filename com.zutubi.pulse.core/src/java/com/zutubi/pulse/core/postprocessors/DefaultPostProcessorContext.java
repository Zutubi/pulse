package com.zutubi.pulse.core.postprocessors;

import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_TEST_RESULTS;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.*;

/**
 * Holds contextual information for the post processing of a single artifact
 * file.  Supports the addition of features to the result, with fail on error
 * and fail on warning handling built in.
 */
public class DefaultPostProcessorContext implements PostProcessorContext
{
    private StoredFileArtifact artifact;
    private CommandResult commandResult;
    private ExecutionContext executionContext;
    private boolean failOnError;
    private boolean failOnWarning;

    public DefaultPostProcessorContext(StoredFileArtifact artifact, CommandResult commandResult, ExecutionContext executionContext, boolean failOnError, boolean failOnWarning)
    {
        this.artifact = artifact;
        this.commandResult = commandResult;
        this.executionContext = executionContext;
        this.failOnError = failOnError;
        this.failOnWarning = failOnWarning;
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

    public TestSuiteResult getTestSuite()
    {
        return executionContext.getValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, TestSuiteResult.class);
    }
    
    public ResultState getResultState()
    {
        return commandResult.getState();
    }

    public void addFeature(Feature feature)
    {
        switch(feature.getLevel())
        {
            case ERROR:
                if (failOnError)
                {
                    commandResult.failure("Error features detected");
                }
                break;
            case WARNING:
                if (failOnWarning)
                {
                    commandResult.failure("Warning features detected");
                }
                break;
        }
        artifact.addFeature(feature);
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
        commandResult.addFeature(feature);
    }
}
