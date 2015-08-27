package com.zutubi.tove.annotations;

/**
 * Defines constants naming annotation handler types.
 */
public interface DefaultAnnotationHandlers
{
    String HANDLER_PACKAGE    = "com.zutubi.pulse.master.tove.handler";

    String CONTROLLING_SELECT = HANDLER_PACKAGE + ".ControllingSelectAnnotationHandler";
    String FIELD_ACTION       = HANDLER_PACKAGE + ".FieldActionAnnotationHandler";
    String FIELD_SCRIPT       = HANDLER_PACKAGE + ".FieldScriptAnnotationHandler";
    String FIELD              = HANDLER_PACKAGE + ".FieldAnnotationHandler";
    String FORM               = HANDLER_PACKAGE + ".FormAnnotationHandler";
    String ITEM_PICKER        = HANDLER_PACKAGE + ".ItemPickerAnnotationHandler";
    String REFERENCE          = HANDLER_PACKAGE + ".ReferenceAnnotationHandler";
    String SELECT             = HANDLER_PACKAGE + ".SelectAnnotationHandler";
    String WIZARD_IGNORE      = HANDLER_PACKAGE + ".WizardIgnoreAnnotationHandler";
}
