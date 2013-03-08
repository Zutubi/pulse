package com.zutubi.pulse.slave.command;

import com.google.common.io.Files;
import com.zutubi.pulse.command.PulseCtl;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.UpgradeState;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.pulse.slave.MasterProxyFactory;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 */
public class UpdateCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(UpdateCommand.class);

    /**
     * The pulse entry point script, may be edited by the user.
     */
    private static final String PULSE_SCRIPT = "pulse";
    /**
     * Files with these extensions may normally be edited by the user.
     */
    private static final List<String> IGNORED_EXTENSIONS = Arrays.asList("bat", "conf", "sh", "txt");
    /**
     * The package directory containing versioned components.
     */
    private static final String VERSIONS_DIRECTORY = "versions";

    private String build;
    private String master;
    private String token;
    private long hostId;
    private String url;
    private ConfigurationManager configurationManager;
    private MasterProxyFactory masterProxyFactory;
    private ShutdownManager shutdownManager;
    private JettyServerManager jettyServerManager;

    public UpdateCommand(String build, String master, String token, long hostId, String url)
    {
        this.build = build;
        this.master = master;
        this.token = token;
        this.hostId = hostId;
        this.url = url;
    }

    public void run()
    {
        MasterService masterService;
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

            jettyServerManager.stop(false);
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

    private void sendMessage(MasterService masterService, UpgradeState state, String message)
    {
        sendMessage(masterService, state, -1, message);
    }

    private void sendMessage(MasterService masterService, UpgradeState state, int progress, String message)
    {
        try
        {
            masterService.upgradeStatus(token, new UpgradeStatus(hostId, state, progress, message));
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
            PulseZipUtils.extractZip(packageFile, unpackDir);

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

            checkUnversionedComponents(pulseHome, packageRoot, true);
            
            // This comes last: we only want to do this when we are happy to upgrade to this version!
            try
            {
                FileSystemUtils.robustRename(packageVersionDir, versionDir);
            }
            catch (IOException e)
            {
                throw new PulseRuntimeException("Unable to rename package version directory: " + e.getMessage(), e);
            }

            return true;
        }
        finally
        {
            try
            {
                FileSystemUtils.rmdir(tempDir);
            }
            catch (IOException e)
            {
                // Ignore
            }
        }
    }

    /**
 	 * Tests if all the non-versioned files/directories in the package (i.e.
 	 * files not under the versions dir) are present and identical in our
 	 * current installation, and warns the user if they are not.  Note that
     * this implementation ignores files that the user may reasonably choose
     * to edit.
 	 *
 	 * @param installDir the directory within the current installation
 	 * @param packageDir the corresponding directory in the incoming package
     * @param top        true iff this is the top level directory
     * @throws java.io.IOException is there is an error comparing files
 	 */
    private void checkUnversionedComponents(File installDir, File packageDir, boolean top) throws IOException
    {
        for (String child : FileSystemUtils.list(packageDir))
        {
            if (isFileIgnored(child, top))
            {
                // Ignore the versioned components
                continue;
            }

            File packageFile = new File(packageDir, child);
            File installFile = new File(installDir, child);

            if (packageFile.isDirectory())
            {
                if (!installFile.isDirectory())
                {
                    LOG.warning("Existing installation has no directory '" + installFile.getAbsolutePath() + "', incoming package does.");
                }

                checkUnversionedComponents(installFile, packageFile, false);
            }
            else
            {
                if (!installFile.isFile())
                {
                    LOG.warning("Existing installation has no file '" + installFile.getAbsolutePath() + "', incoming package does.");
                }

                if (!FileSystemUtils.filesMatch(installFile, packageFile))
                {
                    LOG.warning("Existing installation file '" + installFile.getAbsolutePath() + "', does not match that from incoming package.");
                }
            }
        }
    }

    private boolean isFileIgnored(String file, boolean top)
    {
        if (top)
        {
            return file.equals(VERSIONS_DIRECTORY);
        }
        else
        {
            return IGNORED_EXTENSIONS.contains(FileSystemUtils.getFilenameExtension(file)) || PULSE_SCRIPT.equals(file);
        }
    }

    private void updateActiveVersion(File pulseHome) throws IOException
    {
        File activeVersionFile = PulseCtl.getActiveVersionFile(pulseHome);
        Files.write(build, activeVersionFile, Charset.defaultCharset());
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

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }
}
