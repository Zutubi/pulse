package com.zutubi.pulse.servercore;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.RecipeStatusEvent;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystem;
import org.mockito.InOrder;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class RecipeCleanupTest extends PulseTestCase
{
    private FileSystem fileSystem;
    private RecipeCleanup recipeCleanup;
    private EventManager eventManager;
    private File recipeDir;
    private InOrder inOrder;

    public void setUp()
    {
        fileSystem = mock(FileSystem.class);
        recipeCleanup = new RecipeCleanup(fileSystem);
        eventManager = mock(EventManager.class);
        recipeDir = mock(File.class);
        doReturn(Boolean.TRUE).when(recipeDir).isDirectory();
        inOrder = inOrder(eventManager);
    }

    public void verifyEvents(String... eventMessages)
    {
        for (String eventMessage : eventMessages)
        {
            inOrder.verify(eventManager).publish(new RecipeStatusEvent(this, 42, eventMessage));
        }

        verifyNoMoreInteractions(eventManager);
    }

    public void testCleanupNoSuchDirectory()
    {
        doReturn(Boolean.FALSE).when(recipeDir).isDirectory();
        recipeCleanup.cleanup(eventManager, recipeDir, 42);
        verify(recipeDir).isDirectory();
        verifyNoMoreInteractions(recipeDir);
    }

    public void testCleanupEmptyDirectory()
    {
        doReturn(new File[0]).when(recipeDir).listFiles();

        recipeCleanup.cleanup(eventManager, recipeDir, 42);
    }

    public void testCleanupOneFile() throws IOException
    {
        File file = mock(File.class);
        doReturn("filename").when(file).getName();
        File[] dirContents = new File[] { file };
        doReturn(dirContents).when(recipeDir).listFiles();

        recipeCleanup.cleanup(eventManager, recipeDir, 42);

        verify(fileSystem).delete(file);
        verifyEvents(String.format(RecipeCleanup.DELETING_FILE, "filename"), RecipeCleanup.DELETED);
    }

    public void testCleanupOneFileError() throws IOException
    {
        File file = mock(File.class);
        File[] dirContents = new File[] { file };
        doReturn(dirContents).when(recipeDir).listFiles();
        doReturn("filename").when(file).getName();
        doThrow(new IOException("disk error")).when(fileSystem).delete(file);

        recipeCleanup.cleanup(eventManager, recipeDir, 42);
        verify(fileSystem).delete(file);
        verifyEvents(String.format(RecipeCleanup.DELETING_FILE, "filename"), String.format(RecipeCleanup.UNABLE_TO_DELETE_FILE, "filename"));
    }

    public void testCleanupOneDirectory() throws IOException
    {
        File subDir = mock(File.class);
        File[] dirContents = new File[] { subDir };
        doReturn("dirname").when(subDir).getName();
        doReturn(dirContents).when(recipeDir).listFiles();
        doReturn(true).when(subDir).isDirectory();
        
        recipeCleanup.cleanup(eventManager, recipeDir, 42);

        verifyEvents(String.format(RecipeCleanup.DELETING_DIRECTORY, "dirname"), RecipeCleanup.DELETED);
        verify(fileSystem).delete(subDir);
    }

    public void testCleanupMix() throws IOException
    {
        File subDir1 = mock(File.class);
        File subDir2 = mock(File.class);
        File file = mock(File.class);
        File[] dirContents = new File[] { subDir1, file, subDir2 };
        doReturn(dirContents).when(recipeDir).listFiles();
        doReturn(true).when(subDir1).isDirectory();
        doReturn(true).when(subDir2).isDirectory();
        doReturn(false).when(file).isDirectory();
        doReturn("dir1").when(subDir1).getName();
        doReturn("dir2").when(subDir2).getName();
        doReturn("a_file").when(file).getName();

        recipeCleanup.cleanup(eventManager, recipeDir, 42);

        verify(fileSystem).delete(subDir1);
        verify(fileSystem).delete(file);
        verify(fileSystem).delete(subDir2);
        verifyEvents(String.format(RecipeCleanup.DELETING_DIRECTORY, "dir1"), RecipeCleanup.DELETED,
                String.format(RecipeCleanup.DELETING_FILE, "a_file"), RecipeCleanup.DELETED,
                String.format(RecipeCleanup.DELETING_DIRECTORY, "dir2"), RecipeCleanup.DELETED);
    }

    public void testCleanupDirectoryError()
    {
        doReturn(true).when(recipeDir).isDirectory();
        doReturn(null).when(recipeDir).listFiles();
        doReturn("/not/a/good/path").when(recipeDir).getAbsolutePath();
        recipeCleanup.cleanup(eventManager, recipeDir, 42);
    }

}
