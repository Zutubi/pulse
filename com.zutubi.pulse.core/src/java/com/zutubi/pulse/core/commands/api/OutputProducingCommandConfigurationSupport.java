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

package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper base for configuration for commands that produce output that may be
 * captured and/or processed.
 *
 * @see com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport
 */
@SymbolicName("zutubi.outputProducingCommandConfigSupport")
public abstract class OutputProducingCommandConfigurationSupport extends CommandConfigurationSupport
{
    @Wizard.Ignore
    private String outputFile;
    @Reference @Addable(value = "process", attribute = "processor")
    private List<PostProcessorConfiguration> postProcessors = new LinkedList<PostProcessorConfiguration>();

    public OutputProducingCommandConfigurationSupport(Class<? extends Command> commandType)
    {
        super(commandType);
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public void setOutputFile(String outputFile)
    {
        this.outputFile = outputFile;
    }

    public List<PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        postProcessors.add(postProcessor);
    }
}
