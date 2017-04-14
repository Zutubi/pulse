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

package com.zutubi.pulse.core.dependency.ivy;

import com.google.common.base.Predicate;
import com.zutubi.util.StringUtils;

/**
 * A central location for the predicates that are used to determine what characters
 * need encoding in what ivy fields.
 */
public class AllowedCharacters
{
    /**
     * Defines the allowed characters in the organisation, module, artifact and conf fields.
     */
    public static final Predicate<Character> NAMES = new Predicate<Character>()
    {
        public boolean apply(Character character)
        {
            if (StringUtils.isAsciiAlphaNumeric(character))
            {
                return true;
            }
            else
            {
                // A few more likely-used characters
                switch (character)
                {
                    case '-':
                    case '_':
                    case '.':
                        return true;
                }
            }

            return false;
        }
    };
}
