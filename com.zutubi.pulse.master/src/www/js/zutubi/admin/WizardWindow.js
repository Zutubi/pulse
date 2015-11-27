// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./Wizard.js

(function($)
{
    var Observable = kendo.Observable,
        PADDING = 40;

    Zutubi.admin.WizardWindow = Observable.extend({
        // options: {
        //    path: path we are adding to
        //    label: label of base type we are adding
        //    markRequired: pass false to disable marking required fields
        //    success: function(delta) called when the wizard finishes with a delta that includes
        //             the added model
        //    cancel: (optional) function() called when user bails
        // }
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div style="display: none">' +
                    '<div class="k-wizard-content"></div>' +
                '</div>',
                {wrap: false});

            that.element = that.view.render("body");
        },

        options: {
            label: "config",
            markRequired: true,
            id: "wizard-window",
            width: 720
        },

        mask: function(mask)
        {
            kendo.ui.progress(this.element.closest(".k-widget"), mask);
        },

        show: function()
        {
            var that = this,
                windowWidth = $(window).width(),
                width = Math.min(that.options.width, windowWidth - 2 * PADDING);

            that.completed = false;

            that.window = $(that.element).kendoWindow({
                width: width,
                maxHeight: $(window).height() - 3 * PADDING,
                position: {
                    top: PADDING,
                    left: (windowWidth - width) / 2
                },
                modal: true,
                title: "add new " + that.options.label,
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

            that.mask(true);

            Zutubi.core.ajax({
                type: "GET",
                url: "/api/wizard/" + Zutubi.config.encodePath(that.options.path),
                success: function (data)
                {
                    that.mask(false);
                    that._renderWizard(data);
                },
                error: function (jqXHR)
                {
                    that.close();
                    Zutubi.admin.reportError("Could not get wizard information: " + Zutubi.core.ajaxError(jqXHR));
                }
            });
        },

        close: function()
        {
            this.window.close();
        },

        _renderWizard: function(data)
        {
            var that = this,
                contentEl = that.element.find(".k-wizard-content"),
                wizardEl = $("<div></div>");

            contentEl.append(wizardEl);
            that.wizard = wizardEl.kendoZaWizard({
                structure: data,
                path: that.options.path,
                markRequired: that.options.markRequired
            }).data("kendoZaWizard");

            that.wizard.bind("posting", function()
            {
                that.mask(true);
            });

            that.wizard.bind("posted", function()
            {
                that.mask(false);
            });

            that.wizard.bind("finished", function(e)
            {
                that.close();
                that.options.success(e.delta);
            });

            that.wizard.bind("cancelled", function()
            {
                that.close();
                if (that.options.cancel)
                {
                    that.options.cancel();
                }
            });
        }
    });
}(jQuery));
