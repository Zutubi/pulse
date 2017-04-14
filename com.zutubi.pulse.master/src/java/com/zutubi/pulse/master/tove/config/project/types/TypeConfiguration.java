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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Defines what the pulse file looks like for a project.  It could be built
 * from a restricted template (e.g. an ant project) or could be had-written
 * by the user (e.g. a custom project) or anything else that can produce a
 * valid pulse file.
 */
@SymbolicName("zutubi.typeConfig")
public abstract class TypeConfiguration extends AbstractConfiguration
{
    @Transient
    public abstract PulseFileProvider getPulseFile() throws Exception;
}
