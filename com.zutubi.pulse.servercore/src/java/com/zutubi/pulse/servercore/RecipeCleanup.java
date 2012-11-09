package com.zutubi.pulse.servercore;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.RecipeStatusEvent;
import com.zutubi.util.io.FileSystem;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Class to remove stale recipe files from recipe directory.
 */
public class RecipeCleanup
{
    private static final Logger LOG = Logger.getLogger(RecipeCleanup.class);

    static final String DELETING_DIRECTORY = "Found stale recipe directory '%s', deleting...";
    static final String DELETING_FILE = "Found unexpected file '%s', deleting...";
    static final String DELETED = "Deleted.";
    static final String UNABLE_TO_DELETE_FILE = "Unable to delete file '%s': %s.";

    private FileSystem fileSystem;

    public RecipeCleanup(FileSystem fileSystem)
    {
        this.fileSystem = fileSystem;
    }

    public void cleanup(EventManager eventManager, File directoryToClean, long recipeId)
    {
        if (!directoryToClean.isDirectory())
        {
            // No recipes directory yet exists.
            return;
        }
        
        File[] files = directoryToClean.listFiles();
        if (files == null)
        {
            LOG.warning("Unable to list contents of recipes directory '" + directoryToClean.getAbsolutePath() + "'");
            return;
        }

        for (File node : files)
        {
            String statusMessage;
            if (node.isDirectory())
            {
                statusMessage = DELETING_DIRECTORY;
            }
            else
            {
                statusMessage = DELETING_FILE;
            }
            eventManager.publish(new RecipeStatusEvent(this, recipeId, String.format(statusMessage, node.getName())));
            try
            {
                fileSystem.delete(node);
                eventManager.publish(new RecipeStatusEvent(this, recipeId, DELETED));
            }
            catch (IOException e)
            {
                String message = String.format(UNABLE_TO_DELETE_FILE, node.getName(), e.getMessage());
                LOG.warning(message, e);
                eventManager.publish(new RecipeStatusEvent(this, recipeId, message));
            }
        }
    }
}

