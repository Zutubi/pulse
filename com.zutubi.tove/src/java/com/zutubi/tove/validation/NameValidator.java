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

package com.zutubi.tove.validation;

import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.util.config.ConfigSupport;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;

/**
 * Validates names used for entities in configuration.
 */
public class NameValidator extends StringFieldValidatorSupport
{
    private static final String STRATEGY_STRICT = VariableResolver.ResolutionStrategy.RESOLVE_STRICT.toString();
    private static final String STRATEGY_NON_STRICT = VariableResolver.ResolutionStrategy.RESOLVE_NON_STRICT.toString();

    public NameValidator()
    {
        super(false);
    }

    public void validateStringField(String s) throws ValidationException
    {
        if (s == null || s.length() == 0)
        {
            addError("required");
            return;
        }

        ConfigSupport config = new ConfigSupport(getValidationContext());
        try
        {
            // If we are using non-strict variable resolution, and there are
            // some unresolved variables in the input, be permissive.
            if (config.getProperty(VariableResolver.ResolutionStrategy.class.getName(), STRATEGY_STRICT).equals(STRATEGY_NON_STRICT) &&
                VariableResolver.containsVariable(s))
            {
                return;
            }
        }
        catch (ResolutionException e)
        {
            // Invalid variable reference, proceed to normal validation.
        }

        if (nameContentIsInvalid(s))
        {
            addError();
        }
    }

    public static boolean nameContentIsInvalid(String s)
    {
        return Character.isWhitespace(s.charAt(0)) || Character.isWhitespace(s.charAt(s.length() - 1)) || s.contains("/") || s.contains("\\") || s.contains("$");
    }
}
