// dependency: ./namespace.js
// dependency: ./FormWindow.js

(function($)
{
    var FormWindow = Zutubi.config.FormWindow,
        BUTTON_CLICKED = "buttonClicked";

    Zutubi.config.CreateDirWindow = FormWindow.extend({
        // options:
        //   create: function(name) callback to invoke when the user enters a directory name
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            FormWindow.fn.init.call(that, {
                title: "create directory",
                formOptions: {
                    structure: {
                        fields: [{
                            name: 'dirname',
                            label: 'directory name',
                            required: true
                        }]
                    },
                    submits: ["create", "cancel"]
                }
            });

            that.bind(BUTTON_CLICKED, function(e)
            {
                var that = this,
                    dirname;

                if (e.value === "create")
                {
                    that.form.clearMessages();
                    dirname = that.form.getValues().dirname;
                    if (!dirname)
                    {
                        that.form.showValidationErrors({dirname: ["directory name is required"]});
                    }
                    else
                    {
                        that.options.create(dirname);
                    }
                }
                else
                {
                    that.close();
                }
            });
        },

        events: [
            BUTTON_CLICKED
        ]
    });
}(jQuery));
