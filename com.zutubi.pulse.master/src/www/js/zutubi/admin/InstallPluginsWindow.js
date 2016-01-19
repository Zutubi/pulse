// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        INSTALLED = "installed";

    Zutubi.admin.InstallPluginsWindow = Observable.extend({
        init: function()
        {
            var that = this;

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div class="k-install-plugin">' +
                    '<p class="k-install-plugin-error" style="display:false"></p>' +
                    '<input type="file" name="file" id="plugin-files"/>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: false
                });

            that.element = that.view.render("body");
            that.errorEl = that.element.find(".k-install-plugin-error");
            that.upload = $("#plugin-files").kendoUpload({
                async: {
                    saveUrl: window.baseUrl + "/api/plugins/",
                    autoUpload: false
                },
                multiple: false,
                select: jQuery.proxy(that._onSelect, that),
                upload: jQuery.proxy(that._onUpload, that),
                error: jQuery.proxy(that._onError, that),
                success: jQuery.proxy(that._onSuccess, that)
            });
        },

        events: [
            INSTALLED
        ],

        _clearError: function()
        {
            this.errorEl.empty();
            this.errorEl.hide();
        },

        _onSelect: function()
        {
            this._clearError();
        },

        _onUpload: function(e)
        {
            var xhr = e.XMLHttpRequest;

            this._clearError();

            xhr.addEventListener("readystatechange", function onReady()
            {
                if (xhr.readyState === 1)
                {
                    xhr.setRequestHeader("X-CSRF-TOKEN", Zutubi.core.csrfToken());
                    xhr.removeEventListener("readystatechange", onReady);
                }
            });
        },

        _onError: function(e)
        {
            this.errorEl.html(kendo.htmlEncode(Zutubi.core.ajaxError(e.XMLHttpRequest)));
            this.errorEl.show();
        },

        _onSuccess: function(e)
        {
            this.trigger(INSTALLED, {plugin: e.response});
        },

        show: function()
        {
            var that = this,
                windowWidth = $(window).width(),
                width = Math.min(windowWidth - 80, 600);

            that.window = $(that.element).kendoWindow({
                position: {
                    top: 40,
                    left: (windowWidth - width) / 2
                },
                width: width,
                resizable: false,
                modal: true,
                title: "install plugin",
                deactivate: function()
                {
                    that.window.destroy();
                }
            }).data("kendoWindow");

            that.window.open();
        },

        close: function()
        {
            this.window.close();
        }
    });
}(jQuery));
