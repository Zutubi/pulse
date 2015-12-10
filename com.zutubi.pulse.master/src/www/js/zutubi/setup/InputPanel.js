// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        SUBMIT = "submit";

    Zutubi.setup.InputPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                model = options.model,
                el;

            that.options = options;

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#: id #" class="k-input-panel">' +
                    '<h1>#: label #</h1>' +
                    '<p id="intro-docs" style="display:none"></p>' +
                    '<div id="main-form"></div>' +
                    '<div style="display:none" id="check-wrapper" class="k-check-wrapper">' +
                        '<h1>check configuration</h1>' +
                        '<p>click <em>check</em> below to test your configuration</p>' +
                        '<div id="check-form">' +
                        '</div>' +
                    '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: "input",
                        label: model.label
                    }
                });

            that.view.render($(options.containerSelector));

            that.form = $("#main-form").kendoZaForm({
                symbolicName: model.type.symbolicName,
                structure: model.type.form,
                values: model.type.simplePropertyDefaults || {},
                submits: options.submits || ["next"]
            }).data("kendoZaForm");

            that.form.bind("buttonClicked", jQuery.proxy(that._submitClicked, that));

            if (model.type.checkType)
            {
                $("#check-wrapper").show();

                that.checkForm = $("#check-form").kendoZaForm({
                    formName: "check",
                    symbolicName: model.type.checkType.symbolicName,
                    structure: model.type.checkType.form,
                    values: [],
                    submits: ["check"]
                }).data("kendoZaForm");

                that.checkForm.bind("buttonClicked", jQuery.proxy(that._checkClicked, that));
            }

            if (model.type.docs)
            {
                if (model.type.docs.brief)
                {
                    el = $("#intro-docs");
                    el.html(model.type.docs.brief);
                    el.show();
                }

                that.htmlDocs = model.type.docs.verbose;
            }
        },

        events: [
            SUBMIT
        ],

        destroy: function()
        {
            this.view.destroy();
        },

        showValidationErrors: function(errors)
        {
            this.form.showValidationErrors(errors);
        },

        _submitClicked: function(e)
        {
            var values = this.form.getValues();

            Zutubi.config.coerceProperties(values, this.options.model.type.simpleProperties);

            this.trigger(SUBMIT, {
                status: this.options.status,
                submit: e.value,
                values: values
            });
        },

        _checkClicked: function()
        {
            Zutubi.config.checkConfig(null, this.options.model.type, this.form, this.checkForm);
        }
    });
}(jQuery));
