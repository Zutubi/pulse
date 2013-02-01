package com.zutubi.pulse.master.vfs.provider.pulse;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a node in a template hierarchy.
 *
 * @see TemplateScopesFileObject
 */
public class TemplateFileObject extends AbstractPulseFileObject implements ComparatorProvider
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
            node = hierarchy.getNodeByTemplatePath(UriParser.decode(scopeFile.getName().getRelativeName(getName())));
        }
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        return objectFactory.buildBean(TemplateFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class},
                new Object[]{fileName, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        if(node != null && node.getChildren().size() > 0)
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
    }

    protected String[] doListChildren() throws Exception
    {
        if(node != null)
        {
            Iterable<TemplateNode> visibleChildren = Iterables.filter(node.getChildren(), new Predicate<TemplateNode>()
            {
                public boolean apply(TemplateNode templateNode)
                {
                    return configurationSecurityManager.hasPermission(templateNode.getPath(), AccessManager.ACTION_VIEW);
                }
            });

            List<String> childNodes = CollectionUtils.map(visibleChildren, new Function<TemplateNode, String>()
            {
                public String apply(TemplateNode templateNode)
                {
                    return templateNode.getId();
                }
            });


            String[] children = new String[childNodes.size()];
            childNodes.toArray(children);
            Arrays.sort(children, new Sort.StringComparator());
            return UriParser.encode(children);
        }

        return NO_CHILDREN;
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

    public Comparator<FileObject> getComparator()
    {
        final Comparator<String> c = new Sort.StringComparator();
        return new Comparator<FileObject>()
        {
            public int compare(FileObject o1, FileObject o2)
            {
                TemplateFileObject tfo1 = (TemplateFileObject) o1;
                TemplateFileObject tfo2 = (TemplateFileObject) o2;
                return c.compare(tfo1.getDisplayName(), tfo2.getDisplayName());
            }
        };
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
