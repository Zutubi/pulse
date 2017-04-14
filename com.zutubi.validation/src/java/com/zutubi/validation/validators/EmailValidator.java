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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Checks that a field has a valid email address, according to the default validation
 * of the javax.mail.internet.InternetAddress object.
 */
public class EmailValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String str) throws ValidationException
    {
        try
        {
            new InternetAddress(str);
        }
        catch (AddressException e)
        {
            addError();
        }
    }
}
