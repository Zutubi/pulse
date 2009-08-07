package com.zutubi.pulse.master.cli;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.servercore.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.StringUtils;
import org.apache.commons.cli.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The abstract base command for commands used to import/export data.
 */
public abstract class DataCommand implements Command
{
    private static final String ENV_PULSE_CONFIG = "PULSE_CONFIG";

    /**
     * The pulse data directory
     */
    private String pulseData = null;

    /**
     * The pulse configuration file.
     */
    private String pulseConfig;

    protected DataSource dataSource;
    protected MasterConfigurationManager configurationManager;
    protected DatabaseConfig databaseConfig;
    protected MutableConfiguration configuration;

    /**
     * The path to the configuration file that is used to configure the pulse configuration details.
     *
     * @param path
     */
    public void setConfig(String path)
    {
        this.pulseConfig = path;
    }

    /**
     * The path to the pulse data directory
     *
     * @param data directory
     */
    public void setData(String data)
    {
        this.pulseData = data;
    }

    public int execute(BootContext bootContext) throws Exception
    {
        return execute(bootContext.getCommandArgv());
    }

    public int execute(String... argv) throws ParseException, IOException
    {
        CommandLine commandLine = parse(argv);
        updateSystemProperties();

        SystemBootstrapManager sbm = new SystemBootstrapManager();
        sbm.loadBootstrapContext();

        configurationManager = (MasterConfigurationManager) SpringComponentContext.getBean("configurationManager");
        if (!checkConfigFile())
        {
            return 2;
        }

        SpringComponentContext.addClassPathContextDefinitions("classpath:/com/zutubi/pulse/master/bootstrap/context/databaseContext.xml");
        SpringComponentContext.addClassPathContextDefinitions("classpath:/com/zutubi/pulse/master/bootstrap/context/hibernateMappingsContext.xml");

        dataSource = (DataSource) SpringComponentContext.getBean("dataSource");

        List<String> mappings = (List<String>) SpringComponentContext.getBean("hibernateMappings");

        configuration = new MutableConfiguration();
        configuration.addClassPathMappings(mappings);

        databaseConfig = configurationManager.getDatabaseConfig();
        configuration.setProperties(databaseConfig.getHibernateProperties());

        DefaultSetupManager.printConsoleMessage("Using database configuration '%s'", configurationManager.getDatabaseConfigFile());
        DefaultSetupManager.printConsoleMessage("Using database '%s'", databaseConfig.getUrl());

        return doExecute(commandLine);
   }

    private void updateSystemProperties()
    {
        if (StringUtils.stringSet(pulseData))
        {
            System.setProperty(SystemConfiguration.PULSE_DATA, pulseData);
        }

        if (StringUtils.stringSet(pulseConfig))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, pulseConfig);
        }
        else if (StringUtils.stringSet(System.getenv(ENV_PULSE_CONFIG)))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, System.getenv(ENV_PULSE_CONFIG));
        }
    }

    private boolean checkConfigFile()
    {
        EnvConfig envConfig = configurationManager.getEnvConfig();
        String configFileName = envConfig.getPulseConfig();
        if(StringUtils.stringSet(configFileName))
        {
            File configFile = new File(configFileName);
            DefaultSetupManager.printConsoleMessage("Using config file '%s'", configFile.getAbsolutePath());
            if(!configFile.exists())
            {
                System.err.println("Specified config file '" + configFileName + "' does not exist");
                return false;
            }
        }
        else
        {
            configFileName = envConfig.getDefaultPulseConfig(MasterConfigurationManager.CONFIG_DIR);
            File configFile = new File(configFileName);
            DefaultSetupManager.printConsoleMessage("No config file specified, using default '%s'", configFile.getAbsolutePath());
            if(!configFile.exists())
            {
                System.err.println("Default config file '" + configFileName + "' does not exist");
                return false;
            }
        }

        return true;
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-f [--config] file", "specify an alternate config file");
        options.put("-d [--data] dir", "use the specified directory for all pulse data");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    /**
     * Data command implementations should implement their custom
     * functionality in this method. When this method is invoked, the
     * dataSource will be available.
     */
    public abstract int doExecute(CommandLine commandLine) throws IOException, ParseException;

    @SuppressWarnings({ "AccessStaticViaInstance" })
    protected Options getSharedOptions()
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("config")
                .hasArg()
                .create('f'));
        options.addOption(OptionBuilder.withLongOpt("data")
                .hasArg()
                .create('d'));
        return options;
    }

    protected void processSharedOptions(CommandLine commandLine)
    {
        if (commandLine.hasOption('f'))
        {
            setConfig(commandLine.getOptionValue('f'));
        }
        if (commandLine.hasOption('d'))
        {
            setData(commandLine.getOptionValue('d'));
        }
    }

    public CommandLine parse(String... argv) throws ParseException
    {
        Options options = getSharedOptions();
        
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(options, argv, false);

        processSharedOptions(commandLine);
        return commandLine;
    }
}
