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

package com.zutubi.tove.ui.model;

/**
 * Model for configuration checking.
 */
public class CheckModel
{
    private CompositeModel main;
    private CompositeModel check;

    public CompositeModel getMain()
    {
        return main;
    }

    public void setMain(CompositeModel main)
    {
        this.main = main;
    }

    public CompositeModel getCheck()
    {
        return check;
    }

    public void setCheck(CompositeModel check)
    {
        this.check = check;
    }
}
