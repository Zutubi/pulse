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

package com.zutubi.pulse.acceptance.forms;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.LoginPage;

public class LoginForm extends SeleniumForm
{
    public LoginForm(SeleniumBrowser browser)
    {
        super(browser, false);
    }

    public String getFormName()
    {
        return "login";
    }

    public String[] getFieldNames()
    {
        return new String[]{LoginPage.FIELD_USERNAME, LoginPage.FIELD_PASSWORD, LoginPage.FIELD_REMEMBERME};
    }

    @Override
    public int[] getFieldTypes()
    {
        return new int[] { TEXTFIELD, TEXTFIELD, CHECKBOX};
    }
}
