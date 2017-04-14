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

package com.zutubi.validation.providers;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a validator for classes that extend Validateable.
 */
public class ReflectionValidatorProvider implements ValidatorProvider
{
    private static final List<Validator> VALIDATEABLE_VALIDATOR = Collections.<Validator>singletonList(new ValidateableValidator());

    private Map<Class, Boolean> cache = Collections.synchronizedMap(new HashMap<Class, Boolean>());

    @Override
    public List<Validator> getValidators(Class clazz)
    {
        Boolean isValidateable = cache.get(clazz);
        if(isValidateable == null)
        {
            isValidateable = Validateable.class.isAssignableFrom(clazz);
            cache.put(clazz, isValidateable);
        }

        if (isValidateable)
        {
            return VALIDATEABLE_VALIDATOR;
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
