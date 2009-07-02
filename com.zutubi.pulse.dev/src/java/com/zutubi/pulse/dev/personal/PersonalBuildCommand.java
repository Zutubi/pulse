package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.util.Pair;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command-line interface for requesting personal builds.
 */
@SuppressWarnings({ "AccessStaticViaInstance" })
public class PersonalBuildCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(PersonalBuildCommand.class);

    public int execute(PersonalBuildClient client)
    {
        try
        {
            Pair<WorkingCopy, WorkingCopyContext> pair = client.checkConfiguration();
            WorkingCopy wc = pair.first;
            WorkingCopyContext context = pair.second;

            PersonalBuildRevision revision = client.chooseRevision(wc, context);
            if (revision.isUpdateSupported())
            {
                client.updateIfDesired(wc, context, revision.getRevision());
            }

            File patchFile;
            PersonalBuildConfig config = client.getConfig();
            String patchFilename = config.getPatchFile();
            if(patchFilename == null)
            {
                try
                {
                    patchFile = File.createTempFile("pulse.patch.", ".zip");
                    patchFile.deleteOnExit();
                }
                catch (IOException e)
                {
                    client.getUI().error("Unable to create temporary patch file: " + e.getMessage(), e);
                    return 1;
                }
            }
            else
            {
                patchFile = new File(patchFilename);
            }

            if (client.preparePatch(wc, context, patchFile, config.getFiles()) && config.getSendRequest())
            {
                client.sendRequest(revision.getRevision(), patchFile);
            }
        }
        catch (UserAbortException e)
        {
            return 2;
        }
        catch (PersonalBuildException e)
        {
            client.getUI().error(e.getMessage(), e);
            return 1;
        }

        return 0;
    }

    private int execute(String[] argv) throws ParseException
    {
        DevBootstrapManager.startup("com/zutubi/pulse/dev/personal/bootstrap/context/applicationContext.xml");
        try
        {
            return execute(PersonalBuildClientFactory.newInstance(argv));
        }
        finally
        {
            DevBootstrapManager.shutdown();
        }
    }

    public int execute(BootContext context) throws ParseException
    {
        return execute(context.getCommandArgv());
    }

    public String getHelp()
    {
        return I18N.format("command.help");
    }

    public String getDetailedHelp()
    {
        return I18N.format("command.detailed.help");
    }

    public List<String> getUsages()
    {
        return Arrays.asList("", "<file> ...", ":<changelist>");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("pe", "per", "pers");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-r [--project] project", I18N.format("flag.project"));
        options.put("-s [--server] url", I18N.format("flag.server"));
        options.put("-u [--user] name", I18N.format("flag.user"));
        options.put("-p [--password] password", I18N.format("flag.password"));
        options.put("-b [--base-dir] dir", I18N.format("flag.base.dir"));
        options.put("-f [--file] filename", I18N.format("flag.file"));
        options.put("-d [--define] name=value", I18N.format("flag.define"));
        options.put("-q [--quiet]", I18N.format("flag.quiet"));
        options.put("-v [--verbose]", I18N.format("flag.verbose"));
        options.put("-e [--revision] rev", I18N.format("flag.revision"));
        options.put("--no-send-request", I18N.format("flag.no.request"));
        options.put("--send-request", I18N.format("flag.request"));
        options.put("--no-update", I18N.format("flag.no.update"));
        options.put("--update", I18N.format("flag.update"));
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    public static void main(String[] argv)
    {
        PersonalBuildCommand command = new PersonalBuildCommand();
        try
        {
            System.exit(command.execute(argv));
        }
        catch (ParseException e)
        {
            System.err.println(e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        System.exit(1);
    }
}
