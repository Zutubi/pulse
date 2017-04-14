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

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.Validator;
import com.zutubi.validation.types.TestWallet;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.List;

public class ReflectionValidatorProviderTest extends ZutubiTestCase
{
    private ReflectionValidatorProvider provider;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new ReflectionValidatorProvider();
    }

    protected void tearDown() throws Exception
    {
        provider = null;

        super.tearDown();
    }

    public void testValidateableObject()
    {
        List<Validator> validators = provider.getValidators(TestWallet.class);
        assertEquals(1, validators.size());

        assertTrue(validators.get(0) instanceof ValidateableValidator);
    }

    public void testPlainOldJavaObject()
    {
        List<Validator> validators = provider.getValidators(Object.class);
        assertEquals(0, validators.size());
    }
}
