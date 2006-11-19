package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.command.PulseCtl;
import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.slave.MasterProxyFactory;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 */
public class UpdateCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(UpdateCommand.class);

    private String build;
    private String master;
    private String token;
    private long slaveId;
    private String url;
    private long packageSize;
    private ConfigurationManager configurationManager;
    private MasterProxyFactory masterProxyFactory;
    private ShutdownManager shutdownManager;

    public UpdateCommand(String build, String master, String token, long slaveId, String url, long packageSize)
    {
        this.build = build;
        this.master = master;
        this.token = token;
        this.slaveId = slaveId;
        this.url = url;
        this.packageSize = packageSize;
    }

    public void run()
    {
        MasterService masterService = null;
        try
        {
            masterService = masterProxyFactory.createProxy(master);
        }
        catch (MalformedURLException e)
        {
            LOG.severe(e);
            return;
        }

        try
        {
            sendMessage(masterService, UpgradeState.STARTED);

            File pulseHome = new File(configurationManager.getEnvConfig().getPulseHome());
            File versionDir = PulseCtl.getVersionHome(pulseHome, build);

            if(!versionDir.exists())
            {
                // Make sure we can create the version dir (have write access)
                if(!versionDir.mkdirs())
                {
                    LOG.warning("Unable to create directory '" + versionDir.getAbsolutePath() + "'");
                    sendMessage(masterService, UpgradeState.FAILED, "Unable to create version directory: check that the user running the agent has write access to the agent install directory.");
                    return;
                }

                // Need to remove it again: should not be there if the upgrade fails
                versionDir.delete();

                // Need to obtain the package
                if(!downloadAndApplyUpdate(masterService, pulseHome, versionDir))
                {
                    // The package contains new components that we cannot
                    // update.  Log and give up.
                    LOG.warning("Unable to apply automatic update: please update manually");
                    return;
                }
            }

            sendMessage(masterService, UpgradeState.APPLYING);
            updateActiveVersion(pulseHome);

            sendMessage(masterService, UpgradeState.REBOOTING);
            shutdownManager.reboot();
        }
        catch (Exception e)
        {
            LOG.severe("Exception during update", e);
            sendMessage(masterService, UpgradeState.ERROR, "Error during update: " + e.getMessage());
        }
    }

    private void sendMessage(MasterService masterService, UpgradeState state)
    {
        sendMessage(masterService, state, null);
    }

    private void sendMessage(MasterService masterService, UpgradeState state, int progress)
    {
        sendMessage(masterService, state, progress, null);
    }

    private void sendMessage(MasterService masterService, UpgradeState state, String message)
    {
        sendMessage(masterService, state, -1, message);
    }

    private void sendMessage(MasterService masterService, UpgradeState state, int progress, String message)
    {
        try
        {
            masterService.upgradeStatus(token, new UpgradeStatus(slaveId, state, progress, message));
        }
        catch (Exception e)
        {
            LOG.severe("Error reporting upgrade status to master", e);
        }
    }

    private boolean downloadAndApplyUpdate(MasterService masterService, File pulseHome, File versionDir) throws IOException
    {
        File tempDir = new File(configurationManager.getSystemPaths().getTmpRoot(), RandomUtils.randomString(3));
        tempDir.mkdirs();

        try
        {
            URL packageUrl = new URL(url);
            File packageFile = new File(tempDir, build + ".zip");
            File unpackDir = new File(tempDir, "un");

            sendMessage(masterService, UpgradeState.DOWNLOADING);
            IOUtils.downloadFile(packageUrl, packageFile);

            sendMessage(masterService, UpgradeState.APPLYING);
            FileSystemUtils.extractZip(packageFile, unpackDir);

            // There will be a single directory under unpackDir (e.g. pulse-agent-1.1.1)
            File[] children = unpackDir.listFiles();
            if(children.length != 1)
            {
                throw new PulseRuntimeException("Unexpected number of entries at top level of package.");
            }

            File packageRoot = children[0];
            if(!packageRoot.isDirectory())
            {
                throw new PulseRuntimeException("Expected top level of package to contain a directory.");
            }

            File packageVersionDir = PulseCtl.getVersionHome(packageRoot, build);
            if(!packageVersionDir.isDirectory())
            {
                throw new PulseRuntimeException("Package does not contain expected version directory");
            }

            // Ignore this in the check below
            File packageVersionFile = PulseCtl.getActiveVersionFile(packageRoot);
            packageVersionFile.delete();

            if(!checkUnversionedComponents(pulseHome, packageRoot, true))
            {
                sendMessage(masterService, UpgradeState.FAILED, "Unable to apply this update automatically.  A manual upgrade is required.");
                return false;
            }

            // This comes last: we only want to do this when we are happy to upgrade to this version!
            if(!packageVersionDir.renameTo(versionDir))
            {
                throw new PulseRuntimeException("Unable to rename package version directory '" + packageVersionDir.getAbsolutePath() + "' into Pulse home '" + versionDir.getAbsolutePath() + "'");
            }

            return true;
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    /**
     * Tests if all the non-versioned files/directories in the package (i.e.
     * files not under the versions dir) are present and identical in our
     * current installation.
     *
     * @param installDir
     * @param packageDir
     */
    private boolean checkUnversionedComponents(File installDir, File packageDir, boolean top) throws IOException
    {
        String[] children = packageDir.list();
        for(String child: children)
        {
            if(top && child.equals("versions"))
            {
                // Ignore the versioned components
                continue;
            }

            File packageFile = new File(packageDir, child);
            File installFile = new File(installDir, child);

            if(packageFile.isDirectory())
            {
                if(!installFile.isDirectory())
                {
                    return false;
                }

                if(!checkUnversionedComponents(installFile, packageFile, false))
                {
                    return false;
                }
            }
            else
            {
                if(!installFile.isFile())
                {
                    return false;
                }

                if(!FileSystemUtils.filesMatch(installFile, packageFile))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private void updateActiveVersion(File pulseHome) throws IOException
    {
        File activeVersionFile = PulseCtl.getActiveVersionFile(pulseHome);
        FileSystemUtils.createFile(activeVersionFile, build);
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }
}
