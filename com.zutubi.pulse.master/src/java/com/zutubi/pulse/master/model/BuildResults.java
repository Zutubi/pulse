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

package com.zutubi.pulse.master.model;

import com.google.common.base.Function;

/**
 * Static utilities for working with builds.
 */
public final class BuildResults
{
    // Do not instantiate
    private BuildResults() {}

    /**
     * @return a function the converts a BuildResult to its number
     */
    public static Function<BuildResult, Long> toNumber()
    {
        return ToNumberFunction.INSTANCE;
    }
    
    private enum ToNumberFunction implements Function<BuildResult, Long>
    {
        INSTANCE;

        public Long apply(BuildResult input)
        {
            return input.getNumber();
        }


        @Override
        public String toString()
        {
            return "toNumber";
        }
    }
}
