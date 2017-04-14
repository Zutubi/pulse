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

import com.google.common.base.Charsets;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.WebWorkResultSupport;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import flexjson.JSONSerializer;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A Webwork result that outputs a JSON string, using Flexjson for trivial
 * serialisation from a Java object.
 */
public class FlexJsonResult extends WebWorkResultSupport
{
    private boolean deep;

    public void setDeep(boolean deep)
    {
        this.deep = deep;
    }

    protected void doExecute(String finalLocation, ActionInvocation ai) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        OgnlValueStack stack = ai.getStack();

        JSONSerializer serializer = new JSONSerializer();
        serializer.exclude("*.class");
        Object o = stack.findValue(finalLocation);
        String json = deep ? serializer.deepSerialize(o) : serializer.serialize(o);
        Writer writer = new OutputStreamWriter(response.getOutputStream(), Charsets.UTF_8);
        writer.write(json);
        response.setContentType("application/json"); // opera does not like this...
        writer.flush();
    }

}
