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
 * An implementation of the ArchiveableComponent that does nothing.
 */
public class NoopArchiveableComponent extends AbstractArchiveableComponent
{
    public String getName()
    {
        return "noop";
    }

    public String getDescription()
    {
        return "noop";
    }

    public void backup(File archive) throws ArchiveException
    {
        // noop.
    }

    public void restore(File archive) throws ArchiveException
    {
        // noop.
    }
}