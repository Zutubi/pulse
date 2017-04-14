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

package com.zutubi.pulse.servercore.hessian;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;

import java.io.IOException;

/**
 * Serialises Java 1.5 Enumerations
 *
 * @author kp
 */
public class EnumSerialiser extends AbstractSerializer
{
    public void writeObject(Object obj, AbstractHessianOutput out) throws IOException
    {
        Class clazz = obj.getClass();

        out.writeMapBegin(clazz.getName());
        out.writeString(clazz.getName());
        out.writeString(obj.toString());
        out.writeMapEnd();
    }
}

