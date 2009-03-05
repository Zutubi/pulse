package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.pulse.master.tove.wizard.TypeWizardState;
import com.zutubi.pulse.master.tove.wizard.webwork.ConfigurationWizardAction;
import com.zutubi.pulse.master.vfs.provider.pulse.scm.ScmRootFileObject;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.SimpleInstantiator;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class ProjectWizardFileObject extends AbstractPulseFileObject implements ProjectConfigProvider
{
    private static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();
    {
        nodesDefinitions.put("scm", ScmRootFileObject.class);
    }

    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;

    public ProjectWizardFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        if (nodesDefinitions.containsKey(name))
        {
            Class<? extends AbstractPulseFileObject> clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> children = nodesDefinitions.keySet();
        return children.toArray(new String[children.size()]);
    }

    public ProjectConfiguration getProjectConfig() throws FileSystemException
    {
        AbstractTypeWizard wizard = getWizardInstance();
        if(wizard == null)
        {
            throw new FileSystemException("Wizard not found for parent project '" + getName().getBaseName() + "'");
        }

        TypeWizardState state = wizard.getCompletedStateForType(typeRegistry.getType(ScmConfiguration.class));
        if(state == null)
        {
            throw new FileSystemException("Scm details not configured");
        }

        SimpleInstantiator instantiator = new SimpleInstantiator(null, configurationReferenceManager, configurationTemplateManager);
        Record record = state.getRenderRecord();
        try
        {
            ScmConfiguration config = (ScmConfiguration) instantiator.instantiate(typeRegistry.getType(record.getSymbolicName()), record);
            ProjectConfiguration projectConfig = new ProjectConfiguration();
            projectConfig.setScm(config);
            return projectConfig;
        }
        catch (TypeException e)
        {
            throw new FileSystemException(e);
        }
    }

    private AbstractTypeWizard getWizardInstance()
    {
        String path = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, getName().getBaseName());
        return (AbstractTypeWizard) ConfigurationWizardAction.getWizardInstance(path);
    }

    public boolean isLocal()
    {
        return true;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }
}
