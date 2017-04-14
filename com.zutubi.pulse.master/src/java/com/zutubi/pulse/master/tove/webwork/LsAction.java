/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.webwork;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.vfs.CompoundFileFilter;
import com.zutubi.pulse.master.vfs.FilePrefixFilter;
import com.zutubi.pulse.master.vfs.VfsManagerFactoryBean;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ComparatorProvider;
import com.zutubi.pulse.master.xwork.actions.vfs.DirectoryComparator;
import com.zutubi.pulse.master.xwork.actions.vfs.FileObjectWrapper;
import com.zutubi.pulse.master.xwork.actions.vfs.VFSActionSupport;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.UriParser;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

/**
 * The ls action provides access to 'ls' style functionality for the web ui.
 */
public class LsAction extends VFSActionSupport
{
    private String fs ="pulse";
    private String prefix;

    /**
     * The base path for the request.  This, combined with the {@link #path}
     * define the path to be listed.
     */
    private String basePath;

    /**
     * The path, relative to the base path, that defines what should be listed.
     */
    private String path;

    /**
     * The results of the ls action.
     */
    private ExtFile[] listing;

    /**
     * Show files indicates whether or not the listing should include files. The default value is false.
     */
    private boolean showFiles = true;

    /**
     * Show files that are marked as hidden. The default value for this is false.
     */
    private boolean showHidden;

    /**
     * Number of levels, under the one being listed, to also load.  The default
     * of zero means just list this path's direct children.
     */
    private int depth = 0;

    /**
     * If not null, the name of a property to filter files by.  This is the
     * name of a boolean bean property on the file object.  Files without such
     * a property are always passed by the filter.
     */
    private String filterFlag = null;

    private StartupManager startupManager;
    private SystemConfiguration systemConfiguration;

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setFs(String fs)
    {
        this.fs = fs;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public void setShowFiles(boolean showFiles)
    {
        this.showFiles = showFiles;
    }

    public void setShowHidden(boolean showHidden)
    {
        this.showHidden = showHidden;
    }

    public void setFilterFlag(String filterFlag)
    {
        this.filterFlag = filterFlag;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public ExtFile[] getListing()
    {
        return listing;
    }

    private void checkAccess()
    {
        if (VfsManagerFactoryBean.FS_LOCAL.equals(fs) && startupManager.isSystemStarted())
        {
            accessManager.ensurePermission(ServerPermission.ADMINISTER.name(), null);
        }
    }

    public String execute() throws Exception
    {
        try
        {
            checkAccess();
            return doListing();
        }
        catch (FileSystemException e)
        {
            String className = e.getStackTrace()[0].getClassName();
            Class cls = ClassLoaderUtils.loadClass(className, LsAction.class);
            Messages i18n = Messages.getInstance(cls);
            addActionError(i18n.format(e.getCode(), (Object[])e.getInfo()));
            return ERROR;
        }
    }

    private String doListing() throws FileSystemException
    {
        String fullPath = fs + "://";
        if(StringUtils.stringSet(basePath))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(basePath));
        }
        if(StringUtils.stringSet(path))
        {
            fullPath += "/" + UriParser.encode(PathUtils.normalisePath(path));
        }

        final FileObject fileObject = getFS().resolveFile(fullPath);

        // can only list a file object if
        // a) it is a directory
        if (fileObject.getType() != FileType.FOLDER)
        {
            return ERROR;
        }

        // b) the user has read permissions.
        if (!fileObject.isReadable())
        {
            addActionError("You do not have permission to list this folder.");
            return ERROR;
        }

        Collection<FileType> acceptedTypes = new HashSet<FileType>();
        acceptedTypes.add(FileType.FOLDER);
        if (showFiles)
        {
            acceptedTypes.add(FileType.FILE);
        }

        FileFilterSelector selector = new FileFilterSelector(
                new CompoundFileFilter(
                        new FileTypeFilter(acceptedTypes),
                        new HiddenFileFilter(showHidden),
                        new FilePrefixFilter(prefix),
                        new FlagFileFilter(filterFlag)
                )
        );

        listing = listChildren(fileObject, selector, 0);

        return SUCCESS;
    }

