package com.zutubi.pulse.vfs.pulse;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.TemplateHierarchy;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a template hierarchy.  Nested directly underneath is the file
 * for the root template.
 *
 * @see com.zutubi.pulse.vfs.pulse.TemplateScopesFileObject
 */
public class TemplateScopeFileObject extends AbstractPulseFileObject
{
    private ConfigurationTemplateManager configurationTemplateManager;

    public TemplateScopeFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        return objectFactory.buildBean(TemplateFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class},
                new Object[]{fileName, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        TemplateHierarchy hierarchy = getTemplateHierarchy();
        if(hierarchy != null && hierarchy.getRoot() != null)
        {
            return new String[] { hierarchy.getRoot().getId() };
        }

        return new String[0];
    }

    public TemplateHierarchy getTemplateHierarchy()
    {
        return configurationTemplateManager.getTemplateHierarchy(getName().getBaseName());
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
