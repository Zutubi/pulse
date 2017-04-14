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

package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    public Maven2PostProcessor(Maven2PostProcessorConfiguration config)
    {
        super(config.asGroup());
    }
}
