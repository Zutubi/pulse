// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.config.WorkflowWindow = Observable.extend({
        // options: {
        //    url: URL to GET to load the information required to render window content
        //    title: window title (default: "")
        //    continueLabel: label of continue button (default: "ok")
        //    render: function (data, el) called with result of GET and content element to populate
        //    success: function() called when user chooses to continue
        //    cancel: function() called when user bails,
        //    width: width of the window (default: 400)
        // }
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div style="display: none">' +
                    '<div class="k-dialog-window-content"></div>' +
                    '<div class="k-dialog-window-actions"></div>' +
                '</div>',
                {wrap: false});

            that.element = that.view.render("body");
            that.buttonTemplate = kendo.template(that.options.buttonTemplate);
        },

        options: {
            title: "",
            continueLabel: "ok",
            buttonTemplate: '<button id="#: id #" value="#: name #"><span class="k-sprite"></span> #: name #</button>',
            width: 400
        },

        mask: function(mask)
        {
            kendo.ui.progress(this.element.closest(".k-widget"), mask);
        },

        show: function()
        {
            var that = this;

            that.completed = false;

            that.window = $(that.element).kendoWindow({
                width: Math.min(that.options.width, $(window).width() - 80),
                maxHeight: $(window).height() - 80,
                modal: true,
                actions: [],
                title: that.options.title,
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

            that.window.center();
            that.window.open();

            that.mask(true);

            Zutubi.core.ajax({
                type: "GET",
                url: window.baseUrl + that.options.url,
                success: function (data)
                {
                    that.mask(false);
                    that.options.render(data, that.element.find(".k-dialog-window-content"));
                    that._renderButtons();
                    that.window.center();
                },
                error: function (jqXHR)
                {
                    that.window.close();
                    Zutubi.core.reportError("Could not load: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        },

        complete: function()
        {
            this.completed = true;
            this.options.success();
        },

        close: function()
        {
            this.window.close();
        },

        _renderButtons: function()
        {
            var that = this,
                parentElement = that.element.find(".k-dialog-window-actions"),
                buttonElement;

            buttonElement = $(that.buttonTemplate({name: that.options.continueLabel, id: that.options.id + "-continue"}));
            parentElement.append(buttonElement);
            buttonElement.kendoButton({
                spriteCssClass: "fa fa-check-circle",
                click: jQuery.proxy(that.complete, that)
            });

            buttonElement = $(that.buttonTemplate({name: "cancel", id: that.options.id + "-cancel"}));
            parentElement.append(buttonElement);
            buttonElement.kendoButton({
                spriteCssClass: "fa fa-times-circle",
                click: jQuery.proxy(that.close, that)
            });
        }
    });
}(jQuery));
