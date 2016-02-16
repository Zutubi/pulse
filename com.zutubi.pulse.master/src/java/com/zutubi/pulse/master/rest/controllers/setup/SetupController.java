package com.zutubi.pulse.master.rest.controllers.setup;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.bootstrap.SetupState;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.rest.Utils;
import com.zutubi.pulse.master.rest.errors.ValidationException;
import com.zutubi.pulse.master.rest.model.setup.SetupModel;
import com.zutubi.pulse.master.rest.model.setup.TaskModel;
import com.zutubi.pulse.master.rest.model.setup.VersionModel;
import com.zutubi.pulse.master.restore.Archive;
import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.master.security.Principle;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.setup.*;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.pulse.master.upgrade.UpgradeTask;
import com.zutubi.pulse.master.upgrade.UpgradeTaskGroup;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.SimpleInstantiator;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.ui.ConfigModelBuilder;
import com.zutubi.tove.ui.ToveUiUtils;
import com.zutubi.tove.ui.model.*;
import com.zutubi.util.StringUtils;
import org.apache.xmlrpc.XmlRpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

/**
 * RESTish API controller for server setup.
 */
@RestController
@RequestMapping("/setup")
public class SetupController
{
    private static final int MAX_ERRORS = 5;

    @Autowired
    private SetupManager setupManager;
    @Autowired
    private ConfigModelBuilder configModelBuilder;
    @Autowired
    private TypeRegistry typeRegistry;
    @Autowired
    private ConfigurationTemplateManager configurationTemplateManager;
    @Autowired
    private MasterConfigurationRegistry configurationRegistry;
    @Autowired
    private MasterConfigurationManager configurationManager;
    @Autowired
    private ServerMessagesHandler serverMessagesHandler;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<SetupModel[]> get() throws Exception
    {
        SetupState state = setupManager.getCurrentState();
        SetupModel model = new SetupModel(state.toString().toLowerCase(), setupManager.getStatusMessage());

        switch (state)
        {
            case WAITING:
                addErrorMessages(model);
                break;
            case DATA:
                fillDataModel(model);
                break;
            case DATABASE:
                model.setInput(configModelBuilder.buildTransientModel(SetupDatabaseTypeConfiguration.class));
                break;
            case LICENSE:
                model.setInput(configModelBuilder.buildTransientModel(SetupLicenseConfiguration.class));
                break;
            case MIGRATE:
                fillMigrateModel(model);
                break;
            case RESTORE:
                fillRestoreModel(model);
                break;
            case ADMIN:
                model.setInput(configModelBuilder.buildTransientModel(AdminUserConfiguration.class));
                break;
            case SETTINGS:
            {
                TransientModel input = configModelBuilder.buildTransientModel(ServerSettingsConfiguration.class);
                input.getType().setSimplePropertyDefaults(Utils.getSimplePropertyValues(typeRegistry.getType(ServerSettingsConfiguration.class), setupManager.getDefaultServerSettings()));
                model.setInput(input);
                break;
            }
            case UPGRADE:
                fillUpgradeModel(model);
                break;
            case STARTING:
                addErrorMessages(model);
                break;
        }

        return new ResponseEntity<>(new SetupModel[]{model}, HttpStatus.OK);
    }

    private void fillDataModel(SetupModel model) throws Exception
    {
        TransientModel input = configModelBuilder.buildTransientModel(SetupDataConfiguration.class);
        input.getType().setSimplePropertyDefaults(Utils.getSimplePropertyValues(typeRegistry.getType(SetupDataConfiguration.class), setupManager.getDefaultData()));
        model.setInput(input);

        File pulseConfig = getPulseConfig();
        model.addProperty("configPath", pulseConfig.getAbsolutePath());
        model.addProperty("configExists", pulseConfig.isFile());
    }

    private void addErrorMessages(SetupModel model)
    {
        List<CustomLogRecord> errorRecords = Lists.newLinkedList(Iterables.filter(serverMessagesHandler.takeSnapshot(), new Predicate<CustomLogRecord>()
        {
            public boolean apply(CustomLogRecord record)
            {
                return record.getLevel().intValue() == Level.SEVERE.intValue();
            }
        }));

        Collections.reverse(errorRecords);
        if (errorRecords.size() > MAX_ERRORS)
        {
            errorRecords = errorRecords.subList(0, MAX_ERRORS);
        }

        for (CustomLogRecord record: errorRecords)
        {
            model.addErrorMessage(record.getMessage());
        }
    }

