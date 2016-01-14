// dependency: ./namespace.js
// dependency: ./CreateDirWindow.js
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

            that.buttonTemplate = kendo.template(that.options.buttonTemplate);
            that.selectedNodeButtons = [];
            that._renderToolbar();
            that._renderButtons();

            if (that.options.targetField)
            {
                that._mask(true);
                that.tree.selectPath(that.options.targetField.getValue(), function()
                {
                    that._mask(false);
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
            }).element.addClass("k-fs-window-toolbar-first");

            if (that.options.fs === "local")
            {
                this._addToolbarButton(parentElement, {
                    spriteCssClass: "fa fa-home",
                    click: jQuery.proxy(that._homeClicked, that)
                });

                parentElement.append('<span class="k-flex-spacer"></span>');

                this.selectedNodeButtons.push(this._addToolbarButton(parentElement, {
                    label: "add",
                    spriteCssClass: "fa fa-plus fa-folder-o",
                    click: jQuery.proxy(that._createDirClicked, that)
                }));
                this.selectedNodeButtons.push(this._addToolbarButton(parentElement, {
                    label: "remove",
                    spriteCssClass: "fa fa-minus fa-folder-o",
                    click: jQuery.proxy(that._removeDirClicked, that)
                }));
            }
        },

        _addToolbarButton: function (parentElement, options)
        {
            var buttonElement = $(this.buttonTemplate({label: options.label || ""}));
            parentElement.append(buttonElement);
            return buttonElement.kendoButton(options).data("kendoButton");
        },

        _renderButtons: function()
        {
            var that = this,
                parentElement = that.element.find(".k-fs-window-actions"),
                buttonElement;

            buttonElement = $(that.buttonTemplate({label: that.options.selectLabel}));
            parentElement.append(buttonElement);
            that.selectedNodeButtons.push(buttonElement.kendoButton({
                enable: false,
                spriteCssClass: "fa fa-check-circle",
                click: jQuery.proxy(that._buttonClicked, that, true)
            }).data("kendoButton"));

            buttonElement = $(that.buttonTemplate({label: "cancel"}));
            parentElement.append(buttonElement);
            buttonElement.kendoButton({
                spriteCssClass: "fa fa-times-circle",
                click: jQuery.proxy(that._buttonClicked, that, false)
            });
        },

        _nodeSelected: function(e)
        {
            jQuery.each(this.selectedNodeButtons, function(i, button)
            {
                button.enable(e.node);
            });
        },

        _mask: function(mask)
        {
            kendo.ui.progress(this.element, mask);
        },

        _reloadClicked: function()
        {
            this.tree.reload();
        },

        _homeClicked: function()
        {
            var that = this;

            that._mask(true);
            Zutubi.core.ajax({
                url: window.apiPath + "/fs/home",
                success: function(data)
                {
                    that.tree.selectPath(data, function()
                    {
                        that._mask(false);
                    });
                },
                error: function()
                {
                    that._mask(false);
                }
            });
        },

        _createDirClicked: function()
        {
            var that = this,
                prompt,
                path;

            prompt = new Zutubi.config.CreateDirWindow({
                create: function(dirname)
                {
                    prompt.close();

                    path = that.tree.getSelectedPath() + "/" + dirname;

                    that._mask(true);
                    Zutubi.core.ajax({
                        url: window.apiPath + "/fs/local/" + Zutubi.config.encodePath(path),
                        method: "POST",
                        success: function()
                        {
                            that.tree.addDir(dirname);
                            that.tree.selectPath(path, function()
                            {
                                that._mask(false);
                            });
                        },
                        error: function(jqXHR)
                        {
                            that._mask(false);
                            Zutubi.core.reportError("Could not create directory: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    });

                }
            });
            prompt.show();
        },

        _removeDirClicked: function()
        {
            var that = this,
                path = that.tree.getSelectedPath();

            if (path)
            {
                that._mask(true);
                Zutubi.core.ajax({
                    url: window.apiPath + "/fs/local/" + Zutubi.config.encodePath(path),
                    method: "DELETE",
                    success: function()
                    {
                        that.tree.removeDir();
                        that._mask(false);
                    },
                    error: function(jqXHR)
                    {
                        that._mask(false);
                        Zutubi.core.reportError("Could not remove directory: " + Zutubi.core.ajaxError(jqXHR));
                    }
                });
            }
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
                windowWidth = $(window).width(),
                maxWidth = windowWidth - 80,
                width = Math.min(that.options.width, maxWidth),
                maxHeight = $(window).height() - 80;

            that.window = $(that.element).kendoWindow({
                position: {
                    top: 40,
                    left: (windowWidth - width) / 2
                },
                width: width,
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

            //that.window.center();
            that.window.open();
        },

        close: function()
        {
            this.window.close();
        }
    });
}(jQuery));
