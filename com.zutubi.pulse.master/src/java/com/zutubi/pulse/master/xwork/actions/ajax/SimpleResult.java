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

package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * A simple result object for generating a JSON tuple with a success flag and
 * optional detail message.
 */
public class SimpleResult
{
    private boolean success;
    private boolean redirect;
    private String detail;

    public SimpleResult(boolean success, String detail)
    {
        this.success = success;
        this.detail = detail;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public boolean isRedirect()
    {
        return redirect;
    }

    public void setRedirect(boolean redirect)
    {
        this.redirect = redirect;
    }

    public String getDetail()
    {
        return detail;
    }
}
