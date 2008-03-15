package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.NumericalRevision;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.WorkingCopy;
import com.zutubi.pulse.core.scm.WorkingCopyStatus;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;
import com.zutubi.pulse.personal.PersonalBuildSupport;
import com.zutubi.pulse.personal.PersonalBuildUI;

import java.io.File;

/**
 */
public class PerforceWorkingCopy extends PersonalBuildSupport implements WorkingCopy
{
    public static final String PROPERTY_CONFIRM_RESOLVE = "p4.confirm.resolve";

    private static final int RETRY_LIMIT = 5;

    private PerforceCore core;
    private ConfigSupport configSupport;

    public PerforceWorkingCopy(File base, Config config)
    {
        this.core = new PerforceCore();
        configSupport = new ConfigSupport(config);
    }

    public boolean matchesLocation(String location) throws ScmException
    {
        // Location is <template client>@<port>
        String[] pieces = location.split("@");

        // $ p4 set
        // P4EDITOR=C:\WINDOWS\System32\notepad.exe (set)
        // P4JOURNAL=journal (set -s)
        // P4LOG=log (set -s)
        // P4PORT=10.0.0.3:1666
        // P4ROOT=C:\Program Files\Perforce (set -s)
        // P4USER=Jason (set)
        PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_SET), COMMAND_SET);
        String[] lines = core.splitLines(result);
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

                    if(!value.equals(pieces[1]))
                    {
                        warning("P4PORT setting '" + value + "' does not match Pulse project's P4PORT '" + pieces[1] + "'");
                        return false;
                    }
                }
            }
        }

        // TODO: check the client mapping?  This is difficult...many false positives methinks
        
        return true;
    }

    public WorkingCopyStatus getStatus() throws ScmException
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
        int i = 0;
        do
        {
            revision = core.getLatestRevisionForFiles(null);
            // convert revision.
            status = new WorkingCopyStatus(core.getClientRoot(), core.convertRevision(revision));
            PerforceFStatHandler handler = new PerforceFStatHandler(getUi(), status);
            core.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, "//...");

            checkRevision = core.getLatestRevisionForFiles(null);
        } while (!checkRevision.equals(revision) && i++ < RETRY_LIMIT);

        if(i == RETRY_LIMIT)
        {
            throw new ScmException("Retry limit hit waiting for revision to stabilise");    
        }

        return status;
    }

    public WorkingCopyStatus getLocalStatus(String... spec) throws ScmException
    {
        WorkingCopyStatus status = new WorkingCopyStatus(core.getClientRoot());
        PerforceFStatHandler handler = new PerforceFStatHandler(getUi(), status, false);

        // Spec can be either a changelist # or a list of files
        String changelist;
        if(spec.length == 1 && spec[0].startsWith(":"))
        {
            // It's a changelist
            changelist = spec[0].substring(1);
            if(changelist.length() == 0)
            {
                throw new ScmException("Empty changelist name specified (" + spec[0] + ")");
            }

            core.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, FLAG_FILES_OPENED, FLAG_AFFECTED_CHANGELIST, changelist, "//...");
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

            core.runP4WithHandler(handler, null, commands);
        }
        else
        {
            // Emulate submit behaviour: default changelist
            core.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, FLAG_FILES_OPENED, FLAG_AFFECTED_CHANGELIST, "default", "//...");
        }

        return status;
    }

    public Revision update() throws ScmException
    {
        NumericalRevision revision = core.getLatestRevisionForFiles(null);

        PerforceSyncHandler syncHandler = new PerforceSyncHandler(getUi());
        core.runP4WithHandler(syncHandler, null, getP4Command(COMMAND_SYNC), COMMAND_SYNC, "@" + revision.getRevisionString());

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
                    return core.convertRevision(revision);
                }
            }

            status("Running auto-resolve...");
            enterContext();
            try
            {
                core.runP4WithHandler(new PerforceProgressPrintingHandler(getUi(), false), null, getP4Command(COMMAND_RESOLVE), COMMAND_RESOLVE, FLAG_AUTO_MERGE);
            }
            finally
            {
                exitContext();
            }
            status("Resolve complete.");
        }

        return core.convertRevision(revision);
    }

    PerforceCore getClient()
    {
        return core;
    }
}
