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

package com.zutubi.pulse.core.postprocessors.clang;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned configuration for a regex post-processor for clang (and related
 * tool) output.  Captures error and warning messages.
 */
@SymbolicName("zutubi.clangPostProcessorConfig")
public class ClangPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public ClangPostProcessorConfiguration()
    {
        addErrorRegexes(
                "^.+:[0-9]+:([0-9]+:)?( fatal)? error:",
                "^\\w+:( fatal)? error:",
                "^Undefined symbols for architecture",
                "^ld: symbol\\(s\\) not found for architecture"
        );
        addWarningRegexes(
                "^.+:[0-9]+:([0-9]+:)? warning:",
                "^\\w+: warning:"
        );

        setFailOnError(false);
        setLeadingContext(0);
        setTrailingContext(3);
    }
}
