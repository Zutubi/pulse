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

/**
 * Support base class for output capturing types.  Stores the configuration.
 */
public abstract class ArtifactSupport implements Artifact
{
    private ArtifactConfiguration config;

    /**
     * Creates a new output based on the given configuration.
     *
     * @param config configuration for this output
     */
    protected ArtifactSupport(ArtifactConfiguration config)
    {
        this.config = config;
    }

    /**
     * Returns the configuration for this output.
     *
     * @return the configuration for this output
     */
    public ArtifactConfiguration getConfig()
    {
        return config;
    }
}
