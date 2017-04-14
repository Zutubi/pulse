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

package com.zutubi.tove.ui.model;

import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.type.TypeException;

/**
 * Holds the result of executing a config action.
 */
public class ActionResultModel
{
    private boolean success;
    private String message;
    private String newPath;
    private ConfigModel model;

    private ActionResultModel(boolean success, String message, String newPath, ConfigModel model)
    {
        this.success = success;
        this.message = message;
        this.newPath = newPath;
        this.model = model;
    }

    public ActionResultModel(ActionResult result, String newPath, ConfigModel model) throws TypeException
    {
        this(result.getStatus() == ActionResult.Status.SUCCESS, result.getMessage(), newPath, model);
    }

    public ActionResultModel(boolean success, String message, ConfigModel model)
    {
        this(success, message, null, model);
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }

    public String getNewPath()
    {
        return newPath;
    }

    public ConfigModel getModel()
    {
        return model;
    }
}
