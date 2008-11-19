package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.webwork.Urls;

/**
 * An action to download a raw artifact.  Actually just checks that a known
 * artifact is specified, and if so redirects to the file/ URL so the artifact
 * is served by Jetty.
 */
public class DownloadArtifactAction extends FileArtifactActionBase
{
    private String url;

    public String getUrl()
    {
        return url;
    }

    public String execute()
    {
        url = new Urls("").fileFileArtifact(getRequiredArtifact(), getRequiredFileArtifact());
        return SUCCESS;
    }
}