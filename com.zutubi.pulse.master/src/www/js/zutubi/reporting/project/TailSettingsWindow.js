// dependency: ./namespace.js
// dependency: zutubi/config/package.js

(function($)
{
    var Observable = kendo.Observable,
        SAVED = "saved";

    Zutubi.reporting.project.TailSettingsWindow = Observable.extend({
        // options:
        //   - username: for the logged in user
        //   - tailLines: initial setting for max tail lines
        //   - tailRefreshInterval: initial setting for tail refresh interval
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div class="k-form-window">' +
                    '<div class="k-form-window-content">' +
                    '</div>',
                '</div>',
                {wrap: false});

            that.element = that.view.render("body");

            that.contentEl = that.element.find(".k-form-window-content");
            that.form = that.contentEl.kendoZaForm({
                structure: {
                    fields: [{
                        name: 'tailLines',
                        label: 'maximum lines to show',
                        value: options.tailLines
                    }, {
                        name: 'tailRefreshInterval',
                        label: 'refresh interval (seconds)',
                        value: options.tailRefreshInterval

                    }]
                },
                values: {
                    tailLines: options.tailLines,
                    tailRefreshInterval: options.tailRefreshInterval
                },
                submits: ["apply", "cancel"]
            }).data("kendoZaForm");

            that.form.bind("buttonClicked", function(e)
            {
                if (e.value === "apply")
                {
                    that._applySettings();
                }
                else
                {
                    that.close();
                }
            });
        },

        options: {
            tailLines: 30,
            tailRefreshInterval: 60
        },

        events: [
            SAVED
        ],

        mask: function(mask)
        {
            kendo.ui.progress(this.element.closest(".k-widget"), mask);
        },

        show: function()
        {
            var that = this;

            that.completed = false;

            that.window = $(that.element).kendoWindow({
                width: 300,
                modal: true,
                resizable: false,
                title: "tail view settings",
                close: function()
                {
                    if (!that.completed)
                    {
                        if (that.options.cancel)
                        {
                            that.options.cancel();
                        }
                    }
                },
                deactivate: function()
                {
                    that.window.destroy();
                }
            }).data("kendoWindow");

            that.window.open();
            that.window.center();
        },

        close: function()
        {
            this.window.close();
        },

        _applySettings: function()
        {
            var that = this,
                properties = that.form.getValues();

            Zutubi.config.coerceProperties(properties, [{
                name: "tailLines",
                shortType: "int"
            }, {
                name: "tailRefreshInterval",
                shortType: "int"
            }]);

            that.mask(true);

            Zutubi.config.saveConfig({
                path: "users/" + that.options.username + "/preferences",
                properties: properties,
                success: jQuery.proxy(that._saved, that),
                invalid: jQuery.proxy(that._invalid, that)
            });
        },

        _saved: function()
        {
            this.mask(false);
            this.close();
            this.trigger(SAVED);
        },

        _invalid: function(errors)
        {
            this.mask(false);
            this.form.showValidationErrors(errors);
        }
    });
}(jQuery));
