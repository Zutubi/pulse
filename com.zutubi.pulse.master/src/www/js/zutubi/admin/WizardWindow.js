// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./DocPanel.js
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
                '<div class="k-wizard-window">' +
                    '<div class="k-outer-split" style="width: 100%">' +
                        '<div>' +
                            '<div class="k-wizard-content"></div>' +
                        '</div>' +
                        '<div>' +
                            '<div class="k-help-wrapper"></div>' +
                        '</div>' +
                    '</div>' +
                '</div>',
                {wrap: false});

            that.element = that.view.render("body");
            that.contentEl = that.element.find(".k-wizard-content");
            that.docPanel = new Zutubi.admin.DocPanel({
                container: that.element.find(".k-help-wrapper")
            });
        },

        options: {
            label: "config",
            markRequired: true,
            id: "wizard-window",
            width: 800
        },

        mask: function(mask)
        {
            kendo.ui.progress(this.element.closest(".k-widget"), mask);
        },

        show: function()
        {
            var that = this,
                windowWidth = $(window).width(),
                width = Math.min(that.options.width, windowWidth - 2 * PADDING),
                maxHeight = $(window).height() - 3 * PADDING;

            that.completed = false;

            that.window = $(that.element).kendoWindow({
                width: width,
                maxHeight: maxHeight,
                position: {
                    top: PADDING,
                    left: (windowWidth - width) / 2
                },
                modal: true,
                resizable: false,
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
                activate: function()
                {
                    that.maxSplitterHeight = maxHeight - that.element.find(".k-window-titlebar").outerHeight();
                    that.splitter = that.element.find(".k-outer-split").kendoSplitter({
                        panes: [
                            { collapsible: false },
                            { collapsible: true, size: "250px", collapsed: true }
                        ]
                    });
                    that._updateSplitter();
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
                    that._updateSplitter();
                },
                error: function (jqXHR)
                {
                    that.close();
                    Zutubi.core.reportError("Could not get wizard information: " + Zutubi.core.ajaxError(jqXHR));
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
                wizardEl = $("<div></div>");

            that.contentEl.append(wizardEl);
            that.wizard = wizardEl.kendoZaWizard({
                structure: data,
                path: that.options.path,
                markRequired: that.options.markRequired
            }).data("kendoZaWizard");

            that.wizard.bind("rendered", jQuery.proxy(that._updateSplitter, that));
            that.wizard.bind("posting", jQuery.proxy(that.mask, that, true));
            that.wizard.bind("posted", jQuery.proxy(that.mask, that, false));

            that.wizard.bind("typeUpdated", function(e)
            {
                that._updateDocs(e.type.docs);
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
        },

        _updateDocs: function(docs)
        {
            this.docPanel.setDocs(docs);
        },

        _updateSplitter: function()
        {
            var wizardHeight = this.contentEl.outerHeight();
            this.element.find(".k-outer-split").height(Math.min(wizardHeight, this.maxSplitterHeight));
            this.splitter.resize();
        }
    });
}(jQuery));
