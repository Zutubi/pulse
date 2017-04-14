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

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationAwareSupport;
import com.zutubi.validation.i18n.InMemoryTextProvider;

public abstract class FieldValidatorTestCase extends ZutubiTestCase
{
    protected ValidationAwareSupport validationAware;
    protected InMemoryTextProvider textProvider;
    protected FieldValidator validator;

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        validationAware = new ValidationAwareSupport();
        textProvider = new InMemoryTextProvider();

        validator = createValidator();
        validator.setFieldName("field");
        validator.setValidationContext(new DelegatingValidationContext(validationAware, textProvider));
    }

    protected abstract FieldValidator createValidator();

    protected class FieldProvider
    {
        private Object value;

        public FieldProvider(Object value)
        {
            this.value = value;
        }

        public Object getField()
        {
            return value;
        }
    }
}