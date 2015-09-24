// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoStringList",
        CLICK = "click" + ns,
        KEYPRESS = "keypress" + ns,
        RESIZE = "resize" + ns;

    Zutubi.admin.StringList = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaStringList",
            template: '<div class="k-widget k-multiselect k-header k-stringlist" id="#: id #">' +
                          '<div class="k-multiselect-wrap k-floatwrap">' +
                              '<ul class="k-reset">' +
                              '</ul>' +
                              '<button id="#: id #-add"><span class="k-sprite"></span></button>' +
                              '<input class="k-input" type="text" autocomplete="off" placeholder="enter text">' +
                          '</div>' +
                      '</div>',
            tagTemplate: '<li class="k-button" unselectable="on">' +
                             '<span unselectable="on">' +
                                 '<span class="k-handle">||</span> <span class="k-tag-content" unselectable="on">' +
                                     '#: text #' +
                                 '</span>' +
                             '</span>' +
                             '<span class="k-select">' +
                                 '<span unselectable="on" class="k-icon k-i-close">delete</span>' +
                             '</span>' +
                         '</li>'
        },

        _create: function()
        {
            var that = this,
                options = that.options;

            that.template = kendo.template(options.template);
            that.tagTemplate = kendo.template(options.tagTemplate);
            that.element.html(that.template(options.structure));
            that.outerElement = that.element.find(".k-stringlist");
            that.inputElement = that.element.find("input");

            that.addElement = that.element.find("button");
            that.addButton = that.addElement.kendoButton({spriteCssClass: "fa fa-plus-circle"}).data("kendoButton");

            that.listElement = that.element.find("ul");
            that.listElement.kendoSortable({
                ignore: ".k-tag-content",
                hint: function (element)
                {
                    return element.clone().addClass("k-hint");
                },
                placeholder: function (element)
                {
                    return element.clone().addClass("k-placeholder");
                },
                change: function()
                {
                    // Update value for sorted...
                }
            });

            if (typeof options.value !== "undefined")
            {
                that.bindValue(options.value);
            }

            that._resized();
            that._updateHandlers(true);
        },

        destroy: function()
        {
            var that = this;

            that._updateHandlers(false);

            Widget.fn.destroy.call(that);
            kendo.destroy(that.element);

            that.element = null;
        },

        _updateHandlers: function(enable)
        {
            var that = this;

            if (enable)
            {
                that.element.on(RESIZE, jQuery.proxy(that._resized, that));
                that.inputElement.on(KEYPRESS, jQuery.proxy(that._keyPressed, that));
                that.listElement.on(CLICK, ".k-i-close", jQuery.proxy(that._tagDeleteClicked, that));

                that.addButton.bind("click", jQuery.proxy(that._addClicked, that));
            }
            else
            {
                that.element.off(ns);
                that.inputElement.off(ns);
                that.listElement.off(ns);

                that.addButton.unbind();
            }
        },

        _resized: function(e)
        {
            this.inputElement.css("width", this.outerElement.width() - this.addElement.width() - 60);
        },

        _addString: function()
        {
            var input = this.inputElement.val();
            if (input.length > 0)
            {
                value = this.getValue();
                value.push(input);
                this.bindValue(value);

                this.inputElement.val('');
            }
        },

        _keyPressed: function(e)
        {
            if (e.which === 13)
            {
                this._addString();
            }
        },

        _addClicked: function(e)
        {
            this._addString();
            e.preventDefault();
        },

        _tagDeleteClicked: function(e)
        {
            $(e.target).closest("li").remove();
        },

        getFieldName: function()
        {
            return this.options.structure.name;
        },

        bindValue: function(value)
        {
            var i;

            this.listElement.children("li").remove();
            for (i = 0; i < value.length; i++)
            {
                this.listElement.append(this.tagTemplate({text: value[i]}));
            }
        },

        getValue: function()
        {
            return this.listElement.find(".k-tag-content").map(function()
            {
                return jQuery.trim($(this).text());
            }).get();
        },

        enable: function(enable)
        {
            this._updateHandlers(enable);

            this.inputElement.prop("disabled", !enable);
            this.addButton.enable(enable);
            if (enable)
            {
                this.outerElement.removeClass("k-state-disabled");
            }
            else
            {
                this.outerElement.addClass("k-state-disabled");
            }
        }
    });

    ui.plugin(Zutubi.admin.StringList);
}(jQuery));
