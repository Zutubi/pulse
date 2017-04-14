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

package com.zutubi.validation.types;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * <class-comment/>
 */
public class TestWallet implements Validateable
{
    private Object cc;
    private int money;

    public void validate(ValidationContext context)
    {
        // ensure that we have a credit card and some money, else we want another wallet.
        if (cc == null)
        {
            context.addFieldError("cc", "cc.required");
        }

        if (money < 100)
        {
            context.addFieldError("money", "money.min");
        }
    }
}
