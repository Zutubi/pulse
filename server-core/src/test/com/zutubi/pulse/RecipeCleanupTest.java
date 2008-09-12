package com.zutubi.pulse;

import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeStatusEvent;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystem;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

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

    public void testCleanupEmptyDirectory()
    {
        stub(recipeDir.listFiles()).toReturn(new File[0]);

        recipeCleanup.cleanup(eventManager, recipeDir, 42);
    }

    public void testCleanupOneFile() throws IOException
    {
        File file = mock(File.class);
        File[] dirContents = new File[] { file };
        stub(recipeDir.listFiles()).toReturn(dirContents);
        stub(file.getName()).toReturn("filename");

        recipeCleanup.cleanup(eventManager, recipeDir, 42);

        verify(fileSystem).delete(file);
        verifyEvents(String.format(RecipeCleanup.DELETING_FILE, "filename"), RecipeCleanup.DELETED);
    }

    public void testCleanupOneFileError() throws IOException
    {
        File file = mock(File.class);
        File[] dirContents = new File[] { file };
        stub(recipeDir.listFiles()).toReturn(dirContents);
        stub(file.getName()).toReturn("filename");
        stubVoid(fileSystem).toThrow(new IOException("disk error")).on().delete(file);

        recipeCleanup.cleanup(eventManager, recipeDir, 42);
        verify(fileSystem).delete(file);
        verifyEvents(String.format(RecipeCleanup.DELETING_FILE, "filename"), String.format(RecipeCleanup.UNABLE_TO_DELETE_FILE, "filename"));
    }

    public void testCleanupOneDirectory() throws IOException
    {
        File subDir = mock(File.class);
        File[] dirContents = new File[] { subDir };
        stub(subDir.getName()).toReturn("dirname");
        stub(recipeDir.listFiles()).toReturn(dirContents);
        stub(subDir.isDirectory()).toReturn(true);
        
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
        stub(recipeDir.listFiles()).toReturn(dirContents);
        stub(subDir1.isDirectory()).toReturn(true);
        stub(subDir2.isDirectory()).toReturn(true);
        stub(file.isDirectory()).toReturn(false);
        stub(subDir1.getName()).toReturn("dir1");
        stub(subDir2.getName()).toReturn("dir2");
        stub(file.getName()).toReturn("a_file");

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
        stub(recipeDir.isDirectory()).toReturn(true);
        stub(recipeDir.listFiles()).toReturn(null);
        stub(recipeDir.getAbsolutePath()).toReturn("/not/a/good/path");
        recipeCleanup.cleanup(eventManager, recipeDir, 42);
    }

}
