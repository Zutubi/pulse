package com.zutubi.pulse.form.persist.file;

import com.zutubi.pulse.form.persist.*;
import com.zutubi.pulse.util.IOUtils;
import com.thoughtworks.xstream.XStream;

import java.io.*;

/**
 * <class-comment/>
 */
public class FileObjectStore implements ObjectStore
{
    private File baseDir;

    private KeyGenerator keyGenerator;

    private DescriptorFactory descriptorFactory;

    public Object load(Class clazz, Serializable id) throws PersistenceException
    {
        return readFromStorage(id, clazz);
    }

    public void save(Object obj) throws PersistenceException
    {
        verifyIdAvailable(obj);

        Serializable id = generateId(obj.getClass());
        setId(id, obj);
        save(id, obj);
    }

    public void saveOrUpdate(Object obj) throws PersistenceException
    {
        verifyIdAvailable(obj);

        // if an id exists, attempt an update, else attempt a save.
        if (isPersistent(obj))
        {
            update(obj);
        }
        else
        {
            save(obj);
        }
    }

    public void update(Object obj) throws PersistenceException
    {
        verifyIdAvailable(obj);
        verifyPersistent(obj);

        Serializable id = getId(obj);
        update(id, obj);
    }

    public boolean delete(Object obj) throws PersistenceException
    {
        verifyIdAvailable(obj);
        verifyPersistent(obj);

        Serializable id = getId(obj);
        return delete(id, obj.getClass());
    }

    private void verifyIdAvailable(Object obj) throws PersistenceException
    {
        PersistenceDescriptor descriptor = descriptorFactory.createDescriptor(obj.getClass());
        if (descriptor.getIdProperty() == null)
        {
            throw new PersistenceException();
        }
    }

    private boolean isPersistent(Object obj) throws PersistenceException
    {
        Serializable id = getId(obj);
        if (id == null)
        {
            return false;
        }
        if (id instanceof Number)
        {
            return ((Number)id).longValue()!= 0;
        }
        return true;
    }

    private void verifyPersistent(Object obj) throws PersistenceException
    {
        if (!isPersistent(obj))
        {
            throw new PersistenceException();
        }
    }

    private Serializable getId(Object obj) throws PersistenceException
    {
        PersistenceDescriptor descriptor = descriptorFactory.createDescriptor(obj.getClass());
        try
        {
            return (Serializable)descriptor.getReaderMethod().invoke(obj);
        }
        catch (Exception e)
        {
            throw new PersistenceException();
        }
    }

    private void setId(Serializable id, Object obj) throws PersistenceException
    {
        PersistenceDescriptor descriptor = descriptorFactory.createDescriptor(obj.getClass());
        try
        {
            descriptor.getWriterMethod().invoke(obj, id);
        }
        catch (Exception e)
        {
            throw new PersistenceException();
        }
    }

    public void save(Serializable id, Object obj) throws PersistenceException
    {
        if (storageFile(id, obj.getClass()).exists())
        {
            throw new PersistenceException();
        }

        writeToStorage(id, obj);
    }

    private void writeToStorage(Serializable id, Object obj) throws PersistenceException
    {
        File f = storageFile(id, obj.getClass());
        if (!f.exists())
        {
            createStorageFile(f);
        }

        Writer writer = null;
        try
        {
            XStream xstream = new XStream();
            writer = new OutputStreamWriter(new FileOutputStream(f));
            xstream.toXML(obj, writer);
        }
        catch (FileNotFoundException e)
        {
            // We have already checked this.
            e.printStackTrace();
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    private Object readFromStorage(Serializable id, Class clazz) throws ObjectNotFoundException
    {
        // load the object..
        File f = storageFile(id, clazz);
        if (!f.isFile())
        {
            throw new ObjectNotFoundException();
        }

        Reader reader = null;
        try
        {
            XStream xstream = new XStream();
            reader = new InputStreamReader(new FileInputStream(f));
            return xstream.fromXML(reader);
        }
        catch (FileNotFoundException e)
        {
            // We have already checked this.
            e.printStackTrace();
            throw new ObjectNotFoundException();
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    public void update(Serializable id, Object obj) throws PersistenceException
    {
        writeToStorage(id, obj);
    }

    public boolean delete(Serializable id, Class clazz) throws PersistenceException
    {
        File f = storageFile(id, clazz);
        if (!f.isFile())
        {
            // does not exist.
            return false;
        }

        if (!f.delete())
        {
            throw new PersistenceException("Failed to delete.");
        }
        return true;
    }

    public boolean hasId(Class clazz)
    {
        // analyse the object to see if it has an id field defined.
        PersistenceDescriptor descriptor = descriptorFactory.createDescriptor(clazz);
        return descriptor.getIdProperty() != null;
    }

    public Serializable generateId(Class clazz) throws PersistenceException
    {
        // assuming we use the default key generator...
        return keyGenerator.generate(clazz);
    }

    private void createStorageFile(File f) throws PersistenceException
    {
        if (f.isFile())
        {
            return;
        }

        if (f.exists())
        {
            // already exists but is not a file.
            throw new PersistenceException();
        }

        if (!f.getParentFile().exists() && !f.getParentFile().mkdirs())
        {
            throw new PersistenceException();
        }

        try
        {
            if (!f.createNewFile())
            {
                throw new PersistenceException();
            }
        }
        catch (IOException e)
        {
            throw new PersistenceException(e);
        }
    }

    private File storageFile(Serializable id, Class type)
    {
        return new File(baseDir, type.getName() + "/" + id + ".xml");
    }

    public void setBaseDir(File baseDir)
    {
        this.baseDir = baseDir;
        this.keyGenerator = new FileKeyGenerator(this.baseDir);
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }
}
