package com.zutubi.pulse.master.api;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.tove.config.ConfigurationArchiver;
import com.zutubi.tove.config.ToveRuntimeException;

/**
 * A version checker that allows restoration from an archive as long as there are no upgrade tasks between the archive
 * version and our version.
 */
public class NoInterveningUpgradesVersionChecker implements ConfigurationArchiver.VersionChecker
{
    private UpgradeManager upgradeManager;

    public NoInterveningUpgradesVersionChecker(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }

    public void checkVersion(String version) throws ToveRuntimeException
    {
        if (version.equals("@BUILD_NUMBER@"))
        {
            // Allow for testing.
            return;
        }

        try
        {
            int thatBuild = Integer.parseInt(version);
            int thisBuild = Version.getVersion().getBuildNumberAsInt();
            if (thatBuild > thisBuild)
            {
                throw new ToveRuntimeException("Invalid archive version '" + version + "' cannot restore from an archive created by a later Pulse version");
            }

            if (thatBuild != thisBuild && upgradeManager.isUpgradeRequired(thatBuild, thisBuild))
            {
                throw new ToveRuntimeException("Invalid archive version '" + version + "' upgrades have been applied since that version");
            }
        }
        catch (NumberFormatException e)
        {
            throw new ToveRuntimeException("Invalid archive version '" + version + "': cannot convert to build number");
        }
    }
}
