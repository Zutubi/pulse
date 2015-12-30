// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.core.PromptWindow = Observable.extend({
        // options: {
        //    title: window title (default: "")
        //    messageHTML: message to prompt the user with, may contain any HTML (default: "Continue?")
        //    buttons: array of button definitions (default ok:true/cancel:false), each contains:
        //      - label: button text
        //      - spriteCssClass: classes for icon
        //      - value: value to pass to select callback when the button is clicked
        //    closeValue: value to pass to select callback when the user closes the window (default: false)
        //    select: function(value) called when user selects a button with the button value
        //    width: width of the window (default: 400)
        // }
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div style="display: none">' +
                    '<div class="k-dialog-window-content">#= message #</div>' +
                    '<div class="k-dialog-window-actions"></div>' +
                '</div>',
                {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        message: options.messageHTML
                    }
                });

            that.element = that.view.render("body");
            that.buttonTemplate = kendo.template(that.options.buttonTemplate);
            that._renderButtons();
        },

        options: {
            title: "",
            message: "Continue?",
            buttons: [{
                label: "ok",
                spriteCssClass: "fa fa-check-circle",
                value: true
            }, {
                label: "cancel",
                spriteCssClass: "fa fa-times-circle",
                value: false
            }],
            buttonTemplate: '<button><span class="k-sprite"></span> #: label #</button>',
            closeValue: false,
            width: 400
        },

        _renderButtons: function()
        {
            var that = this,
                parentElement = that.element.find(".k-dialog-window-actions"),
                buttons = that.options.buttons,
                i,
                button,
                buttonElement;

            for (i = 0; i < buttons.length; i++)
            {
                button = buttons[i];
                buttonElement = $(that.buttonTemplate(button));
                parentElement.append(buttonElement);
                buttonElement.kendoButton({
                    spriteCssClass: button.spriteCssClass,
                    click: jQuery.proxy(that._buttonClicked, that, button.value)
                });
            }
        },

        _buttonClicked: function(value)
        {
            this.completed = true;
            this.window.close();
            this.options.select(value);
        },

        show: function()
        {
            var that = this;

            that.completed = false;

            that.window = $(that.element).kendoWindow({
                width: Math.min(that.options.width, $(window).width() - 80),
                maxHeight: $(window).height() - 80,
                modal: true,
                title: that.options.title,
                close: function()
                {
                    if (!that.completed)
                    {
                        if (that.options.cancel)
                        {
                            that.options.select(that.options.closeValue);
                        }
                    }
                },
                deactivate: function()
                {
                    that.window.destroy();
                }
            }).data("kendoWindow");

            that.window.center();
            that.window.open();
        },

        close: function()
        {
            this.window.close();
        }
    });
}(jQuery));
