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

package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves to the log page for a specific stage.
 */
public class StageLogActionResolver extends ParameterisedActionResolver
{
    private String stage;

    public StageLogActionResolver(String stage)
    {
        super("tailBuildLog");
        this.stage = stage;
    }

    public Map<String, String> getParameters()
    {
        Map<String, String> params = new HashMap<String, String>(1);
        params.put("stageName", stage);
        return params;
    }
}
