// dependency: ./namespace.js
// dependency: ./Table.js

(function($)
{
    var Observable = kendo.Observable,
        CHECK = "check",
        SAVE = "save";

    Zutubi.admin.CompositePanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                composite = options.composite;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-composite-panel">' +
                    '<h1>#: label #</h1>' +
                    '<div id="#: id #-form"></div>' +
                    '<div style="display:none" id="#: id #-checkwrapper">' +
                        '<h1>check</h1>' +
                        '<div id="#: id #-checkform">' +
                        '</div>' +
                    '</div>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: "composite",
                        label: composite.label
                    }
                });

            that.view.render($(options.containerSelector));

            that.form = $("#composite-form").kendoZaForm({
                path: options.path,
                structure: composite.type.form,
                values: composite.properties
            }).data("kendoZaForm");

            that.form.bind("submit", function(e)
            {
                that.trigger(SAVE, {values: that.form.getValues()});
            });

            // FIXME kendo if the composite is not writable, don't show this
            if (composite.type.checkType)
            {
                $("#composite-checkwrapper").show();

                that.checkForm = $("#composite-checkform").kendoZaForm({
                    formName: "check",
                    structure: composite.type.checkType.form,
                    values: [],
                    submits: ["check"]
                }).data("kendoZaForm");

                that.checkForm.bind("submit", function(e)
                {
                    that.trigger(CHECK, {
                        values: that.form.getValues(),
                        checkValues: that.checkForm.getValues()
                    });
                });
            }
        },

        events: [
            CHECK,
            SAVE
        ],

        destroy: function()
        {
            // FIXME moar destruction?
            this.view.destroy();
        },

        showValidationErrors: function(details)
        {
            this.form.showValidationErrors(details);
        },

        showCheckValidationErrors: function(details)
        {
            this.checkForm.showValidationErrors(details);
        }
    });
}(jQuery));
