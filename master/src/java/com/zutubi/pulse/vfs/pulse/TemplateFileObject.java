package com.zutubi.pulse.vfs.pulse;

import com.zutubi.prototype.config.TemplateHierarchy;
import com.zutubi.prototype.config.TemplateNode;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Sort;
import org.apache.commons.vfs.FileName;
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
    public TemplateFileObject(final FileName name, final AbstractFileSystem fs)
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
        TemplateScopeFileObject scopeFile = getAncestor(TemplateScopeFileObject.class);
        TemplateHierarchy hierarchy = scopeFile.getTemplateHierarchy();
        if(hierarchy != null)
        {
            TemplateNode node = hierarchy.getNodeByTemplatePath(scopeFile.getName().getRelativeName(getName()));
            if(node != null)
            {
                List<String> childNodes = CollectionUtils.map(node.getChildren(), new Mapping<TemplateNode, String>()
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
        }

        return new String[0];
    }
}
