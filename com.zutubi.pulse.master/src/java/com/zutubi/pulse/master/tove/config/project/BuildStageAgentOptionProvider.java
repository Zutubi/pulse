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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.DefaultReferenceOptionProvider;
import com.zutubi.tove.ui.forms.FormContext;

import java.util.Map;

/**
 * An option provider for the agent field of a build stage.
 */
public class BuildStageAgentOptionProvider extends DefaultReferenceOptionProvider
{
    public Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        Map<String, String> allAgents = super.getMap(property, context);
        allAgents.put("", "[any capable]");
        return allAgents;
    }
}
