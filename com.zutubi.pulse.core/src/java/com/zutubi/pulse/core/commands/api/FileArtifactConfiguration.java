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

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a capture of a single file from the local file sysytem.
 *
 * @see FileArtifact
 */
@SymbolicName("zutubi.fileArtifactConfig")
@Form(fieldOrder = {"name", "file", "featured", "postProcessors", "calculateHash", "hashAlgorithm", "type", "failIfNotPresent", "ignoreStale", "publish", "artifactPattern"})
public class FileArtifactConfiguration extends FileSystemArtifactConfigurationSupport
{
    @Required
    private String file;

    public FileArtifactConfiguration()
    {
    }

    public FileArtifactConfiguration(String name, String file)
    {
        super(name);
        this.file = file;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public Class<? extends Artifact> artifactType()
    {
        return FileArtifact.class;
    }
}
