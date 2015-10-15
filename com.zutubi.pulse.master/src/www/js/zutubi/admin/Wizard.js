// dependency: ./namespace.js
// dependency: ./Form.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        PREVIOUS = "previous",
        NEXT = "next",
        CANCEL = "cancel",
        FINISH = "finish",
        POSTING = "posting",
        POSTED = "posted",
        FINISHED = "finished",
        CANCELLED = "cancelled",

    WizardStep = function(config)
    {
        this.key = config.key;
        this.label = config.label;
        this.selectedTypeIndex = 0;

        if (config.kind === "custom")
        {
            this.types = [{
                label: "",
                form: config.form,
                propertyTypes: []
            }];

            this.valuesByType = [config.formDefaults];
        }
        else if(config.kind === "typed")
        {
            this.types = jQuery.map(config.types, function(type) { return {
                label: type.label,
                symbolicName: type.type.symbolicName,
                form: type.type.form,
                propertyTypes: type.type.simpleProperties
            }; });

            this.valuesByType = jQuery.map(config.types, function() { return {}; });
        }
        else
        {
            console.warn("Unknown wizard step type '" + config.kind + "'");
        }
    };

    WizardStep.prototype = {

    };

    Zutubi.admin.Wizard = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        events: [
            POSTING,
            POSTED,
            FINISHED
        ],

        options: {
            name: "ZaWizard",
            template: '<ul class="k-wizard-step-index"></ul>' +
                      '<div class="k-wizard-type-select"><input></div>' +
                      '<div class="k-wizard-form"></div>',
            stepIndexTemplate: '<li>#: label #</li>'
        },

        _create: function()
        {
            var that = this,
                structure = that.options.structure,
                steps = structure.steps,
                i;

            that.steps = jQuery.map(steps, function(step)
            {
                return new WizardStep(step);
            });

            that.template = kendo.template(that.options.template);
            that.stepIndexTemplate = kendo.template(that.options.stepIndexTemplate);

            that.element.html(that.template({}));
            that.stepIndexElement = that.element.find(".k-wizard-step-index");
            if (steps.length === 1)
            {
                that.stepIndexElement.hide();
            }
            else
            {
                for (i = 0; i < steps.length; i++)
                {
                    that.stepIndexElement.append(that.stepIndexTemplate(steps[i]));
                }
            }

            that.typeSelectWrapper = that.element.find(".k-wizard-type-select");
            that.typeSelectDropDown = that.typeSelectWrapper.children("input").kendoDropDownList({
                change: jQuery.proxy(that._typeSelected, that)
            }).data("kendoDropDownList");

            that.formWrapper = that.element.find(".k-wizard-form");

            that._showStepAtIndex(0);
        },

        destroy: function()
        {
            var that = this;

            Widget.fn.destroy.call(that);
            kendo.destroy(that.element);

            that.element = null;
        },

        getValue: function()
        {
            var that = this,
                result = {};

            jQuery.each(that.steps, function(index, step)
            {
                result[step.key] = {
                    properties: step.valuesByType[step.selectedTypeIndex],
                    type: step.types[step.selectedTypeIndex]
                };
            });

            return result;
        },

        showValidationErrors: function(details)
        {
            var that = this,
                stepIndex = -1;

            jQuery.each(that.steps, function(index, step)
            {
                if (step.key === details.key)
                {
                    stepIndex = index;
                    return false;
                }
            });

            if (stepIndex === -1)
            {
                Zutubi.admin.reportError("Unrecognisable validation failure in wizard");
            }
            else
            {
                that._showStepAtIndex(stepIndex);
                that.form.showValidationErrors(details);
            }
        },

        _stashValuesAndCleanupForm: function()
        {
            var form = this.form,
                step;

            if (form)
            {
                step = this.steps[this.currentStepIndex];
                step.valuesByType[step.selectedTypeIndex] = form.getValues();

                form.element.empty();
                form.destroy();
                this.form = null;
            }
        },

        _showStepAtIndex: function(index)
        {
            var that = this,
                step = that.steps[index],
                items = that.stepIndexElement.children("li");

            this._stashValuesAndCleanupForm();

            items.removeClass("active");
            items.eq(index).addClass("active");

            this.currentStepIndex = index;
            this._updateTypeSelect(step.types);
            this._showTypeAtIndex(step.selectedTypeIndex);
        },

        _updateTypeSelect: function(types)
        {
            var labels,
                dropDown = this.typeSelectDropDown,
                width;

            if (types.length > 1)
            {
                labels = jQuery.map(types, function(type) { return type.label; });
                dropDown.setDataSource(labels);
                this.typeSelectWrapper.show();
            }
            else
            {
                this.typeSelectWrapper.hide();
            }
        },

        _typeSelected: function()
        {
            var that = this,
                step = that.steps[that.currentStepIndex],
                typeLabel = that.typeSelectDropDown.value();

            jQuery.each(step.types, function(i, type)
            {
                if (type.label === typeLabel)
                {
                    that._showTypeAtIndex(i);
                    return false;
                }
            });
        },

        _showTypeAtIndex: function(index)
        {
            var that = this,
                stepIndex = that.currentStepIndex,
                step = that.steps[stepIndex],
                type = step.types[index],
                lastIndex = that.steps.length - 1,
                submits = [];

            this._stashValuesAndCleanupForm();

            step.selectedTypeIndex = index;

            if (stepIndex !== 0)
            {
                submits.push(PREVIOUS);
            }

            if (stepIndex !== lastIndex)
            {
                submits.push(NEXT);
            }
            else
            {
                submits.push(FINISH);
            }

            submits.push(CANCEL);

            that.form = that.formWrapper.kendoZaForm({
                structure: that._filterFields(type.form),
                values: step.valuesByType[index],
                submits: submits
            }).data("kendoZaForm");

            that.form.bind("submit", jQuery.proxy(that._formSubmitted, that));
        },

        _filterFields: function(formStructure)
        {
            return jQuery.extend({}, formStructure, {
                fields: jQuery.grep(formStructure.fields, function(field)
                {
                    return !field.parameters.hasOwnProperty("wizardIgnore");
                })
            });
        },

        _formSubmitted: function(e)
        {
            var submit = e.value,
                step = this.steps[this.currentStepIndex];

            if (submit === CANCEL)
            {
                this.trigger(CANCELLED);
            }
            else
            {
                step.valuesByType[step.selectedTypeIndex] = this.form.getValues();

                if (submit === PREVIOUS)
                {
                    this._back();
                }
                else
                {
                    this._forward();
                }
            }
        },

        _back: function()
        {
            if (this.currentStepIndex > 0)
            {
                this._showStepAtIndex(this.currentStepIndex - 1);
            }
        },

        _forward: function()
        {
            // FIXME kendo validate before stepping forward.
            if (this.currentStepIndex === this.steps.length - 1)
            {
                this._finish();
            }
            else
            {
                this._showStepAtIndex(this.currentStepIndex + 1);
            }
        },

        _finish: function()
        {
            var that = this,
                wizardData = that.getValue();

            jQuery.each(wizardData, function(property, data)
            {
                data.kind = "composite";
                Zutubi.admin.coerceProperties(data.properties, data.type.propertyTypes);
                if (data.type.symbolicName)
                {
                    data.type = {symbolicName: data.type.symbolicName};
                }
                else
                {
                    delete data.type;
                }
            });

            that.trigger(POSTING);

            Zutubi.admin.ajax({
                type: "POST",
                url: "/api/wizard/" + that.options.path,
                data: wizardData,
                success: function (data)
                {
                    that.trigger(POSTED);
                    that.trigger(FINISHED, {delta: data});
                },
                error: function (jqXHR)
                {
                    var details;

                    that.trigger(POSTED);

                    if (jqXHR.status === 422)
                    {
                        try
                        {
                            details = JSON.parse(jqXHR.responseText);
                            if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                            {
                                that.showValidationErrors(details);
                                return;
                            }
                        }
                        catch(e)
                        {
                            // Do nothing.
                            console.dir(e);
                        }
                    }

                    Zutubi.admin.reportError("Could not finish wizard: " + Zutubi.admin.ajaxError(jqXHR));
                }
            });
        }
    });

    ui.plugin(Zutubi.admin.Wizard);
}(jQuery));
