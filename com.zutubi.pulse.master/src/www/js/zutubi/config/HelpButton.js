// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Button = Zutubi.config.Button;

    Zutubi.config.HelpButton = Button.extend({
        init: function(element, options)
        {
            $(element).html('<span class="k-sprite"></span> <span class="k-help-button-label">show full help</span>');
            this.form = options.form;

            Button.fn.init.call(this, element, options);

            this.label = this.element.find(".k-help-button-label");
            this.bind("click", jQuery.proxy(this._clicked, this));
            this._syncToForm();
        },

        options: {
            name: "ZaHelpButton",
            spriteCssClass: "fa fa-question-circle"
        },

        _syncToForm: function()
        {
            if (this.form)
            {
                this.label.html(this.form.isHelpShown() ? "hide full help" : "show full help");
                this.enable(this.form.hasHelp());
            }
            else
            {
                this.enable(false);
            }
        },

        _clicked: function()
        {
            this.form.toggleHelp();
            this._syncToForm();
        },

        setForm: function(form)
        {
            this.form = form;
            this._syncToForm();
        }
    });

    ui.plugin(Zutubi.config.HelpButton);
}(jQuery));
