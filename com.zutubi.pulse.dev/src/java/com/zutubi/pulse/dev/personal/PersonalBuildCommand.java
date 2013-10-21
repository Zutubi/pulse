package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.scm.patch.PatchFormatFactory;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.pulse.dev.client.AbstractClientFactory;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.client.UserAbortException;
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
public class PersonalBuildCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(PersonalBuildCommand.class);

    public int execute(PersonalBuildClient client)
    {
        try
        {
            PersonalBuildContext context = client.checkConfiguration();
            PersonalBuildRevision revision = client.chooseRevision(context);

            File patchFile;
            PersonalBuildConfig config = client.getConfig();
            String patchFilename = config.getPatchFile();
            if (patchFilename == null)
            {
                if (revision.isUpdateSupported())
                {
                    client.updateIfDesired(context, revision.getRevision());
                }

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

                if (!client.preparePatch(context, patchFile, config.getFiles()))
                {
                    client.getUI().error("Aborting personal build as there is no patch to apply.");
                    return 1;
                }
            }
            else
            {
                patchFile = new File(patchFilename);
            }

            if (config.getSendRequest())
            {
                client.sendRequest(context, revision.getRevision(), patchFile);
            }
        }
        catch (UserAbortException e)
        {
            return 2;
        }
        catch (ClientException e)
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
            PersonalBuildClient client = new PersonalBuildClientFactory().newInstance(argv);
            client.setPatchFormatFactory((PatchFormatFactory) SpringComponentContext.getBean("patchFormatFactory"));
            client.setPluginManager((PluginManager) SpringComponentContext.getBean("pluginManager"));
            client.setPluginSynchroniser((PluginSynchroniser) SpringComponentContext.getBean("pluginSynchroniser"));
            return execute(client);
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
        options.putAll(AbstractClientFactory.getOptions());
        options.put("-r [--project] project", I18N.format("flag.project"));
        options.put("-f [--file] filename", I18N.format("flag.file"));
        options.put("-t [--patch-type] type", I18N.format("flag.patch.type"));
        options.put("-e [--revision] rev", I18N.format("flag.revision"));
        options.put("-a [--reason] reason", I18N.format("flag.reason"));
        options.put("-o [--override] name=value", I18N.format("flag.override"));
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
