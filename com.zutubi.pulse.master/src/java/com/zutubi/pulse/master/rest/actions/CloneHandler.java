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

package com.zutubi.pulse.master.rest.actions;

import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.ui.model.ActionModel;

import java.util.Map;

/**
 * Handler for the clone action (does not handle smart cloning).
 */
public class CloneHandler extends AbstractCloneHandler
{
    @Override
    public ActionModel getModel(String path, String variant)
    {
        if (!configurationRefactoringManager.canClone(path))
        {
            throw new IllegalArgumentException("Cannot clone path '" + path + "'");
        }

        return getModel(path, false);
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
    {
        return doAction(path, input, false);
    }
}
