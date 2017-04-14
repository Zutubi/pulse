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

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 * Basic interface for artifact capture configurations.
 */
@SymbolicName("zutubi.artifactConfig")
@Table(columns = {"name", "featured"})
public interface ArtifactConfiguration extends NamedConfiguration
{
    /**
     * Indicates the type of artifact to create for this configuration.  This artifact type must
     * have a single-argument constructor which will accept this configuration.
     *
     * @return the type of artifact to create for this configuration
     */
    Class<? extends Artifact> artifactType();

    /**
     * Indicates if this artifact is marked by the user as "featured", i.e.
     * deserving extra prominence.
     *
     * @return true if this artifact is marked as featured
     */
    boolean isFeatured();
}
