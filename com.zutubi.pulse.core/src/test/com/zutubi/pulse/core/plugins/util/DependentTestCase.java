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

package com.zutubi.pulse.core.plugins.util;

import com.zutubi.pulse.core.test.api.PulseTestCase;

/**
 * Helper base class for testing dependent sorting and related functionality.
 */
public abstract class DependentTestCase extends PulseTestCase
{
    protected TransitiveFunction<String> fn;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        FixedDependentsFunction fixedFn = new FixedDependentsFunction();
        fixedFn.add("no-deps");
        fixedFn.add("one-dep", "one-dep-1");
        fixedFn.add("two-deps", "two-deps-1", "two-deps-2");

        // top > middle > bottom
        fixedFn.add("top", "middle");
        fixedFn.add("middle", "bottom");

        // root
        //   > left
        //       > left-left
        //       > left-right
        //   > right
        //       > right-left
        //       > right-right
        fixedFn.add("root", "left", "right");
        fixedFn.add("left", "left-left", "left-right");
        fixedFn.add("right", "right-left", "right-right");

        // depends on itself
        fixedFn.add("self-dep", "self-dep");

        // non-trivial cycle
        fixedFn.add("home", "first");
        fixedFn.add("first", "second");
        fixedFn.add("second", "third");
        fixedFn.add("third", "home");

        fn = new TransitiveFunction<String>(fixedFn);
    }
}
