package com.zutubi.pulse.command;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.bootstrap.DatabaseConfig;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * The abstract base command for commands used to import/export data.
 */
public abstract class DataCommand implements Command
{
    private static final String ENV_PULSE_CONFIG = "PULSE_CONFIG";
    private String pulseConfig;
    protected DataSource dataSource;
    protected MutableConfiguration configuration;

    public void setConfig(String path)
    {
        this.pulseConfig = path;
    }

    public int execute(BootContext context) throws ParseException, IOException
    {
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

        DatabaseConfig databaseConfig = (DatabaseConfig) ComponentContext.getBean("databaseConfig");
        configuration.setProperties(databaseConfig.getHibernateProperties());

        return doExecute(context);
   }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-f [--config] file", "specify an alternate config file");
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
    public abstract int doExecute(BootContext context) throws IOException, ParseException;

    @SuppressWarnings({ "AccessStaticViaInstance" })
    protected Options getSharedOptions()
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("config")
                .hasArg()
                .create('f'));
        return options;
    }

    protected void processSharedOptions(CommandLine commandLine)
    {
        if (commandLine.hasOption('f'))
        {
            setConfig(commandLine.getOptionValue('f'));
        }
    }
}
