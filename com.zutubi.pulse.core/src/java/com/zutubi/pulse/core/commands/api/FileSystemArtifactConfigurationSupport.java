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
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper base for configuring artifacts that capture files from the local file
 * system.
 *
 * @see FileSystemArtifactSupport
 */
@SymbolicName("zutubi.fileSystemArtifactConfigSupport")
public abstract class FileSystemArtifactConfigurationSupport extends ArtifactConfigurationSupport
{
    @ControllingCheckbox(checkedFields = {"hashAlgorithm"})
    private boolean calculateHash;
    @Required
    private CommandContext.HashAlgorithm hashAlgorithm = CommandContext.HashAlgorithm.MD5;
    @Wizard.Ignore
    private boolean failIfNotPresent = true;
    @Wizard.Ignore
    private boolean ignoreStale = false;
    @Wizard.Ignore
    private String type;
    @Reference @Addable(value = "process", attribute = "processor")
    private List<PostProcessorConfiguration> postProcessors = new LinkedList<PostProcessorConfiguration>();

    /**
     * Indicates whether or not this file system artifact should be
     * published to the internal artifact repository.
     */
    @Wizard.Ignore
    @ControllingCheckbox(checkedFields = {"artifactPattern"})
    private boolean publish = false;

    /**
     * The default artifact pattern uses the files name for the
     * artifact name, and the files extension for the artifacts
     * extension.  The artifacts extension is optional.
     */
    @ValidRegex
    @Wizard.Ignore
    private String artifactPattern = "(.*?)(?:\\.([^.]*))?";

    protected FileSystemArtifactConfigurationSupport()
    {
    }

    protected FileSystemArtifactConfigurationSupport(String name)
    {
        super(name);
    }

    public boolean isCalculateHash()
    {
        return calculateHash;
    }

    public void setCalculateHash(boolean calculateHash)
    {
        this.calculateHash = calculateHash;
    }

    public CommandContext.HashAlgorithm getHashAlgorithm()
    {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(CommandContext.HashAlgorithm hashAlgorithm)
    {
        this.hashAlgorithm = hashAlgorithm;
    }

    public boolean isFailIfNotPresent()
    {
        return failIfNotPresent;
    }

    public void setFailIfNotPresent(boolean failIfNotPresent)
    {
        this.failIfNotPresent = failIfNotPresent;
    }

    public boolean isIgnoreStale()
    {
        return ignoreStale;
    }

    public void setIgnoreStale(boolean ignoreStale)
    {
        this.ignoreStale = ignoreStale;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isPublish()
    {
        return publish;
    }

    public void setPublish(boolean publish)
    {
        this.publish = publish;
    }

    public String getArtifactPattern()
    {
        return artifactPattern;
    }

    public void setArtifactPattern(String artifactPattern)
    {
        this.artifactPattern = artifactPattern;
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
