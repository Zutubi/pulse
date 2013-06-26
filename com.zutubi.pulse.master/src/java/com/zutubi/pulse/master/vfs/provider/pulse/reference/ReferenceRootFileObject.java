package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
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
public class ReferenceRootFileObject extends AbstractReferenceFileObject implements ComparatorProvider
{
    private static final Messages I18N = Messages.getInstance(ReferenceRootFileObject.class);

    private static final Map<String, String> FILE_TYPES = new HashMap<String, String>();

    static
    {
        FILE_TYPES.put(PulseFileLoaderFactory.ROOT_ELEMENT, I18N.format("type.pulse"));
        FILE_TYPES.put(ResourceFileLoader.ROOT_ELEMENT, I18N.format("type.resource"));
    }

    public ReferenceRootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    protected String[] getDynamicChildren()
    {
        return new String[]{ PulseFileLoaderFactory.ROOT_ELEMENT, ResourceFileLoader.ROOT_ELEMENT };
    }

    public AbstractPulseFileObject createDynamicFile(FileName fileName)
    {
        return objectFactory.buildBean(FileTypeFileObject.class, fileName, getFileSystem(), FILE_TYPES.get(fileName.getBaseName()));
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

    public Comparator<FileObject> getComparator()
    {
        return null;
    }
}
