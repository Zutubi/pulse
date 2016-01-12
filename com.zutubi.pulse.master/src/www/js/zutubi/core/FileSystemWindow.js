// dependency: ./namespace.js
// dependency: ./FileSystemTree.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.core.FileSystemWindow = Observable.extend({
        // options: {
        //    title: window title (default: "")
        //    selectLabel: text for the ok/select button (default: "ok")
        //    fs: file system (default: "local")
        //    basePath: path to use for the root of this tree (default: "")
        //    select: function(path) called when user selects a file with the file's path relative
        //            to this tree's root
        // }
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div style="display: none">' +
                    '<div class="k-fs-window-content">' +
                        '<div class="k-fs-tree-wrapper">' +
                            '<div class="k-fs-tree">' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="k-fs-window-actions"></div>' +
                '</div>',
                {
                    wrap: false,
                    evalTemplate: false
                });

            that.element = that.view.render("body");
            that.tree = that.element.find(".k-fs-tree").kendoZaFileSystemTree(options).data("kendoZaFileSystemTree");
            that.tree.bind("select", jQuery.proxy(that._nodeSelected, that));
            that.buttonTemplate = kendo.template(that.options.buttonTemplate);
            that._renderButtons();
        },

        options: {
            title: "Select File",
            selectLabel: "ok",
            buttonTemplate: '<button><span class="k-sprite"></span> #: label #</button>',
            width: 400,
            height: 500
        },

        _renderButtons: function()
        {
            var that = this,
                parentElement = that.element.find(".k-fs-window-actions"),
                buttonElement;

            buttonElement = $(that.buttonTemplate({label: that.options.selectLabel}));
            parentElement.append(buttonElement);
            that.okButton = buttonElement.kendoButton({
                enable: false,
                spriteCssClass: "fa fa-check-circle",
                click: jQuery.proxy(that._buttonClicked, that, true)
            }).data("kendoButton");

            buttonElement = $(that.buttonTemplate({label: "cancel"}));
            parentElement.append(buttonElement);
            buttonElement.kendoButton({
                spriteCssClass: "fa fa-times-circle",
                click: jQuery.proxy(that._buttonClicked, that, false)
            });
        },

        _nodeSelected: function(e)
        {
            this.okButton.enable(e.node);
        },

        _buttonClicked: function(ok)
        {
            var item;

            this.window.close();
            if (ok && this.options.select)
            {
                item = this.tree.dataItem(this.tree.select());
                this.options.select(item ? item.path : "");
            }
        },

        show: function()
        {
            var that = this,
                maxWidth = $(window).width() - 80;
                maxHeight = $(window).height() - 80;

            that.completed = false;

            that.window = $(that.element).kendoWindow({
                width: Math.min(that.options.width, maxWidth),
                height: Math.min(that.options.height, maxHeight),
                minWidth: 200,
                maxWidth: maxWidth,
                minHeight: 200,
                maxHeight: maxHeight,
                modal: true,
                title: that.options.title,
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
