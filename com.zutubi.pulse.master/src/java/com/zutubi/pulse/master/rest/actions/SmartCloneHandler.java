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
 * Handler for the smart clone (clone by introducing a new parent template) action.
 */
public class SmartCloneHandler extends AbstractCloneHandler
{
    @Override
    public ActionModel getModel(String path, String variant)
    {
        if (!configurationRefactoringManager.canSmartClone(path))
        {
            throw new IllegalArgumentException("Cannot smart clone path '" + path + "'");
        }

        return getModel(path, true);
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
    {
        return doAction(path, input, true);
    }

}
