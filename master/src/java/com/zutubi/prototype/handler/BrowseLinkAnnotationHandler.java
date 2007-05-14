package com.zutubi.prototype.handler;

import com.zutubi.config.annotations.BrowseLink;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.config.annotations.BrowseLink} annotation.
 */
public class BrowseLinkAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        BrowseLink browseLink = (BrowseLink) annotation;
        fieldDescriptor.addParameter("browseLink", browseLink.linkKey());
        fieldDescriptor.addScript(browseLink.template());
    }
}
