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

package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@SymbolicName("fakeRecipe")
public class FakeRecipe extends AbstractNamedConfiguration
{
    @Ordered
    private Map<String, FakeCommand> commands = new LinkedHashMap<String, FakeCommand>();

    public FakeRecipe()
    {
    }

    public FakeRecipe(String name)
    {
        super(name);
    }

    public Map<String, FakeCommand> getCommands()
    {
        return commands;
    }

    public void setCommands(Map<String, FakeCommand> commands)
    {
        this.commands = commands;
    }

    public void addCommand(FakeCommand command)
    {
        commands.put(command.getName(), command);
    }
}
