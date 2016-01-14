// dependency: ./namespace.js
// dependency: zutubi/config/package.js

(function($)
{
    var FormWindow = Zutubi.config.FormWindow,
        BUTTON_CLICKED = "buttonClicked",
        SAVED = "saved";

    Zutubi.reporting.project.TailSettingsWindow = FormWindow.extend({
        // options:
        //   - username: for the logged in user
        //   - tailLines: initial setting for max tail lines
        //   - tailRefreshInterval: initial setting for tail refresh interval
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            FormWindow.fn.init.call(that, {
                title: "tail view settings",
                formOptions: {
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
                }
            });

            that.bind(BUTTON_CLICKED, function(e)
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
            BUTTON_CLICKED,
            SAVED
        ],

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
