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

package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.servercore.RecipeRunner;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.util.logging.Logger;

/**
 * A {@link com.zutubi.pulse.servercore.RecipeRunner} implementation that wraps
 * another runner and catches any build or unexpected exceptions that it throws.
 *  The exceptions are reported back to the master as recipe errors.  This
 * prevents an error killing the build on a slave without the master realising
 * it.
 */
public class ErrorHandlingRecipeRunner implements RecipeRunner
{
    private static final Logger LOG = Logger.getLogger(ErrorHandlingRecipeRunner.class);

    private MasterService master;
    private String serviceToken;
    private long buildId;
    private long recipeId;
    private RecipeRunner delegate;

    public ErrorHandlingRecipeRunner(MasterService master, String serviceToken,long buildId, long recipeId, RecipeRunner delegate)
    {
        this.master = master;
        this.serviceToken = serviceToken;
        this.buildId = buildId;
        this.recipeId = recipeId;
        this.delegate = delegate;
    }

    public void runRecipe(RecipeRequest request, RecipeProcessor recipeProcessor)
    {
        try
        {
            delegate.runRecipe(request, recipeProcessor);
        }
        catch (BuildException e)
        {
            LOG.warning(e);
            sendError(e.getMessage());
        }
        catch (Exception e)
        {
            LOG.severe(e);
            sendError("Unexpected error: " + e.getMessage());
        }
    }

    private void sendError(String error)
    {
        RecipeErrorEvent event = new RecipeErrorEvent(null, buildId, recipeId, error, false);

        try
        {
            master.handleEvent(serviceToken, event);
        }
        catch (RuntimeException e)
        {
            LOG.warning("Unable to send error to master '" + master + "': " + e.getMessage(), e);
        }
    }

}
