/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.hessian;

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

