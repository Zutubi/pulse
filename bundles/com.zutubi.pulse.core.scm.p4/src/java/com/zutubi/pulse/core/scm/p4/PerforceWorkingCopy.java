package com.zutubi.pulse.core.scm.p4;

import com.zutubi.diff.unified.UnifiedPatch;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatus;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatusBuilder;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.util.StringUtils;
import com.zutubi.util.config.Config;
import com.zutubi.util.config.ConfigSupport;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;

/**
 * Implementation of {@link WorkingCopy} that interfaces with Perforce by
 * wrapping the p4 command-line tool.
 */
public class PerforceWorkingCopy implements WorkingCopy, WorkingCopyStatusBuilder
{
    private static final Messages I18N = Messages.getInstance(PerforceWorkingCopy.class);

    // Note that the preferred way to set these standard Perforce properties is
    // to just use regular p4 configuration (e.g. environment, P4CONFIG, etc).
    // They are supported by this implementation for completeness and testing.
    public static final String PROPERTY_CLIENT = "p4.client";
    public static final String PROPERTY_PORT   = "p4.port";
    public static final String PROPERTY_USER   = "p4.user";

    // Pulse-specific perforce configuration properties.
    public static final String PROPERTY_CONFIRM_RESOLVE = "p4.confirm.resolve";
    public static final String PROPERTY_PRE_2004_2 = "p4.pre.2004.2";

    private static final int GUESS_REVISION_RETRIES = 5;

    public Set<WorkingCopyCapability> getCapabilities()
    {
        return EnumSet.allOf(WorkingCopyCapability.class);
    }

    public boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException
    {
        // Location is <template workspace/view hash>@<port>
        String[] pieces = location.split("@");

        // $ p4 set
        // P4EDITOR=C:\WINDOWS\System32\notepad.exe (set)
        // P4JOURNAL=journal (set -s)
        // P4LOG=log (set -s)
        // P4PORT=10.0.0.3:1666
        // P4ROOT=C:\Program Files\Perforce (set -s)
        // P4USER=Jason (set)
        PerforceCore core = createCore(context);
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
                        context.getUI().warning(I18N.format("warning.p4.port", value, pieces[1]));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException
    {
        PerforceCore core = createCore(context);
        return core.getLatestRevisionForFiles(null, "//" + getClientName(core) + "/...");
    }

    public Revision guessLocalRevision(WorkingCopyContext context) throws ScmException
    {
        PerforceCore core = createCore(context);
        String client = getClientName(core);

        // This technique is described here:
        //   http://kb.perforce.com/UserTasks/ManagingFile..Changelists/DeterminingC..OfWorkspace
        //
        // Basically, p4 changes -m1 @<client name> *almost* does what we want,
        // but:
        //   1) It doesn't take into account changes submitted from this client
        //      since it was last synced; and
        //   2) If the latest changelist "sunc" to contains only deleted files,
        //      it will not be reported.
        //   3) It is possible that the client is not sunc to a single
        //      revision, different files may be sunc to different revisions.
        // Note that my own testing suggests 1) is not actually a problem
        // (unless I am missing what they mean), but 2) and 3) do happen.  To
        // overcome this, we can do a dry-run sync to the given changelist and
        // see if it would do anything.  If  it reports that something would
        // change, we get all submitted changes for the project after our
        // original guess and try a few of them.  We can't try forever due to
        // 3) - so after a few attempts we guess that we have hit this case and
        // bail.
        PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_CHANGES), COMMAND_CHANGES, FLAG_MAXIMUM, "1", "@" + client);
        Matcher matcher = PATTERN_CHANGES.matcher(result.stdout);
        if (!matcher.find())
        {
            throw new ScmException("p4 changes did not return any changelists, expecting one");
        }

        long revision = Long.parseLong(matcher.group(1));
        if (isSyncNonTrivial(core, revision))
        {
            revision = searchForLocalRevisionInProjectChanges(core, client, revision);
        }

