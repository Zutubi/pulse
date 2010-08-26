package com.zutubi.pulse.dev.sync;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.pulse.dev.client.AbstractClientFactory;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.client.UserAbortException;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command to synchronise plugins with the Pulse master.
 */
public class SynchronisePluginsCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(SynchronisePluginsCommand.class);
    
    private int execute(String[] argv) throws ParseException
    {
        DevBootstrapManager.startup("com/zutubi/pulse/dev/sync/bootstrap/context/applicationContext.xml");
        try
        {
            SynchronisePluginsClient client = new SynchronisePluginsClientFactory().newInstance(argv);
            client.setPluginManager(SpringComponentContext.<PluginManager>getBean("pluginManager"));
            client.setPluginSynchroniser(SpringComponentContext.<PluginSynchroniser>getBean("pluginSynchroniser"));
            return execute(client);
        }
        finally
        {
            DevBootstrapManager.shutdown();
        }
    }

    private int execute(SynchronisePluginsClient client)
    {
        try
        {
            client.syncPlugins();
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
        return Arrays.asList("");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("sy", "syn", "sync", "synch");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.putAll(AbstractClientFactory.getOptions());
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }
}
