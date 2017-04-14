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

    public void cleanup(EventManager eventManager, File directoryToClean, long buildId, long recipeId)
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
            eventManager.publish(new RecipeStatusEvent(this, buildId, recipeId, String.format(statusMessage, node.getName())));
            try
            {
                fileSystem.delete(node);
                eventManager.publish(new RecipeStatusEvent(this, buildId, recipeId, DELETED));
            }
            catch (IOException e)
            {
                String message = String.format(UNABLE_TO_DELETE_FILE, node.getName(), e.getMessage());
                LOG.warning(message, e);
                eventManager.publish(new RecipeStatusEvent(this, buildId, recipeId, message));
            }
        }
    }
}

