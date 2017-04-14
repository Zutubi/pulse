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

package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned regular expression post-processor configuration for NAnt output.
 * Attempts to capture features from NAnt itself (e.g. "BUILD FAILED") and from
 * commonly-used tasks (e.g. csc).
 */
@SymbolicName("zutubi.nantPostProcessorConfig")
public class NAntPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public NAntPostProcessorConfiguration()
    {
        addErrorRegexes("^BUILD FAILED", "^Error loading buildfile.", ": error [A-Z0-9]+:");
        addWarningRegexes(": warning [A-Z0-9]+:");

        setLeadingContext(3);
        setTrailingContext(3);
    }

    public NAntPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
