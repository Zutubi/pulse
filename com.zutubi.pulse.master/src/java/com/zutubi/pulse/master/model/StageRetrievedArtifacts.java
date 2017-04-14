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

import java.util.List;

/**
 * Information about all artifacts retrieved by a stage, extracted from the Ivy retrieval report.
 */
public class StageRetrievedArtifacts
{
    private String stageName;
    private List<RetrievedArtifactSource> retrievedArtifacts;

    public StageRetrievedArtifacts(String stageName, List<RetrievedArtifactSource> artifacts) throws Exception
    {
        this.stageName = stageName;
        this.retrievedArtifacts = artifacts;
    }

    /**
     * @return the name of the stage that retrieved the artifacts
     */
    public String getStageName()
    {
        return stageName;
    }

    /**
     * @return true iff an ivy retrieval report was found and processed to determine the artifacts
     */
    public boolean isArtifactInformationAvailable()
    {
        return retrievedArtifacts != null;
    }

    /**
     * @return details for each artifact retrieved by the stage
     */
    public List<RetrievedArtifactSource> getRetrievedArtifacts()
    {
        return retrievedArtifacts;
    }
}
