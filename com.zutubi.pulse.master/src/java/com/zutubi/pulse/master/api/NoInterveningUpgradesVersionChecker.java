package com.zutubi.pulse.master.api;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.tove.config.ConfigurationArchiver;
import com.zutubi.tove.config.ToveRuntimeException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A version checker that allows restoration from an archive as long as there are no upgrade tasks between the archive
 * version and our version.
 */
public class NoInterveningUpgradesVersionChecker implements ConfigurationArchiver.VersionChecker
{
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

            // The upgrade context is only kept around while needed, so we need to load it again
            // here to get a fresh UpgradeManager.
            ClassPathXmlApplicationContext upgradeContext = new ClassPathXmlApplicationContext(new String[]{"classpath:/com/zutubi/pulse/master/bootstrap/context/upgradeContext.xml"}, false, SpringComponentContext.getContext());
            try
            {
                upgradeContext.refresh();
                UpgradeManager upgradeManager = (UpgradeManager) upgradeContext.getBean("upgradeManager");
                if (thatBuild != thisBuild && upgradeManager.isUpgradeRequired(thatBuild, thisBuild))
                {
                    throw new ToveRuntimeException("Invalid archive version '" + version + "' upgrades have been applied since that version");
                }
            }
            finally
            {
                upgradeContext.close();
            }
        }
        catch (NumberFormatException e)
        {
            throw new ToveRuntimeException("Invalid archive version '" + version + "': cannot convert to build number");
        }
    }
}
