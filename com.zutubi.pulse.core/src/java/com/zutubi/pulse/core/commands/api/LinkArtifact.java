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
 * An artifact that "captures" a link to an external resource, for presentation
 * along other outputs in the build result.
 */
public class LinkArtifact implements Artifact
{
    private LinkArtifactConfiguration config;

    /**
     * Constructor that stores the configuration.
     *
     * @param config configuration for this artifact
     */
    public LinkArtifact(LinkArtifactConfiguration config)
    {
        this.config = config;
    }

    public void capture(CommandContext context)
    {
        context.registerLink(config.getName(), config.getUrl(), true, config.isFeatured());
    }
}