    private File getPulseConfig()
    {
        EnvConfig envConfig = configurationManager.getEnvConfig();
        if (envConfig.hasPulseConfig())
        {
            return new File(envConfig.getPulseConfig());
        }
        else
        {
            return new File(envConfig.getDefaultPulseConfig(MasterConfigurationManager.CONFIG_DIR));
        }
    }

    private void fillMigrateModel(SetupModel model) throws IOException
    {
        MigrationManager migrationManager = SpringComponentContext.getBean("migrationManager");
        Monitor monitor = migrationManager.getMonitor();
        model.setProgressMonitor(monitor);
        if (monitor == null || !monitor.isStarted())
        {
            model.setInput(configModelBuilder.buildTransientModel(MigrateDatabaseTypeConfiguration.class));
        }

        DatabaseConfig databaseConfig = configurationManager.getDatabaseConfig();
        String url = databaseConfig.getUrl();
        MigrateDatabaseTypeConfiguration configuration;
        if (url.startsWith("jdbc:mysql"))
        {
            configuration = DatabaseType.MYSQL.getDatabaseConfiguration(databaseConfig.getProperties());
        }
        else if (url.startsWith("jdbc:postgresql"))
        {
            configuration = DatabaseType.POSTGRESQL.getDatabaseConfiguration(databaseConfig.getProperties());
        }
        else
        {
            configuration = DatabaseType.EMBEDDED.getDatabaseConfiguration(databaseConfig.getProperties());
        }

        model.addProperty("databaseType", configuration.getType().getPrettyName());
        model.addProperty("host", configuration.getHost());
        model.addProperty("port", configuration.getPort());
        model.addProperty("database", configuration.getDatabase());
        model.addProperty("user", configuration.getUser());
    }

    private void fillRestoreModel(SetupModel model)
    {
        RestoreManager restoreManager = SpringComponentContext.getBean("restoreManager");
        model.setProgressMonitor(restoreManager.getMonitor());

        Archive archive = restoreManager.getArchive();
        model.addProperty("archiveCreated", archive.getCreated());
        model.addProperty("archiveName", getArchiveName(archive));
        model.addProperty("archiveLocation", getArchiveLocation(archive));
    }

    public String getArchiveName(Archive archive)
    {
        if (archive.getOriginal() != null)
        {
            return archive.getOriginal().getName();
        }
        return "n/a";
    }

    public String getArchiveLocation(Archive archive)
    {
        File original = archive.getOriginal();
        if (original != null)
        {
            try
            {
                File resolved = original.getCanonicalFile();
                return resolved.getParentFile().getAbsolutePath();
            }
            catch (IOException e)
            {
                if (original.getParentFile() != null)
                {
                    return original.getParentFile().getAbsolutePath();
                }
                return "unknown";
            }
        }

        return "n/a";
    }

