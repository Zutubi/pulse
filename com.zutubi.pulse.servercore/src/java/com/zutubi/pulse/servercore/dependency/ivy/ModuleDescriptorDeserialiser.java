package com.zutubi.pulse.servercore.dependency.ivy;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import com.zutubi.pulse.core.dependency.ivy.IvyConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.repository.Resource;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * A hessian deserialiser for the ivy ModuleDescriptor
 */
public class ModuleDescriptorDeserialiser extends AbstractDeserializer
{
    private static final String FIELD_VALUE = "value";

    private File tmpDir;

    private IvyManager ivyManager;

    public Object readMap(AbstractHessianInput input) throws IOException
    {
        // we serialised the object as a map, so we need to read it as one.

        String descriptor = null;

        while (!input.isEnd())
        {
            String key = input.readString();
            String value = input.readString();

            if (key.equals(FIELD_VALUE))
            {
                descriptor = value;
            }
        }

        // we have the data we need or are at the end of the map, so indicate
        // this accordingly.
        input.readMapEnd();

        // if we did not locate a descriptor (value field) then something went wrong
        // in the transfer.
        if (!StringUtils.stringSet(descriptor))
        {
            throw new IOException("Invalid descriptor string.");
        }

        return parseDescriptor(descriptor);
    }

    private ModuleDescriptor parseDescriptor(String descriptor) throws IOException
    {
        // ivy seems to prefer to parse its descriptors from a file, so we write the
        // string to a temporary file first.

        File tmp = null;
        try
        {
            tmp = createTempFile();

            FileSystemUtils.createFile(tmp, descriptor);

            // the name for this resource is pretty arbitrary at the moment, just something that
            // is at least somewhat indicative of what the resource represents.  I don't think it
            // is getting used anywhere - at least not this particular instances name.
            Resource res = new MemoryResource("ivy.xml", descriptor);
            ModuleDescriptorParser parser = ModuleDescriptorParserRegistry.getInstance().getParser(res);
            
            IvySettings ivySettings = new IvyConfiguration().loadDefaultSettings();

            return parser.parseDescriptor(ivySettings, tmp.toURI().toURL(), res, ivySettings.doValidate());
        }
        catch (ParseException e)
        {
            throw new IOException(e.getMessage());
        }
        finally
        {
            deleteTempFile(tmp);
        }
    }

    private void deleteTempFile(File tmp)
    {
        if (tmp != null && tmp.isFile())
        {
            if (!tmp.delete())
            {
                // if we can not delete it right now, mark it for
                // deletion later.
                tmp.deleteOnExit();
            }
        }
    }

    private File createTempFile() throws IOException
    {
        if (tmpDir != null && tmpDir.isDirectory())
        {
            return FileSystemUtils.createTempFile(tmpDir);
        }
        else
        {
            return File.createTempFile("ivy", "xml");
        }
    }

    public void setTmpDir(File tmpDir)
    {
        this.tmpDir = tmpDir;
    }

    public void setIvyManager(IvyManager ivyManager)
    {
        this.ivyManager = ivyManager;
    }
}
