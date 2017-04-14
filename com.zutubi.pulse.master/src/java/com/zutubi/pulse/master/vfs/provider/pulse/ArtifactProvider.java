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

package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.core.model.StoredArtifact;
import org.apache.commons.vfs.FileSystemException;

/**
 * A provider interface that indicates the current node represents a StoredArtifact instance.
 * 
 * @see com.zutubi.pulse.core.model.StoredArtifact
 */
public interface ArtifactProvider
{
    StoredArtifact getArtifact() throws FileSystemException;

    long getArtifactId() throws FileSystemException;
}
