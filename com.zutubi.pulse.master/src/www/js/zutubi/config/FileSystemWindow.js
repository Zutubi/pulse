// dependency: ./namespace.js
// dependency: ./FileSystemTree.js

(function($)
{
    var Observable = kendo.Observable;

    Zutubi.config.FileSystemWindow = Observable.extend({
        // options: {
        //    title: window title (default: "")
        //    selectLabel: text for the ok/select button (default: "ok")
        //    fs: file system (default: "local")
        //    basePath: path to use for the root of this tree (default: "")
        //    select: function(path) called when user selects a file with the file's path relative
        //            to this tree's root,
        //    targetField: if specified this is a form field to be populated by selecting a file
        // }
        init: function (options)
        {
            var that = this;

            that.options = jQuery.extend({}, that.options, options);

            Observable.fn.init.call(that);

            that.view = new kendo.View(
                '<div style="display: none">' +
                    '<div class="k-fs-window-toolbar-wrapper">' +
                        '<div class="k-fs-window-toolbar">' +
                        '</div>' +
                    '</div>' +
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
            that.tree = that.element.find(".k-fs-tree").kendoZaFileSystemTree(that.options).data("kendoZaFileSystemTree");
            that.tree.bind("select", jQuery.proxy(that._nodeSelected, that));
            that._renderToolbar();
            that._renderButtons();

            if (that.options.targetField)
            {
                kendo.ui.progress(that.element, true);
                that.tree.selectPath(that.options.targetField.getValue(), function()
                {
                    kendo.ui.progress(that.element, false);
                });
            }
        },

        options: {
            title: "Select File",
            selectLabel: "ok",
            basePath: "",
            fs: "local",
            buttonTemplate: '<button><span class="k-sprite"></span> #: label #</button>',
            width: 400,
            height: 500
        },

        _renderToolbar: function()
        {
            var that = this,
                parentElement = that.element.find(".k-fs-window-toolbar");

            this._addToolbarButton(parentElement, {
                spriteCssClass: "fa fa-refresh",
                click: jQuery.proxy(that._reloadClicked, that)
            });

            if (that.options.fs === "local")
            {
                this._addToolbarButton(parentElement, {
                    spriteCssClass: "fa fa-home",
                    click: jQuery.proxy(that._homeClicked, that)
                });
            }
        },

        _addToolbarButton: function (parentElement, options)
        {
            var buttonElement = $('<button><span class="k-sprite"></span></button>');
            parentElement.append(buttonElement);
            buttonElement.kendoButton(options);
        },

        _renderButtons: function()
        {
            var that = this,
                parentElement = that.element.find(".k-fs-window-actions"),
                buttonElement;

            that.buttonTemplate = kendo.template(that.options.buttonTemplate);

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

        _reloadClicked: function()
        {
            this.tree.reload();
        },

        _homeClicked: function()
        {
            var that = this;

            kendo.ui.progress(that.element, true);
            Zutubi.core.ajax({
                url: "/api/fs/home",
                success: function(data)
                {
                    that.tree.selectPath(data, function()
                    {
                        kendo.ui.progress(that.element, false);
                    });
                },
                error: function()
                {
                    kendo.ui.progress(that.element, false);
                }
            });
        },

        _buttonClicked: function(ok)
        {
            var item, value, field;

            this.window.close();
            if (ok)
            {
                item = this.tree.dataItem(this.tree.select());
                value = item ? item.path : "";
                field = this.options.targetField;

                if (this.options.select)
                {
                    this.options.select(value);
                }

                if (field)
                {
                    field.bindValue(value);
                    field.options.parentForm.updateButtons();
                }
            }
        },

        show: function()
        {
            var that = this,
                maxWidth = $(window).width() - 80,
                maxHeight = $(window).height() - 80;

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
