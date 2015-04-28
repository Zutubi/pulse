package com.zutubi.pulse.master.tove.nimda;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ComparatorProvider;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.vfs.DirectoryComparator;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import com.zutubi.tove.config.TemplateNode;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.util.*;

import static com.google.common.collect.Collections2.transform;

/**
 * An action to list the config children of a node in either the template or config tree.
 */
public class ConfigLsAction extends ActionSupport
{
    /**
     * If true list the template children of the item, otherwise list the config children.
     */
    private boolean template;

    /**
     * Path of the item to list.
     */
    private String path;

    /**
     * The results of the ls action.
     */
    private FileModel[] listing;

    /**
     * Number of levels, under the one being listed, to also load.  The default
     * of zero means just list this path's direct children.
     */
    private int depth = 0;

    private ConfigurationTemplateManager configurationTemplateManager;
    private ClassificationManager classificationManager;

    public void setTemplate(boolean template)
    {
        this.template = template;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public FileModel[] getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        try
        {
            return doListing();
        }
        catch (FileSystemException e)
        {
            String className = e.getStackTrace()[0].getClassName();
            Class cls = ClassLoaderUtils.loadClass(className, ConfigLsAction.class);
            Messages i18n = Messages.getInstance(cls);
            addActionError(i18n.format(e.getCode(), (Object[])e.getInfo()));
            return ERROR;
        }
    }

    private String doListing() throws FileSystemException
    {
        if (template)
        {
            List<TemplateNode> children;
            if (PathUtils.getPathElements(path).length == 1)
            {
                try
                {
                    TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(path);
                    children = Collections.singletonList(hierarchy.getRoot());
                }
                catch (IllegalArgumentException e)
                {
                    addActionError("Path '" + path + "' does not refer to a template scope.");
                    return ERROR;
                }
            }
            else
            {
                TemplateNode templateNode = configurationTemplateManager.getTemplateNode(path);
                if (templateNode == null)
                {
                    addActionError("Path '" + path + "' does not refer to a template item.");
                    return ERROR;
                }

                children = templateNode.getChildren();
            }

            Collection<FileModel> models = transform(children, new Function<TemplateNode, FileModel>()
            {
                public FileModel apply(TemplateNode node)
                {
                    String cls = configurationTemplateManager.isDeeplyValid(node.getPath()) ? null : "config-invalid";
                    String iconClass = node.isConcrete()? "config-concrete-icon" : "config-template-icon";
                    return new FileModel(node.getPath(), node.getId(), node.getChildren().size() > 0, cls, iconClass);
                }
            });

            listing = models.toArray(new FileModel[models.size()]);
            Arrays.sort(listing, new Comparator<FileModel>()
            {
                public int compare(FileModel o1, FileModel o2)
                {
                    return o1.getText().compareTo(o2.getText());
                }
            });
        }
        else
        {
            Configuration configuration = configurationTemplateManager.getInstance(path);
            if (configuration == null)
            {
                addActionError("Path '" + path + "' does not exist");
                return ERROR;
            }

            final ComplexType type = configurationTemplateManager.getType(path);
            Collection<FileModel> models = transform(ToveUtils.getPathListing(path, type, configurationTemplateManager, configurationSecurityManager), new Function<String, FileModel>()
            {
                public FileModel apply(String baseName)
                {
                    String childPath = PathUtils.getPath(path, baseName);
                    ComplexType childType = configurationTemplateManager.getType(childPath);
                    if (childType != null)
                    {
                        String text = ToveUtils.getDisplayName(path, childType, type, configurationTemplateManager.getRecord(childPath));
                        if (!StringUtils.stringSet(text))
                        {
                            text = baseName;
                        }

                        boolean hasChildren = ToveUtils.isFolder(childPath, configurationTemplateManager, configurationSecurityManager);
                        Configuration child = configurationTemplateManager.getInstance(childPath);
                        String cls = null;
                        if (child != null && !configurationTemplateManager.isDeeplyValid(childPath))
                        {
                            cls = "config-invalid";
                        }

                        String iconClass = ToveUtils.getIconCls(path, classificationManager);
                        return new FileModel(childPath, text, hasChildren, cls, iconClass);
                    }
                    else
                    {
                        // Could be a missing plugin.
                        // FIXME tree
                        return new FileModel(childPath, baseName, false, "config-invalid", null);
                    }
                }
            });

            listing = models.toArray(new FileModel[models.size()]);
        }

        return SUCCESS;
    }

//    private FileModel[] listChildren(final FileObject fileObject, final FileFilterSelector selector, final int currentDepth) throws FileSystemException
//    {
//        FileModel[] fileModels = null;
//        FileObject[] children = fileObject.findFiles(selector);
//        if (children != null)
//        {
//            sortChildren(fileObject, children);
//
//            final String baseUrl = configurationProvider == null ? "" : configurationProvider.get(GlobalConfiguration.class).getBaseUrl();
//            fileModels = transform(asList(children), new Function<FileObject, FileModel>()
//            {
//                public FileModel apply(FileObject child)
//                {
//                    FileModel fileModel = new FileModel(new FileObjectWrapper(child, fileObject), baseUrl);
//                    if (fileModel.getHasChildren() && currentDepth < depth)
//                    {
//                        try
//                        {
//                            fileModel.addChildren(listChildren(child, selector, currentDepth + 1));
//                        }
//                        catch (FileSystemException e)
//                        {
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    return fileModel;
//                }
//            }).toArray(new FileModel[children.length]);
//        }
//
//        return fileModels;
//    }

    private void sortChildren(FileObject fileObject, FileObject[] children)
    {
        Comparator<FileObject> comparator = getComparator(fileObject);
        if (comparator != null)
        {
            Arrays.sort(children, comparator);
        }
    }

    private Comparator<FileObject> getComparator(FileObject parentFile)
    {
        if(parentFile instanceof AbstractPulseFileObject)
        {
            try
            {
                ComparatorProvider provider = ((AbstractPulseFileObject) parentFile).getAncestor(ComparatorProvider.class);
                if (provider != null)
                {
                    return provider.getComparator();
                }
            }
            catch (FileSystemException e)
            {
                // Fall through to default.
            }
        }

        return new DirectoryComparator();
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setClassificationManager(ClassificationManager classificationManager)
    {
        this.classificationManager = classificationManager;
    }
}
