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

package com.zutubi.pulse.core.test.api;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.regex.Pattern;

/**
 * A Hamcrest matcher that tests if a string matches a Java regular expression.
 */
public class MatchesRegex extends TypeSafeMatcher<String>
{
    private Pattern pattern;

    /**
     * Create a matcher that will match strings against the given regular
     * expression.
     *
     * @see java.util.regex.Pattern
     *
     * @param expression regular expression, in java.util.regex format
     */
    public MatchesRegex(String expression)
    {
        pattern = Pattern.compile(expression);
    }

    public boolean matchesSafely(String s)
    {
        return pattern.matcher(s).matches();
    }

    public void describeTo(Description description)
    {
        description.appendText("string matching regex '" + pattern.toString() + "'");
    }
}
