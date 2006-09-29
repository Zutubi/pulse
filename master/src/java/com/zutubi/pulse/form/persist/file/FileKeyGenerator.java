package com.zutubi.pulse.form.persist.file;

import com.zutubi.pulse.form.persist.PersistenceException;
import com.zutubi.pulse.form.persist.KeyGenerator;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.*;

/**
 * <class-comment/>
 */
public class FileKeyGenerator implements KeyGenerator
{
    private static final int PRE_ALLOCATION_SIZE = 20;

    private static final String KEY_FILENAME = "keys.properties";

    private File dir = null;

    private File keyFile = null;

    private Map<Class, List<Serializable>> keyCache = new HashMap<Class, List<Serializable>>();

    public FileKeyGenerator(File dir)
    {
        this.dir = dir;
    }

    public void setBaseDir(File dir)
    {
        this.dir = dir;
    }

    public synchronized Serializable generate(Class clazz) throws PersistenceException
    {
        // get the next key from the cache. If the cache is empty, allocate
        // the next batch of keys.
        if (!keyCache.containsKey(clazz) || keyCache.get(clazz).size() == 0)
        {
            allocateKeys(clazz);
        }

        List<Serializable> cachedKeys = keyCache.get(clazz);

        return cachedKeys.remove(0);
    }

    private void allocateKeys(Class type) throws PersistenceException
    {
        if (!keyCache.containsKey(type))
        {
            keyCache.put(type, new LinkedList<Serializable>());
        }

        List<Serializable> keys = keyCache.get(type);

        String keyName = type.getName();
        Properties props = loadFromFile();

        // allocate the next x keys.
        long start = Long.valueOf(props.getProperty(keyName, "0"));
        long finish = start + com.zutubi.pulse.form.persist.file.FileKeyGenerator.PRE_ALLOCATION_SIZE;
        props.put(keyName, Long.toString(finish));

        saveToFile(props);

        // fill the cache.
        for (long i = start + 1; i <= finish; i++)
        {
            keys.add(i);
        }
    }

    private void saveToFile(Properties props) throws PersistenceException
    {
        try
        {
            IOUtils.write(props, keyFile());
        }
        catch (IOException e)
        {
            throw new PersistenceException(e);
        }
    }

    private Properties loadFromFile() throws PersistenceException
    {
        try
        {
            return IOUtils.read(keyFile());
        }
        catch (IOException e)
        {
            throw new PersistenceException(e);
        }
    }

    private File keyFile() throws PersistenceException
    {
        if (keyFile == null)
        {
            keyFile = new File(dir, com.zutubi.pulse.form.persist.file.FileKeyGenerator.KEY_FILENAME);
            // ensure the file exists.
            if (!keyFile.exists())
            {
                try
                {
                    File parent = keyFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs())
                    {
                        throw new IOException();
                    }
                    keyFile.createNewFile();
                }
                catch (IOException e)
                {
                    throw new PersistenceException(e);
                }
            }
        }
        return keyFile;
    }
}
