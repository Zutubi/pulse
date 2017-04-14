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

package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.lang.reflect.Field;

/**
 * Validates that a string field can be compiled as a regular expression.
 */
public class PatternValidator extends StringFieldValidatorSupport
{
    private static final int UNDEFINED = 0;

    /**
     * The group count refers to explicitly defined groups.
     */
    private int groupCount = UNDEFINED;

    public void setGroupCount(int groupCount)
    {
        this.groupCount = groupCount;
    }

    public void validateStringField(String str) throws ValidationException
    {
        Pattern p = null;
        try
        {
            p = Pattern.compile(str);
        }
        catch (PatternSyntaxException e)
        {
            addErrorMessage(e.getMessage());
        }

        // attempt to verify the group count.
        if (groupCount != UNDEFINED)
        {
            try
            {
                Field f = p.getClass().getDeclaredField("capturingGroupCount");
                f.setAccessible(true);
                int capturingGroupCount = (Integer)f.get(p);
                // group count is incremented by 1 since capturing group count always starts
                // with 1, and then adds 1 for each explicit group that is encountered.  
                if (capturingGroupCount != groupCount + 1)
                {
                    addErrorMessage("Expected " + groupCount + " regex groups, but instead found " + (capturingGroupCount - 1));
                }
            }
            catch (Exception e)
            {
                //
            }
        }
    }
}
