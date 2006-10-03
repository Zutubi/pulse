package com.zutubi.pulse.form.persist.file;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.form.persist.file.mock.MockIdField;
import com.zutubi.pulse.form.persist.file.mock.MockNoIdField;
import com.zutubi.pulse.form.persist.file.mock.MockBook;
import com.zutubi.pulse.form.persist.file.mock.MockBookWithId;
import com.zutubi.pulse.form.persist.reflection.ReflectionDescriptorFactory;
import com.zutubi.pulse.form.persist.PersistenceException;
import com.zutubi.pulse.form.persist.ObjectNotFoundException;

import java.io.File;

/**
 * <class-comment/>
 */
public class FileObjectStoreTest extends PulseTestCase
{
    private FileObjectStore objectStore;

    private File baseDir = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        baseDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".tmp");

        objectStore = new FileObjectStore();
        objectStore.setBaseDir(baseDir);
        objectStore.setDescriptorFactory(new ReflectionDescriptorFactory());
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(baseDir);

        super.tearDown();
    }

    public void testHasId()
    {
        assertTrue(objectStore.hasId(MockIdField.class));
        assertFalse(objectStore.hasId(MockNoIdField.class));
    }

    public void testGenerateId() throws PersistenceException
    {
        assertEquals(1L, objectStore.generateId(MockIdField.class));
        assertEquals(1L, objectStore.generateId(MockNoIdField.class));
    }

    public void testSave() throws PersistenceException
    {
        MockBook mockBook = new MockBook();
        mockBook.setTitle("Book Title");
        mockBook.setPageCount(1234321);

        objectStore.save(1L, mockBook);

        MockBook persistedBook = (MockBook) objectStore.load(MockBook.class, 1L);
        assertEquals("Book Title", persistedBook.getTitle());
        assertEquals(1234321, persistedBook.getPageCount());
    }

    public void testSaveWithId() throws PersistenceException
    {
        MockBookWithId mockBook = new MockBookWithId();
        mockBook.setTitle("Book Title");

        objectStore.save(mockBook);
        assertEquals(1L, mockBook.getId());

        MockBookWithId persistentBook = (MockBookWithId) objectStore.load(MockBookWithId.class, mockBook.getId());
        assertEquals("Book Title", persistentBook.getTitle());
    }

    public void testSaveOrUpdateWithId() throws PersistenceException
    {
        MockBookWithId mockBook = new MockBookWithId();
        mockBook.setTitle("Book Title");

        objectStore.saveOrUpdate(mockBook);
        assertEquals(1L, mockBook.getId());

        mockBook.setTitle("Updated Title");
        objectStore.saveOrUpdate(mockBook);
        assertEquals(1L, mockBook.getId());

        MockBookWithId persistentBook = (MockBookWithId) objectStore.load(MockBookWithId.class, mockBook.getId());
        assertEquals("Updated Title", persistentBook.getTitle());
    }

    public void testUpdate() throws PersistenceException
    {
        MockBook mockBook = new MockBook();
        mockBook.setTitle("Book Title");
        mockBook.setPageCount(1234321);

        objectStore.save(1L, mockBook);

        mockBook.setTitle("Updated Book Title");
        objectStore.update(1L, mockBook);

        MockBook persistedBook = (MockBook) objectStore.load(MockBook.class, 1L);
        assertEquals("Updated Book Title", persistedBook.getTitle());
        assertEquals(1234321, persistedBook.getPageCount());
    }

    public void testSaveUsingExistingIdThrowsException() throws PersistenceException
    {
        MockBook mockBook = new MockBook();
        mockBook.setTitle("Book Title");
        mockBook.setPageCount(1234321);

        objectStore.save(1L, mockBook);
        try
        {
            objectStore.save(1L, mockBook);
            fail();
        }
        catch (PersistenceException e)
        {
            // expected.
        }
    }

    public void testDelete() throws PersistenceException
    {
        MockBook mockBook = new MockBook();
        mockBook.setTitle("Book Title");
        mockBook.setPageCount(1234321);

        objectStore.save(1L, mockBook);

        objectStore.delete(1L, MockBook.class);

        try
        {
            objectStore.load(MockBook.class, 1L);
            fail();
        }
        catch (ObjectNotFoundException e)
        {
            // expected
        }
    }

    public void testDeleteWithId() throws PersistenceException
    {
        MockBookWithId mockBook = new MockBookWithId();
        mockBook.setTitle("Book Title");

        objectStore.save(mockBook);
        objectStore.delete(mockBook);

        try
        {
            objectStore.load(MockBook.class, mockBook.getId());
            fail();
        }
        catch (ObjectNotFoundException e)
        {
            // expected
        }
    }

    public void testDeletingNonExistantObject() throws PersistenceException
    {
        objectStore.delete(1L, MockBook.class);
    }

}
