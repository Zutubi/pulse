package com.zutubi.pulse.master.rest.model.setup;

import com.zutubi.pulse.Version;

/**
 * Models a Pulse server version.
 */
public class VersionModel
{
    private final String buildDate;
    private final String buildNumber;
    private final String versionNumber;

    public VersionModel(Version version)
    {
        buildDate = version.getBuildDate();
        buildNumber = version.getBuildNumber();
        versionNumber = version.getVersionNumber();
    }

    public String getBuildDate()
    {
        return buildDate;
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public String getVersionNumber()
    {
        return versionNumber;
    }
}
