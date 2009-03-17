package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.core.ResourceFileLoader;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.ComparatorProvider;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * The root file of the reference documentation tree.  This contains static
 * pages with links to manuals etc, as well as generated documentation for
 * files, types etc.
 */
public class ReferenceFileObject extends AbstractPulseFileObject implements ComparatorProvider
{
    private static final Messages I18N = Messages.getInstance(ReferenceFileObject.class);

    private static final Map<String, String> FILE_TYPES = new HashMap<String, String>();

    {
        FILE_TYPES.put(PulseFileLoaderFactory.ROOT_ELEMENT, I18N.format("type.pulse"));
        FILE_TYPES.put(ResourceFileLoader.ROOT_ELEMENT, I18N.format("type.resource"));
    }

    public ReferenceFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName) throws Exception
    {
        return objectFactory.buildBean(FileTypeFileObject.class, new Class[]{FileName.class, AbstractFileSystem.class, String.class}, new Object[]{fileName, getFileSystem(), FILE_TYPES.get(fileName.getBaseName())});
    }

    @Override
    public String getIconCls()
    {
        return "reference-icon";
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[]{ PulseFileLoaderFactory.ROOT_ELEMENT, ResourceFileLoader.ROOT_ELEMENT };
    }

    public Comparator<FileObject> getComparator()
    {
        return null;
    }
}
