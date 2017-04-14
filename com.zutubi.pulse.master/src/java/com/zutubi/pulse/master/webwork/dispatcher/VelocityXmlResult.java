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

package com.zutubi.pulse.master.webwork.dispatcher;

import com.opensymphony.webwork.dispatcher.VelocityResult;

/**
 * An extension of the default velocity result that overrides the content type,
 * fixing it to text/xml.
 *
 * This result should be used by velocity templates producing xml.
 */
public class VelocityXmlResult extends VelocityResult
{
    protected String getContentType(String s)
    {
        return "text/xml";
    }
}
