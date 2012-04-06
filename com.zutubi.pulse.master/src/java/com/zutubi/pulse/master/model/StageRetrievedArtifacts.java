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
