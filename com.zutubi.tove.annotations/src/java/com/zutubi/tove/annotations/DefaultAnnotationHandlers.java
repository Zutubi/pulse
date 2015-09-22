package com.zutubi.tove.annotations;

/**
 * Defines constants naming annotation handler types.
 */
public interface DefaultAnnotationHandlers
{
    String HANDLER_PACKAGE    = "com.zutubi.pulse.master.tove.handler";

    String FIELD_ACTION       = HANDLER_PACKAGE + ".FieldActionAnnotationHandler";
    String FIELD_SCRIPT       = HANDLER_PACKAGE + ".FieldScriptAnnotationHandler";
    String FIELD              = HANDLER_PACKAGE + ".FieldAnnotationHandler";
    String FORM               = HANDLER_PACKAGE + ".FormAnnotationHandler";
    String OPTION             = HANDLER_PACKAGE + ".OptionAnnotationHandler";
    String REFERENCE          = HANDLER_PACKAGE + ".ReferenceAnnotationHandler";
    String WIZARD_IGNORE      = HANDLER_PACKAGE + ".WizardIgnoreAnnotationHandler";
}
