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

package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorContext;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;

import java.util.Map;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Holds contextual information for the post processing of a single artifact
 * file.
 */
public class DefaultPostProcessorContext implements PostProcessorContext
{
    private StoredFileArtifact artifact;
    private CommandResult commandResult;
    private int featureLimit;
    private ExecutionContext executionContext;
    private boolean featuresDiscarded = false;

    public DefaultPostProcessorContext(StoredFileArtifact artifact, CommandResult commandResult, int featureLimit, ExecutionContext executionContext)
    {
        this.artifact = artifact;
        this.commandResult = commandResult;
        this.featureLimit = featureLimit;
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
        if (featureLimit > 0)
        {
            int newFeatureCount = artifact.getFeatures().size() + 1;
            if (newFeatureCount < featureLimit)
            {
                artifact.addFeature(convertFeature(feature));
            }
            else if (newFeatureCount == featureLimit)
            {
                if (featuresDiscarded)
                {
                    // Can we evict a feature of lower severity?
                    if (artifact.evictFeature(feature.getLevel()))
                    {
                        artifact.addFeature(convertFeature(feature));                
                    }
                }
                else
                {
                    artifact.addFeature(convertFeature(feature));
                }
            }
            else
            {
                featuresDiscarded = true;
    
                // Always evict one feature to make way for the informative
                // message.  We don't add it yet as we want to avoid it being
                // evicted.
                if (!artifact.evictFeature(Feature.Level.ERROR))
                {
                    artifact.getFeatures().remove(artifact.getFeatures().size() - 1);
                }
                
                if (artifact.evictFeature(feature.getLevel()))
                {
                    artifact.addFeature(convertFeature(feature));
                }
            }
        }
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

    public void addCustomField(FieldScope scope, String name, String value)
    {
        if (!StringUtils.stringSet(name))
        {
            throw new IllegalArgumentException("Name must be specified");
        }
        
        @SuppressWarnings({"unchecked"})
        Map<Pair<FieldScope, String>, String> fields = executionContext.getValue(NAMESPACE_INTERNAL, PROPERTY_CUSTOM_FIELDS, Map.class);
        fields.put(asPair(scope, name), value);
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

    public boolean isFeaturesDiscarded()
    {
        return featuresDiscarded;
    }
}
