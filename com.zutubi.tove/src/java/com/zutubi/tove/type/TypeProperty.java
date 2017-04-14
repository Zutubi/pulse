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

package com.zutubi.tove.type;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public abstract class TypeProperty
{
    /**
     * Annotations associated with this property.  This includes annotations
     * directly on the property, on the property in supertypes, and meta-
     * annotations recursively.
     */
    private List<Annotation> annotations = new LinkedList<Annotation>();

    /**
     * The name of this property.
     */
    private String name;

    /**
     * The type of this property.
     */
    private Type type;

    public TypeProperty()
    {
    }

    public TypeProperty(String name)
    {
        this.name = name;
    }

    public TypeProperty(String name, Type type)
    {
        this.name = name;
        this.type = type;
    }

    public List<Annotation> getAnnotations()
    {
        return annotations;
    }

    protected void setAnnotations(List<Annotation> annotations)
    {
        this.annotations = annotations;
    }

    protected void addAnnotation(Annotation annotation)
    {
        this.annotations.add(annotation);
    }

    public <T extends Annotation> T getAnnotation(final Class<T> type)
    {
        return type.cast(find(annotations, new Predicate<Annotation>()
        {
            public boolean apply(Annotation annotation)
            {
                return annotation.annotationType() == type;
            }
        }, null));
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }


    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Class getClazz()
    {
        return type.getClazz();
    }

    public abstract Object getValue(Object instance) throws Exception;
    
    public abstract void setValue(Object instance, Object value) throws TypeException;

    public abstract boolean isReadable();
    
    public abstract boolean isWritable();
}
