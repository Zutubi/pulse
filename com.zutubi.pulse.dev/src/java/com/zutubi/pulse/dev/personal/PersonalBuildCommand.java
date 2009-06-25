package com.zutubi.pulse.dev.personal;

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
        return "request a personal build";
    }

    public String getDetailedHelp()
    {
        return "Sends a personal build request to a pulse server.  This involves choosing\n" +
               "the revision to build against, optionally updating to this revision,\n" +
               "analysing any outstanding changes, forming a patch file and sending the patch\n" +
               "to the pulse server to execute a build.\n\n" +
               "Configuration is defined via properties files or command line arguments.  The\n" +
               "configuration specifies connection details for the pulse server, along with\n" +
               "information about the project you wish to execute.  The SCM configuration of\n" +
               "the project must match the working copy.";
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
        options.put("-r [--project] project", "set project to build");
        options.put("-s [--server] url", "set pulse server url");
        options.put("-u [--user] name", "set pulse user name");
        options.put("-p [--password] password", "set pulse password");
        options.put("-b [--base-dir] dir", "set base directory of working copy");
        options.put("-f [--file] filename", "set patch file name");
        options.put("-d [--define] name=value", "set named property to given value");
        options.put("-q [--quiet]", "suppress unnecessary output");
        options.put("-v [--verbose]", "show verbose output");
        options.put("-e [--revision] rev", "build against the specified revision");
        options.put("--no-send-request", "create patch but do not request build");
        options.put("--send-request", "request a personal build (the default)");
        options.put("--no-update", "do not update the working copy");
        options.put("--update", "update the working copy to the build revision");
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
