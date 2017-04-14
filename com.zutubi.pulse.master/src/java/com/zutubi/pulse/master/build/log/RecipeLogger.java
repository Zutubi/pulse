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

package com.zutubi.pulse.master.build.log;

import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.events.build.RecipeDispatchedEvent;

/**
 * Creates the combined log of a recipe's execution.
 */
public interface RecipeLogger extends HookLogger
{
    void prepare();

    void log(RecipeAssignedEvent event);
    void log(RecipeDispatchedEvent event);
    void log(RecipeCommencedEvent event, RecipeResult result);
    void log(CommandCommencedEvent event, CommandResult result);
    void log(CommandCompletedEvent event, CommandResult result);
    void log(RecipeCompletedEvent event, RecipeResult result);
    void log(RecipeStatusEvent event);
    void log(RecipeErrorEvent event, RecipeResult result);

    void complete(RecipeResult result);

    void collecting(RecipeResult recipeResult);
    void collectionComplete();

    void cleaning();
    void cleaningComplete();

    void preStage();
    void preStageComplete();
    void postStage();
    void postStageComplete();

    void close();
}
