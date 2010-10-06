package com.zutubi.pulse.dev.expand;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command line entry point for a debugging command that expands a Pulse file.
 * That is, it loads the file, processing all imports and macros, then dumps it
 * out again as expanded XML.
 */
public class ExpandCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(ExpandCommand.class);
    
    public int execute(BootContext context) throws ParseException
    {
        return execute(context.getCommandArgv());
    }

    private int execute(String[] argv) throws ParseException
    {
        DevBootstrapManager.startup("com/zutubi/pulse/dev/expand/bootstrap/context/applicationContext.xml");
        try
        {
            ObjectFactory objectFactory = SpringComponentContext.getBean("objectFactory");
            PulseFileExpander expander = objectFactory.buildBean(PulseFileExpander.class);
            expander.expand(new PulseFileExpanderOptions(argv));
            return 0;
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (StringUtils.stringSet(message))
            {
                System.err.println(message);
            }
            else
            {
                e.printStackTrace(System.err);
            }
            return 1;
        }
        finally
        {
            DevBootstrapManager.shutdown();
        }
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
        return Arrays.asList("<pulse file>");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("exp");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-r [--recipe] recipe", I18N.format("flag.recipe"));
        options.put("-d [--define] name=value", I18N.format("flag.define"));
        options.put("-q [--require] name[/ver]", I18N.format("flag.require"));
        options.put("-e [--resources-file] file", I18N.format("flag.resources"));
        options.put("-b [--base-dir] dir", I18N.format("flag.base.dir"));
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    public static void main(String[] argv)
    {
        ExpandCommand command = new ExpandCommand();
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