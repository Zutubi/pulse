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

import com.zutubi.util.StringUtils;

/**
 * Represents the result of a configuration check.
 */
public class CheckResultModel
{
    private boolean success;
    private String message;

    public CheckResultModel()
    {
        success = true;
    }

    public CheckResultModel(Exception e)
    {
        success = false;
        message = StringUtils.stringSet(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }
}
