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

package com.zutubi.pulse.master.restore;

import java.io.File;

/**
 *
 *
 */
public class Archive
{
    /**
     * The base (expanded directory) for this imported archive.
     */
    private File base;

    /**
     * If specified, this file is a reference to the original archive file, the file that was imported.
     *
     * @see ArchiveFactory#importArchive(java.io.File) 
     */
    private File original;

    /**
     * This archives manifest.
     */
    private ArchiveManifest manifest;

    public Archive(File original, File base, ArchiveManifest manifest)
    {
        this.base = base;
        this.manifest = manifest;
        this.original = original;
    }

    public Archive(File base, ArchiveManifest manifest)
    {
        this(null, base, manifest);
    }

    public ArchiveManifest getManifest()
    {
        return manifest;
    }

    public File getBase()
    {
        return base;
    }

    public String getVersion()
    {
        return manifest.getVersion();
    }

    public String getCreated()
    {
        return manifest.getCreated();
    }

    public File getOriginal()
    {
        return original;
    }
}
