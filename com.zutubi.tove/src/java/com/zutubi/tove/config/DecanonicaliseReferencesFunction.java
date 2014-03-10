package com.zutubi.tove.config;

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;

/**
 * A function that walks a record tree de-canonicalising all references found.  This changes the
 * referenced handles to point to the referee in the same owner (as opposed to a canonical
 * reference which uses the handle of the template owner of the referee).
 */
public class DecanonicaliseReferencesFunction extends ReferenceUpdatingFunction
{
    private String templateOwnerPath;
    private RecordManager recordManager;
    private ConfigurationReferenceManager configurationReferenceManager;

    public DecanonicaliseReferencesFunction(CompositeType type, MutableRecord record, String path, RecordManager recordManager, ConfigurationReferenceManager configurationReferenceManager)
    {
        super(type, record, path);
        templateOwnerPath = PathUtils.getPrefix(path, 2);
        this.recordManager = recordManager;
        this.configurationReferenceManager = configurationReferenceManager;
    }

    @Override
    protected String updateReference(String value)
    {
        long handle = Long.parseLong(value);
        if (handle == 0)
        {
            return "0";
        }
        else
        {
            String referencedPath = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, handle);
            handle = recordManager.select(referencedPath).getHandle();
            return Long.toString(handle);
        }
    }
}
