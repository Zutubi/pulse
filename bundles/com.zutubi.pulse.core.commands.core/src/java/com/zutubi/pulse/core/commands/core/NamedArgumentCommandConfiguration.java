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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.annotations.Wizard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
@SymbolicName("zutubi.namedArgumentCommandConfig")
public abstract class NamedArgumentCommandConfiguration extends ExecutableCommandConfiguration
{
    public NamedArgumentCommandConfiguration()
    {
    }

    protected NamedArgumentCommandConfiguration(Class<? extends ExecutableCommand> clazz)
    {
        super(clazz);
    }

    protected NamedArgumentCommandConfiguration(Class<? extends ExecutableCommand> clazz, String exeProperty, String defaultExe)
    {
        super(clazz, exeProperty, defaultExe);
    }

    @Override
    public List<String> getCombinedArguments()
    {
        List<String> combined = new LinkedList<String>();
        List<NamedArgument> namedArguments = getNamedArguments();
        for (NamedArgument namedArgument: namedArguments)
        {
            combined.addAll(namedArgument.getArgs());
        }

        combined.addAll(super.getCombinedArguments());
        return combined;
    }

    @Override @Wizard.Ignore
    public String getExe()
    {
        return super.getExe();
    }

    @Transient
    protected abstract List<NamedArgument> getNamedArguments();

    public static class NamedArgument
    {
        String name;
        String value;
        List<String> args;

        public NamedArgument(String name, String value)
        {
            this(name, value, Arrays.asList(value));
        }

        public NamedArgument(String name, String value, String flag)
        {
            this(name, value, Arrays.asList(flag, value));
        }

        public NamedArgument(String name, String value, List<String> args)
        {
            this.name = name;
            this.value = value;
            this.args = args;
        }

        public String getName()
        {
            return name;
        }

        public String getValue()
        {
            return value;
        }

        public List<String> getArgs()
        {
            return args;
        }
    }
}