    private ExtFile[] listChildren(final FileObject fileObject, final FileFilterSelector selector, final int currentDepth) throws FileSystemException
    {
        ExtFile[] extFiles = null;
        FileObject[] children = fileObject.findFiles(selector);
        if (children != null)
        {
            sortChildren(fileObject, children);

            extFiles = transform(asList(children), new Function<FileObject, ExtFile>()
            {
                public ExtFile apply(FileObject child)
                {
                    ExtFile extFile = new ExtFile(new FileObjectWrapper(child, fileObject), systemConfiguration.getContextPath());
                    if (!extFile.isLeaf() && currentDepth < depth)
                    {
                        try
                        {
                            extFile.addChildren(listChildren(child, selector, currentDepth + 1));
                        }
                        catch (FileSystemException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }

                    return extFile;
                }
            }).toArray(new ExtFile[children.length]);
        }

        return extFiles;
    }

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

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setSystemConfiguration(SystemConfiguration systemConfiguration) {
        this.systemConfiguration = systemConfiguration;
    }

    /**
     * Filter that accepts only specified types of files.
     */
    private static class FileTypeFilter implements FileFilter
    {
        private Collection<FileType> acceptedTypes = new HashSet<FileType>();

        private FileTypeFilter(Collection<FileType> acceptedTypes)
        {
            this.acceptedTypes = acceptedTypes;
        }

        public boolean accept(final FileSelectInfo fileInfo)
        {
            try
            {
                return acceptedTypes.contains(fileInfo.getFile().getType());
            }
            catch (FileSystemException e)
            {
                return false;
            }
        }
    }

    /**
     * Filter based on the files hidden flag.
     */
    private static class HiddenFileFilter implements FileFilter
    {
        private boolean showHidden;

        private HiddenFileFilter(boolean showHidden)
        {
            this.showHidden = showHidden;
        }

        public boolean accept(FileSelectInfo fileSelectInfo)
        {
            try
            {
                FileObject file = fileSelectInfo.getFile();
                return showHidden || !file.isHidden();
            }
            catch (FileSystemException e)
            {
                return false;
            }
        }
    }

    /**
     * Filter based on arbitrary flags represented by bean properties.  The
     * property must be true for the filter to pass.
     */
    private static class FlagFileFilter implements FileFilter
    {
        /**
         * Keeps a cache of (class, flag name) pairs mapped to (boolean,
         * method) pairs.  The map entry boolean indicates if a read method
         * exists.
         */
        private static final Map<Pair<Class, String>, Method> cache = new HashMap<Pair<Class, String>, Method>();

        private String flag;

        private FlagFileFilter(String flag)
        {
            this.flag = flag;
        }

        public boolean accept(FileSelectInfo fileSelectInfo)
        {
            if (!StringUtils.stringSet(flag))
            {
                return true;
            }

            FileObject file = fileSelectInfo.getFile();
            Class fileClass = file.getClass();
            Method method = lookup(fileClass);
            if (method != null)
            {
                try
                {
                    return (Boolean) method.invoke(file);
                }
                catch (Exception e)
                {
                    return false;
                }
            }
            else
            {
                // Flag doesn't exist in the file class, don't filter this type
                // of file.
                return true;
            }
        }

        private Method lookup(Class fileClass)
        {
            Method method;
            synchronized (cache)
            {
                Pair<Class, String> key = asPair(fileClass, flag);
                if (cache.containsKey(key))
                {
                    method = cache.get(key);
                }
                else
                {
                    method = introspect(fileClass);
                    cache.put(key, method);
                }
            }
            return method;
        }

        private Method introspect(Class fileClass)
        {
            try
            {
                BeanInfo beanInfo = Introspector.getBeanInfo(fileClass);
                PropertyDescriptor descriptor = find(asList(beanInfo.getPropertyDescriptors()), new Predicate<PropertyDescriptor>()
                {
                    public boolean apply(PropertyDescriptor propertyDescriptor)
                    {
                        if (propertyDescriptor.getName().equals(flag))
                        {
                            Method readMethod = propertyDescriptor.getReadMethod();
                            return readMethod != null && readMethod.getReturnType() == Boolean.TYPE;
                        }

                        return false;
                    }
                }, null);

                if (descriptor != null)
                {
                    return descriptor.getReadMethod();
                }
            }
            catch (IntrospectionException e)
            {
                // Fall through.
            }

            return null;
        }
    }
}
