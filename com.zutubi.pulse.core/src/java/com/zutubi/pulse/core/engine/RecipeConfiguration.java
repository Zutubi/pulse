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

package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configures a sequence of commands to run as a single unit or recipe.
 */
@SymbolicName("zutubi.recipeConfig")
@Form(fieldOrder = {"name"})
public class RecipeConfiguration extends AbstractNamedConfiguration
{
    @Ordered
    private Map<String, CommandConfiguration> commands = new LinkedHashMap<String, CommandConfiguration>();

    public RecipeConfiguration()
    {
    }

    public RecipeConfiguration(String name)
    {
        super(name);
    }

    public Map<String, CommandConfiguration> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, CommandConfiguration> commands)
    {
        this.commands = commands;
    }

    public void addCommand(CommandConfiguration command)
    {
        commands.put(command.getName(), command);
    }
}
