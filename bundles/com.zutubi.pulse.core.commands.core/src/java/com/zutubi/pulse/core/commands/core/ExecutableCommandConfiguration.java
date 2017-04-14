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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.commands.api.OutputProducingCommandConfigurationSupport;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmDirAction;
import com.zutubi.tove.annotations.*;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for the base command used for running an external process.
 * 
 * @see ExecutableCommand
 */
@SymbolicName("zutubi.executableCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "exe", "args", "extraArguments", "postProcessors", "inputFile", "outputFile", "force"})
public class ExecutableCommandConfiguration extends OutputProducingCommandConfigurationSupport implements Validateable
{
    @Transient
    private String exeProperty;
    @Transient
    private String defaultExe;

    @BrowseScmDirAction
    private String workingDir;
    private String exe;
    private String args;
    @Addable(value = "arg", attribute = "") @Wizard.Ignore @StringList
    private List<String> extraArguments = new LinkedList<String>();
    @Wizard.Ignore
    private String inputFile;
    @Addable("environment")
    private List<EnvironmentConfiguration> environments = new LinkedList<EnvironmentConfiguration>();
    @Addable("status-mapping")
    private List<StatusMappingConfiguration> statusMappings = new LinkedList<StatusMappingConfiguration>();

    public ExecutableCommandConfiguration()
    {
        super(ExecutableCommand.class);
    }

    public ExecutableCommandConfiguration(String name)
    {
        this();
        setName(name);
    }

    protected ExecutableCommandConfiguration(Class<? extends ExecutableCommand> clazz)
    {
        super(clazz);
    }

    protected ExecutableCommandConfiguration(Class<? extends ExecutableCommand> clazz, String exeProperty, String defaultExe)
    {
        super(clazz);
        this.exeProperty = exeProperty;
        this.defaultExe = defaultExe;
    }

    public String getExeProperty()
    {
        return exeProperty;
    }

    public String getDefaultExe()
    {
        return defaultExe;
    }

    public String getExe()
    {
        return exe;
    }

    public void setExe(String exe)
    {
        this.exe = exe;
    }

    public String getArgs()
    {
        return args;
    }

    public void setArgs(String args)
    {
        this.args = args;
    }

    public List<String> getExtraArguments()
    {
        return extraArguments;
    }

    public void setExtraArguments(List<String> extraArguments)
    {
        this.extraArguments = extraArguments;
    }

    @Transient
    public List<String> getCombinedArguments()
    {
        List<String> combined = new LinkedList<String>();
        if (StringUtils.stringSet(args))
        {
            combined.addAll(Arrays.asList(args.split("\\s+")));
        }
        
        combined.addAll(extraArguments);
        return Lists.newArrayList(Iterables.filter(combined, new Predicate<String>()
        {
            public boolean apply(String s)
            {
                return StringUtils.stringSet(s);
            }
        }));
    }

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getInputFile()
    {
        return inputFile;
    }

    public void setInputFile(String inputFile)
    {
        this.inputFile = inputFile;
    }

    public List<EnvironmentConfiguration> getEnvironments()
    {
        return environments;
    }

    public void setEnvironments(List<EnvironmentConfiguration> environments)
    {
        this.environments = environments;
    }

    public void addEnvironment(EnvironmentConfiguration environmentConfiguration)
    {
        environments.add(environmentConfiguration);
    }

    public List<StatusMappingConfiguration> getStatusMappings()
    {
        return statusMappings;
    }

    public void setStatusMappings(List<StatusMappingConfiguration> statusMappings)
    {
        this.statusMappings = statusMappings;
    }

    public void validate(ValidationContext context)
    {
        if (!StringUtils.stringSet(exe) && !StringUtils.stringSet(getDefaultExe()))
        {
            context.addFieldError("exe", "exe is required");
        }
    }
}
