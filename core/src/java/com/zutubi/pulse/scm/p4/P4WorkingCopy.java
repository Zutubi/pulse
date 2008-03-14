package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PersonalBuildSupport;
import com.zutubi.pulse.personal.PersonalBuildUI;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.WorkingCopy;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import static com.zutubi.pulse.scm.p4.P4Constants.*;

import java.io.File;
import java.util.Properties;

/**
 */
public class P4WorkingCopy extends PersonalBuildSupport implements WorkingCopy
{
    public static final String PROPERTY_CONFIRM_RESOLVE = "p4.confirm.resolve";

    private P4Client client;
    private ConfigSupport configSupport;

    public P4WorkingCopy(File base, Config config)
    {
        this.client = new P4Client();
        configSupport = new ConfigSupport(config);
    }

    public boolean matchesRepository(Properties repositoryDetails) throws SCMException
    {
        String port = (String) repositoryDetails.get(PROPERTY_PORT);
        if (port != null)
        {
            // $ p4 set
            // P4EDITOR=C:\WINDOWS\System32\notepad.exe (set)
            // P4JOURNAL=journal (set -s)
            // P4LOG=log (set -s)
            // P4PORT=10.0.0.3:1666
            // P4ROOT=C:\Program Files\Perforce (set -s)
            // P4USER=Jason (set)
            P4Client.P4Result result = client.runP4(null, getP4Command(COMMAND_SET), COMMAND_SET);
            String[] lines = client.splitLines(result);
            for(String line: lines)
            {
                int index = line.indexOf('=');
                if(index > 0 && index < line.length() - 1)
                {
                    String key = line.substring(0, index);
                    if(key.equals(ENV_PORT))
                    {
                        String value = line.substring(index + 1);
                        value = value.split(" ")[0];

                        if(!value.equals(port))
                        {
                            warning("P4PORT setting '" + value + "' does not match Pulse project's P4PORT '" + port + "'");
                            return false;
                        }
                    }
                }
            }
        }

        // TODO: check the client mapping?  This is difficult...many false positives methinks
        
        return true;
    }

    public WorkingCopyStatus getStatus() throws SCMException
    {
        WorkingCopyStatus status;
        NumericalRevision revision;
        NumericalRevision checkRevision;

        // A little strange, perhaps.  We first get the latest revision, then
        // run an fstat.  Unfortunately, restricting the fstat to the
        // revision prevents some required things being reported (e.g. files
        // that are open for add).  Instead, we double-check the revision
        // after the fstat.  In the unlikely event that it has changed, we
        // just go again.
        do
        {
            revision = client.getLatestRevisionForFiles(null);
            status = new WorkingCopyStatus(client.getClientRoot(), revision);
            P4FStatHandler handler = new P4FStatHandler(getUi(), status);
            client.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, "//...");

            checkRevision = client.getLatestRevisionForFiles(null);
        } while (!checkRevision.equals(revision));

        return status;
    }

    public WorkingCopyStatus getLocalStatus(String... spec) throws SCMException
    {
        WorkingCopyStatus status = new WorkingCopyStatus(client.getClientRoot());
        P4FStatHandler handler = new P4FStatHandler(getUi(), status, false);

        // Spec can be either a changelist # or a list of files
        String changelist;
        if(spec.length == 1 && spec[0].startsWith(":"))
        {
            // It's a changelist
            changelist = spec[0].substring(1);
            if(changelist.length() == 0)
            {
                throw new SCMException("Empty changelist name specified (" + spec[0] + ")");
            }

            client.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, FLAG_FILES_OPENED, FLAG_AFFECTED_CHANGELIST, changelist, "//...");
        }
        else if(spec.length > 0)
        {
            // Then it is a list of files
            String[] commands = new String[spec.length + 4];
            commands[0] = getP4Command(COMMAND_FSTAT);
            commands[1] = COMMAND_FSTAT;
            commands[2] = FLAG_PATH_IN_DEPOT_FORMAT;
            commands[3] = FLAG_FILES_OPENED;
            System.arraycopy(spec, 0, commands, 4, spec.length);

            client.runP4WithHandler(handler, null, commands);
        }
        else
        {
            // Emulate submit behaviour: default changelist
            client.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, FLAG_FILES_OPENED, FLAG_AFFECTED_CHANGELIST, "default", "//...");
        }

        return status;
    }

    public Revision update() throws SCMException
    {
        NumericalRevision revision = client.getLatestRevisionForFiles(null);

        P4SyncHandler syncHandler = new P4SyncHandler(getUi());
        client.runP4WithHandler(syncHandler, null, getP4Command(COMMAND_SYNC), COMMAND_SYNC, "@" + revision.getRevisionString());

        if(syncHandler.isResolveRequired())
        {
            if(configSupport.getBooleanProperty(PROPERTY_CONFIRM_RESOLVE, true))
            {
                PersonalBuildUI.Response response = ynaPrompt("Some files must be resolved.  Auto-resolve now?", PersonalBuildUI.Response.YES);
                if(response.isPersistent())
                {
                    configSupport.setBooleanProperty(PROPERTY_CONFIRM_RESOLVE, !response.isAffirmative());
                }

                if(!response.isAffirmative())
                {
                    return revision;
                }
            }

            status("Running auto-resolve...");
            enterContext();
            try
            {
                client.runP4WithHandler(new P4ProgressPrintingHandler(getUi(), false), null, getP4Command(COMMAND_RESOLVE), COMMAND_RESOLVE, FLAG_AUTO_MERGE);
            }
            finally
            {
                exitContext();
            }
            status("Resolve complete.");
        }

        return revision;
    }

    P4Client getClient()
    {
        return client;
    }
}
