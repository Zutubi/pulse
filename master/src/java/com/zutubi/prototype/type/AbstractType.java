package com.zutubi.prototype.type;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 *
 *
 */
public abstract class AbstractType implements Type
{
    private List<Annotation> annotations = new LinkedList<Annotation>();

    public void setAnnotations(List<Annotation> annotations)
    {
        this.annotations = new LinkedList<Annotation>(annotations);
    }

    public List<Annotation> getAnnotations()
    {
        return Collections.unmodifiableList(annotations);
    }

    public void addAnnotation(Annotation annotation)
    {
        this.annotations.add(annotation);
    }
}
