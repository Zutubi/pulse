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

package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-cannoed regex post-processor configuration that looks for error
 * messages from Boost Jam (bjam).
 */
@SymbolicName("zutubi.bjamPostProcessorConfig")
public class BJamPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public BJamPostProcessorConfiguration()
    {
        addErrorRegexes("^error:",
                       "^rule [a-zA-Z0-9_-]+ unknown",
                       "^\\.\\.\\.failed",
                       "^\\*\\*\\* argument error",
                       "^don't know how to make",
                       "^syntax error");

        addWarningRegexes("^warning:");

        setFailOnError(false);
        setLeadingContext(1);
        setTrailingContext(3);
    }

    public BJamPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
