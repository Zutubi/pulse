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

import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;
import org.apache.ivy.core.IvyPatternHelper;

import java.util.Collections;

/**
 * A validator that checks the format of ivy patterns.
 */
public class IvyPatternValidator extends StringFieldValidatorSupport
{
    public IvyPatternValidator()
    {
        super(false);
    }

    protected void validateStringField(String value) throws ValidationException
    {
        try
        {
            if (VariableResolver.containsVariable(value))
            {
                // do not validate strings that contain references.
                return;
            }
        }
        catch (ResolutionException e)
        {
            // noop.
        }

        try
        {
            IvyPatternHelper.substituteTokens(value, Collections.emptyMap());
        }
        catch (Exception e)
        {
            addErrorMessage(e.getMessage());
        }
    }
}
