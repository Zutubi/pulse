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

package com.zutubi.pulse.master.velocity;

import com.zutubi.util.junit.ZutubiTestCase;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;

public abstract class VelocityDirectiveTestCase extends ZutubiTestCase
{
    private VelocityEngine velocity;

    public void setUp() throws Exception
    {
        super.setUp();

        // initialise velocity.
        velocity = new VelocityEngine();

        // register the user directives.
        StringBuffer directives = new StringBuffer();
        String sep = "";
        for (String directive : getUserDirectives())
        {
            directives.append(sep);
            directives.append(directive);
            sep = ",";
        }
        velocity.addProperty("userdirective", directives.toString());

        // initiaise the velocity system.
        velocity.init();
    }

    protected String evaluate(String template) throws Exception
    {
        StringWriter writer = new StringWriter();
        velocity.evaluate(new VelocityContext(), writer, "", template);
        return writer.toString();
    }


    public abstract String[] getUserDirectives();

}
