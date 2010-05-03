package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.Result;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper base class for files that represents results shown on the details
 * tab.
 */
public abstract class AbstractResultDetailsFileObject extends AbstractPulseFileObject
{
    private static final Logger LOG = Logger.getLogger(AbstractResultDetailsFileObject.class);

    /**
     * Creates a new file with the given path in teh given file system.
     *
     * @param name the name of this file object instance
     * @param fs   the filesystem this file belongs to
     */
    protected AbstractResultDetailsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    @Override
    public String getIconCls()
    {
        try
        {
            return "status-" + getResult().getState().getString();
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        Map<String, Object> attributes = new HashMap<String, Object>();
        try
        {
            Map<String, Object> parentAttributes = ((AbstractPulseFileObject) getParent()).getExtraAttributes();
            if (parentAttributes != null)
            {
                attributes.putAll(parentAttributes);
            }
            attributes.put("resultId", getResult().getId());
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
        }
        
        return attributes;
    }

    /**
     * Returns the result this file represents.
     *  
     * @return the result this file represents
     */
    protected abstract Result getResult() throws FileSystemException;
}