        return new Revision(revision);
    }

    private long searchForLocalRevisionInProjectChanges(PerforceCore core, String client, long revision) throws ScmException
    {
        // Looks like we hit a tricky case.  Get the next few changelists
        // submitted for our project, and try them.
        List<Long> tried = new LinkedList<Long>();
        tried.add(revision);

        PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_CHANGES), COMMAND_CHANGES, FLAG_STATUS, VALUE_SUBMITTED, "//" + client + "/...@" + (revision + 1) + ",#head");
        Matcher matcher = PATTERN_CHANGES.matcher(result.stdout);
        int retries = 0;
        boolean found = false;
        while (!found && retries++ < GUESS_REVISION_RETRIES && matcher.find())
        {
            revision = Long.parseLong(matcher.group(1));
            if (isSyncNonTrivial(core, revision))
            {
                tried.add(revision);
            }
            else
            {
                found = true;
            }
        }

        if (!found)
        {
            throw new ScmException("Unable to guess have revision: tried " + tried + ": is your client at a single changelist?");
        }

        return revision;
    }

    private String getClientName(PerforceCore core) throws ScmException
    {
        // Small optimisation: if the client is explicitly set, don't ask for it.
        String client = core.getEnv().get(ENV_CLIENT);
        if (!StringUtils.stringSet(client))
        {
            PerforceCore.P4Result result = core.runP4(null, getP4Command(COMMAND_CLIENT), COMMAND_CLIENT, FLAG_OUTPUT);
            PerforceWorkspace workspace = PerforceWorkspace.parseSpecification(result.stdout.toString());
            client = workspace.getName();
        }
        return client;
    }

    private boolean isSyncNonTrivial(PerforceCore core, long revision) throws ScmException
    {
        PerforceCore.P4Result result = core.runP4(false, null, getP4Command(COMMAND_SYNC), COMMAND_SYNC, FLAG_PREVIEW, "@" + revision);
        return result.stdout.length() > 0;
    }

    public Revision update(WorkingCopyContext context, Revision revision) throws ScmException
    {
        PerforceCore core = createCore(context);
        if (revision == null)
        {
            revision = getLatestRemoteRevision(context);
        }

        UserInterface ui = context.getUI();
        PerforceSyncFeedbackHandler syncHandler = new PerforceSyncFeedbackHandler(ui);
        core.runP4WithHandler(syncHandler, null, getP4Command(COMMAND_SYNC), COMMAND_SYNC, "@" + revision.getRevisionString());

        if(syncHandler.isResolveRequired())
        {
            ConfigSupport configSupport = new ConfigSupport(context.getConfig());
            if(configSupport.getBooleanProperty(PROPERTY_CONFIRM_RESOLVE, true))
            {
                YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.auto.resolve"), true, false, YesNoResponse.YES);
                if(response.isPersistent())
                {
                    configSupport.setBooleanProperty(PROPERTY_CONFIRM_RESOLVE, !response.isAffirmative());
                }

                if(!response.isAffirmative())
                {
                    return revision;
                }
            }

            ui.status(I18N.format("status.resolving"));
            ui.enterContext();
            try
            {
                core.runP4WithHandler(new PerforceProgressPrintingFeedbackHandler(ui, false), null, getP4Command(COMMAND_RESOLVE), COMMAND_RESOLVE, FLAG_AUTO_MERGE);
            }
            finally
            {
                ui.exitContext();
            }
            ui.status(I18N.format("status.resolved"));
        }

        return revision;
    }

    public WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... spec) throws ScmException
    {
        PerforceCore core = createCore(context);
        WorkingCopyStatus status = new WorkingCopyStatus(core.getClientRoot());
        StatusBuildingFStatFeedbackHandler handler = new StatusBuildingFStatFeedbackHandler(context.getUI(), status);

        ConfigSupport configSupport = new ConfigSupport(context.getConfig());
        boolean pre2004_2 = configSupport.getBooleanProperty(PROPERTY_PRE_2004_2, false);

        // Spec can be either a changelist # or a list of files
        String changelist;
        if(spec.length == 1 && spec[0].startsWith(":"))
        {
            if (pre2004_2)
            {
                throw new ScmException("Unable to specify a changelist with configuration property '" + PROPERTY_PRE_2004_2 + "' set to true");
            }

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
            if (pre2004_2)
            {
                core.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, FLAG_FILES_OPENED, "//...");
            }
            else
            {
                core.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, FLAG_PATH_IN_DEPOT_FORMAT, FLAG_FILES_OPENED, FLAG_AFFECTED_CHANGELIST, "default", "//...");
            }
        }

        return status;
    }

    public boolean canDiff(WorkingCopyContext context, String path) throws ScmException
    {
        FileTypeFStatFeedbackHandler handler = new FileTypeFStatFeedbackHandler();
        PerforceCore core = createCore(context);
        File f = new File(core.getClientRoot(), path);
        core.runP4WithHandler(handler, null, getP4Command(COMMAND_FSTAT), COMMAND_FSTAT, f.getAbsolutePath());
        return handler.isText();
    }

    public void diff(WorkingCopyContext context, String path, OutputStream output) throws ScmException
    {
        PerforceCore core = createCore(context);
        File f = new File(core.getClientRoot(), path);
        final PrintWriter writer = new PrintWriter(output);

        // p4 outputs only hunks, no header, so we output a header ourselves
        writer.println(UnifiedPatch.HEADER_OLD_FILE + " " + path);
        writer.println(UnifiedPatch.HEADER_NEW_FILE + " " + path);
        
        core.setEnv(ENV_DIFF, "");
        
        core.runP4WithHandler(new PerforceErrorDetectingFeedbackHandler(true)
        {
            private boolean reachedFirstHunk = false;
            
            public void handleStdout(String line)
            {
                if (!reachedFirstHunk)
                {
                    if (line.startsWith("@@"))
                    {
                        // Now we have seen a hunk, there is no longer any
                        // chance of a header.
                        reachedFirstHunk = true;
                    }
                    else
                    {
                        if (line.startsWith("---") || line.startsWith("+++"))
                        {
                            // Some versions of perforce output headers, which
                            // we need to ignore.
                            return;
                        }
                    }
                }
                
                writer.println(line);
            }
        }, null, getP4Command(COMMAND_DIFF), COMMAND_DIFF, FLAG_UNIFIED_DIFF, f.getAbsolutePath());
        writer.flush();
    }

    private PerforceCore createCore(WorkingCopyContext context)
    {
        PerforceCore core = new PerforceCore();
        Config config = context.getConfig();
        transferPropertyIfSet(config, core, PROPERTY_CLIENT, ENV_CLIENT);
        transferPropertyIfSet(config, core, PROPERTY_PASSWORD, ENV_PASSWORD);
        transferPropertyIfSet(config, core, PROPERTY_PORT, ENV_PORT);
        transferPropertyIfSet(config, core, PROPERTY_USER, ENV_USER);
        return core;
    }

    private void transferPropertyIfSet(Config config, PerforceCore core, String property, String environmentVariable)
    {
        core.setEnv(environmentVariable, config.getProperty(property));
    }
}
