package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.config.annotations.FieldAction;
import com.zutubi.config.annotations.Handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@FieldAction(filterClass = "com.zutubi.pulse.master.tove.config.project.ScmBrowsablePredicate", template = "actions/browse-scm-file")
@Handler(className = "com.zutubi.pulse.master.tove.config.project.BrowseScmFileAnnotationHandler")
public @interface BrowseScmFileAction
{
    String baseDirField() default "work";
}
