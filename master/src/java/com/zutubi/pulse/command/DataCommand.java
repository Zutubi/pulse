package com.zutubi.pulse.command;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.util.TextUtils;
import org.apache.commons.cli.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
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
        parse(argv);

        // update the system properties
        if (TextUtils.stringSet(pulseData))
        {
            System.setProperty(SystemConfiguration.PULSE_DATA, pulseData);
        }

        if (TextUtils.stringSet(pulseConfig))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, pulseConfig);
        }
        else if (TextUtils.stringSet(System.getenv(ENV_PULSE_CONFIG)))
        {
            System.setProperty(EnvConfig.PULSE_CONFIG, System.getenv(ENV_PULSE_CONFIG));
        }

        SystemBootstrapManager sbm = new SystemBootstrapManager();
        sbm.loadBootstrapContext();
        ComponentContext.addClassPathContextDefinitions("classpath:/com/zutubi/pulse/bootstrap/context/databaseContext.xml");
        ComponentContext.addClassPathContextDefinitions("classpath:/com/zutubi/pulse/bootstrap/context/hibernateMappingsContext.xml");

        dataSource = (DataSource) ComponentContext.getBean("dataSource");

        configuration = new MutableConfiguration();

        List<String> mappings = (List<String>) ComponentContext.getBean("hibernateMappings");
        for (String mapping : mappings)
        {
            Resource resource = new ClassPathResource(mapping);
            configuration.addInputStream(resource.getInputStream());
        }

        databaseConfig = (DatabaseConfig) ComponentContext.getBean("databaseConfig");
        configuration.setProperties(databaseConfig.getHibernateProperties());

        return doExecute(argv);
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
    public abstract int doExecute(String... argv) throws IOException, ParseException;

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

    public void parse(String... argv) throws ParseException
    {
        Options options = getSharedOptions();
        
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(options, argv, true);

        processSharedOptions(commandLine);
    }
}
