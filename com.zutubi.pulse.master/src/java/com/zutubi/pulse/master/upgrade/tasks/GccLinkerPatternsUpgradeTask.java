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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.adt.Pair;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;

/**
 * Task to add new expressions to GCC processor to catch linker errors.
 */
public class GccLinkerPatternsUpgradeTask extends AbstractRegexProcessorPatternsUpgradeTask
{
    private static final String TYPE_GCC_PROCESSOR = "zutubi.gccPostProcessorConfig";

    private static final String PATTERN_UNDEFINED  = ": undefined reference to";
    private static final String PATTERN_ERROR_EXIT = "^collect2: ld returned [1-9][0-9]* exit status";
    
    @Override
    protected String getProcessorType()
    {
        return TYPE_GCC_PROCESSOR;
    }

    @Override
    protected Iterable<? extends Pair<String, String>> getPatterns()
    {
        return asList(
                asPair(CATEGORY_ERROR, PATTERN_UNDEFINED),
                asPair(CATEGORY_ERROR, PATTERN_ERROR_EXIT)
        );
    }
}
