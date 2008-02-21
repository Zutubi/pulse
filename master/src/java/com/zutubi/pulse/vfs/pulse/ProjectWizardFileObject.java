package com.zutubi.pulse.vfs.pulse;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.SimpleInstantiator;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.wizard.TypeWizardState;
import com.zutubi.prototype.wizard.webwork.AbstractTypeWizard;
import com.zutubi.prototype.wizard.webwork.ConfigurationWizardAction;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class ProjectWizardFileObject extends AbstractPulseFileObject implements ProjectProvider
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

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
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

        SimpleInstantiator instantiator = new SimpleInstantiator(configurationTemplateManager, configurationReferenceManager);
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

    public Project getProject() throws FileSystemException
    {
        Project project = new Project();
        project.setConfig(getProjectConfig());
        return project;
    }

    public long getProjectId() throws FileSystemException
    {
        return -1;
    }

    private AbstractTypeWizard getWizardInstance()
    {
        String path = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, getName().getBaseName());
        Map session = ActionContext.getContext().getSession();
        return (AbstractTypeWizard) session.get(ConfigurationWizardAction.getSessionKey(path));
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