    private void fillUpgradeModel(SetupModel model)
    {
        UpgradeManager upgradeManager = SpringComponentContext.getBean("upgradeManager");
        model.setProgressMonitor(upgradeManager.getMonitor());
        List<UpgradeTaskGroup> taskGroups = upgradeManager.previewUpgrade();
        List<TaskModel> taskModels = new ArrayList<>();
        for (UpgradeTaskGroup group: taskGroups)
        {
            for (UpgradeTask task: group.getTasks())
            {
                taskModels.add(new TaskModel(task, null));
            }
        }

        model.addProperty("tasks", taskModels);

        Data existingData = configurationManager.getData();
        model.addProperty("existingVersion", new VersionModel(existingData.getVersion()));
        model.addProperty("newVersion", new VersionModel(Version.getVersion()));
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postData(@RequestBody CompositeModel model) throws Exception
    {
        SetupDataConfiguration instance = convertAndValidate(SetupDataConfiguration.class, model);
        setupManager.setData(instance);
        return get();
    }

    @RequestMapping(value = "/database", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postDatabase(@RequestBody CompositeModel model) throws Exception
    {
        SetupDatabaseTypeConfiguration instance = convertAndValidate(SetupDatabaseTypeConfiguration.class, model);
        setupManager.setDatabaseType(instance);
        return get();
    }

    @RequestMapping(value = "/license", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postLicense(@RequestBody CompositeModel model) throws Exception
    {
        SetupLicenseConfiguration instance = convertAndValidate(SetupLicenseConfiguration.class, model);
        setupManager.setLicense(instance);
        return get();
    }

    @RequestMapping(value = "/requestLicense", method = RequestMethod.POST)
    public ResponseEntity<ActionResultModel> postRequestLicense(@RequestBody CompositeModel model) throws Exception
    {
        RequestLicenseConfiguration instance = convertAndValidate(RequestLicenseConfiguration.class, model);
        try
        {
            XmlRpcClient client = new XmlRpcClient("http://zutubi.com/xmlrpc/");
            Vector<String> args = new Vector<>(2);
            args.add(instance.getFullName());
            args.add(instance.getEmail());

            String license = (String) client.execute("evaluation", args);
            license = StringUtils.wrapString(license.replaceAll("\\s", ""), 42, null);
            return new ResponseEntity<>(new ActionResultModel(true, license, null), HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(new ActionResultModel(false, e.getMessage(), null), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postAdmin(HttpServletRequest request, @RequestBody CompositeModel model) throws Exception
    {
        AdminUserConfiguration instance = convertAndValidate(AdminUserConfiguration.class, model);
        UserConfiguration adminUser = setupManager.setAdminUser(instance);
        // login as the admin user.  safe to directly create Principle as
        // we know the user has no external authorities
        User user = new User();
        user.setConfig(adminUser);
        SecurityUtils.loginAs(new Principle(user, Collections.<UserGroupConfiguration>emptyList()));
        SecurityUtils.saveAuthenticationInSession(request.getSession());
        return get();
    }

    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postSettings(@RequestBody CompositeModel model) throws Exception
    {
        ServerSettingsConfiguration instance = convertAndValidate(ServerSettingsConfiguration.class, model);
        setupManager.setServerSettings(instance);
        return get();
    }

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postRestore() throws Exception
    {
        setupManager.executeRestore();
        return get();
    }

    @RequestMapping(value = "/restoreAbort", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postRestoreAbort() throws Exception
    {
        setupManager.abortRestore();
        return get();
    }

    @RequestMapping(value = "/restoreContinue", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postRestoreContinue() throws Exception
    {
        setupManager.postRestore();
        return get();
    }

    @RequestMapping(value = "/migrate", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postMigrate(@RequestBody CompositeModel body) throws Exception
    {
        MigrateDatabaseTypeConfiguration config = convertAndValidate(MigrateDatabaseTypeConfiguration.class, body);
        setupManager.executeMigrate(config);
        return get();
    }

    @RequestMapping(value = "/migrateAbort", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postMigrateAbort() throws Exception
    {
        setupManager.abortMigrate();
        return get();
    }

    @RequestMapping(value = "/migrateContinue", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postMigrateContinue() throws Exception
    {
        setupManager.postMigrate();
        return get();
    }
    @RequestMapping(value = "/upgrade", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postUpgrade() throws Exception
    {
        setupManager.executeUpgrade();
        return get();
    }

    @RequestMapping(value = "/upgradeContinue", method = RequestMethod.POST)
    public ResponseEntity<SetupModel[]> postUpgradeContinue() throws Exception
    {
        setupManager.postUpgrade();
        return get();
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ResponseEntity<CheckResultModel> check(@RequestBody CheckModel check) throws TypeException
    {
        String symbolicName = check.getMain().getType().getSymbolicName();
        CompositeType compositeType = typeRegistry.getType(symbolicName);
        if (compositeType == null)
        {
            throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
        }

        CompositeType checkType = configurationRegistry.getConfigurationCheckType(compositeType);
        if (checkType == null)
        {
            throw new IllegalArgumentException("Type '" + compositeType + "' does not support configuration checking");
        }

        MutableRecord record = ToveUiUtils.convertProperties(compositeType, null, check.getMain().getProperties());
        MutableRecord checkRecord = ToveUiUtils.convertProperties(checkType, null, check.getCheck().getProperties());
        Configuration checkInstance = configurationTemplateManager.validate(null, null, checkRecord, true, false);
        Configuration mainInstance = configurationTemplateManager.validate(null, null, record, true, false);
        if (!checkInstance.isValid())
        {
            throw new ValidationException(checkInstance, "check");
        }

        if (!mainInstance.isValid())
        {
            throw new ValidationException(mainInstance, "main");
        }

        SimpleInstantiator instantiator = new SimpleInstantiator(null, null, configurationTemplateManager);
        Configuration instance = (Configuration) instantiator.instantiate(compositeType, record);
        @SuppressWarnings("unchecked")
        ConfigurationCheckHandler<Configuration> handler = (ConfigurationCheckHandler<Configuration>) instantiator.instantiate(checkType, checkRecord);
        CheckResultModel result = Utils.runCheck(handler, instance);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public <T extends Configuration> T convertAndValidate(Class<T> clazz, CompositeModel model) throws TypeException
    {
        CompositeType type = typeRegistry.getType(clazz);
        Record record = ToveUiUtils.convertProperties(type, null, model.getProperties());

        T instance = configurationTemplateManager.validate(null, null, record, true, true);
        if (!instance.isValid())
        {
            throw new ValidationException(instance);
        }

        return instance;
    }
}