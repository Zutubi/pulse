package com.zutubi.pulse.vfs.pulse;

import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.TemplateHierarchy;
import com.zutubi.prototype.config.TemplateNode;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a node in a template hierarchy.
 *
 * @see TemplateScopesFileObject
 */
public class TemplateFileObject extends AbstractPulseFileObject
{
    private TemplateNode node;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationSecurityManager configurationSecurityManager;

    public TemplateFileObject(final FileName name, final AbstractFileSystem fs) throws FileSystemException
    {
        super(name, fs);

        TemplateScopeFileObject scopeFile = getAncestor(TemplateScopeFileObject.class);
        TemplateHierarchy hierarchy = scopeFile.getTemplateHierarchy();
        if(hierarchy != null)
        {
            node = hierarchy.getNodeByTemplatePath(scopeFile.getName().getRelativeName(getName()));
        }
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
        if(node != null)
        {
            List<TemplateNode> visibleChildren = CollectionUtils.filter(node.getChildren(), new Predicate<TemplateNode>()
            {
                public boolean satisfied(TemplateNode templateNode)
                {
                    return configurationSecurityManager.hasPermission(templateNode.getPath(), AccessManager.ACTION_VIEW);
                }
            });

            List<String> childNodes = CollectionUtils.map(visibleChildren, new Mapping<TemplateNode, String>()
            {
                public String map(TemplateNode templateNode)
                {
                    return templateNode.getId();
                }
            });


            String[] children = new String[childNodes.size()];
            childNodes.toArray(children);
            Arrays.sort(children, new Sort.StringComparator());
            return children;
        }

        return new String[0];
    }

    public String getCls()
    {
        if(node == null)
        {
            return null;
        }

        return configurationTemplateManager.isDeeplyValid(node.getPath()) ? null : "config-invalid";
    }

    public String getIconCls()
    {
        if(node != null && node.isConcrete())
        {
            return "config-concrete-icon";
        }
        else
        {
            return "config-template-icon";
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }
}
