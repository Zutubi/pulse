// dependency: ./namespace.js
// dependency: ./Form.js

(function($)
{
    var Observable = kendo.Observable,
        BUTTON_CLICKED = "buttonClicked";

    Zutubi.config.FormWindow = Observable.extend({
        // options:
        //   - title: window title
        //   - formOptions: options passed to form init
        //   - width: window width in pixels (default: 300)
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
            that.form = that.contentEl.kendoZaForm(that.options.formOptions).data("kendoZaForm");

            that.form.bind(BUTTON_CLICKED, function(e)
            {
                that.trigger(BUTTON_CLICKED, e);
            });
        },

        options: {
            width: 300
        },

        events: [
            BUTTON_CLICKED
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
                width: that.options.width,
                modal: true,
                resizable: false,
                title: that.options.title,
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
        }
    });
}(jQuery));
