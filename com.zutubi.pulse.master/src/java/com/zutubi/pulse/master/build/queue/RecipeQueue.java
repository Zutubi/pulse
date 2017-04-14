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

package com.zutubi.pulse.master.build.queue;

import java.util.List;

/**
 */
public interface RecipeQueue
{
    void enqueue(RecipeAssignmentRequest request);

    List<RecipeAssignmentRequest> takeSnapshot();

    /**
     * Attempts to cancel the request for the given recipe.
     *
     * @param id the id of the recipe to cancel the request for
     * @return true if the request was cancelled, false if it was missed
     */
    boolean cancelRequest(long id);

    void start();

    void stop();

    boolean isRunning();
}
