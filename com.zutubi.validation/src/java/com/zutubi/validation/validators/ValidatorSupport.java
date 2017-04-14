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

import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ShortCircuitableValidator;

/**
 * The validator support class implements the boiler plate methods from the Validator and
 * ShortCircuitValidator interfaces, allowing validator implementations to focus on the
 * details of the validation.
 */
public abstract class ValidatorSupport implements Validator, ShortCircuitableValidator
{
    protected ValidationContext validationContext;

    private boolean shortCircuit = true;

    public ValidationContext getValidationContext()
    {
        return validationContext;
    }

    public void setValidationContext(ValidationContext validationContext)
    {
        this.validationContext = validationContext;
    }

    public void setShortCircuit(boolean b)
    {
        shortCircuit = b;
    }

    public boolean isShortCircuit()
    {
        return shortCircuit;
    }

}
