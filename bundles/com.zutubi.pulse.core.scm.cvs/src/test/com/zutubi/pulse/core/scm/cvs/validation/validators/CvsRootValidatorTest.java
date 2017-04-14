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

package com.zutubi.pulse.core.scm.cvs.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.validators.FieldValidatorTestCase;
import junit.framework.Assert;

public class CvsRootValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new CvsRootValidator();
    }

    public void testEmptyString() throws Exception
    {
        validator.validate(new FieldProvider(""));
        Assert.assertFalse(validationAware.hasErrors());
    }

    public void testNull() throws Exception
    {
        validator.validate(new FieldProvider(null));
        Assert.assertFalse(validationAware.hasErrors());
    }

    public void testLocalRoot() throws Exception
    {
        validator.validate(new FieldProvider("/local"));
        Assert.assertFalse(validationAware.hasErrors());
    }

    public void testPSever() throws Exception
    {
        validator.validate(new FieldProvider(":pserver:blah@somehost.com:/path/to/root"));
        Assert.assertFalse(validationAware.hasErrors());
    }
}