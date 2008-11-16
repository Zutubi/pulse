package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.vfs.provider.pulse.scm.ScmRootFileObject;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class ProjectConfigFileObject extends AbstractPulseFileObject implements ProjectConfigProvider, AddressableFileObject
{
    private static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();
    {
        nodesDefinitions.put("scm", ScmRootFileObject.class);
    }

    private ConfigurationProvider configurationProvider;

    public ProjectConfigFileObject(final FileName name, final AbstractFileSystem fs)
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

    public ProjectConfiguration getProjectConfig()
    {
        return configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, getName().getBaseName()), ProjectConfiguration.class);
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        return new Urls("").project(StringUtils.uriComponentEncode(getProjectConfig().getName()));
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
