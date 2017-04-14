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
