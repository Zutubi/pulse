//dependency: ext/package.js
//dependency: ./namespace.js

Zutubi.FloatManager = (function() {
    var ID_SUFFIX_BUTTON = '-button',
        ID_SUFFIX_LINK = '-link',
        ID_SUFFIX_WINDOW = '-window',
        ID_SUFFIX_WINDOW_CONTENT = '-window-content',
    
        CLASS_SUFFIX_PRESSED = '-pressed',
    
        idByCategory = {},
        clsByCategory = {},
        displayedCategories = 0,
        showTime = new Date(),
        initialised = false,
        onMouseDown;

    function getWindowId(category)
    {
        return category + ID_SUFFIX_WINDOW;
    }

    function unpress(id, cls)
    {
        var buttonEl;

        buttonEl = Ext.get(id + ID_SUFFIX_BUTTON);
        if(buttonEl)
        {
            buttonEl.removeClass(cls + CLASS_SUFFIX_PRESSED);
        }
    }

    function hideAll()
    {
        var category, id, cls;

        for (category in idByCategory)
        {
            id = idByCategory[category];
            cls = clsByCategory[category];
            unpress(id, cls);
            Ext.get(getWindowId(category)).setDisplayed(false);
        }

        idByCategory = {};
        clsByCategory = {};
        displayedCategories = 0;
        Ext.getDoc().un('mousedown', onMouseDown);
    }

    function initialise()
    {
        Ext.getDoc().addKeyListener(27, function() {
            if(displayedCategories > 0)
            {
                hideAll();
            }
        });
    }

    function press(id, cls)
    {
        var buttonEl;

        buttonEl = Ext.get(id + ID_SUFFIX_BUTTON);
        if(buttonEl)
        {
            buttonEl.addClass(cls + CLASS_SUFFIX_PRESSED);
        }
    }

    onMouseDown = function(e)
    {
        if(showTime.getElapsed() > 50 && displayedCategories > 0 && !e.getTarget(".floating-widget"))
        {
            hideAll();
        }
    };

    return {
        /**
         * Function to show or hide a small floating window.  Used to popup full
         * comments from summaries and lists of build results.  Note that it is
         * critical that the element being popped up is a direct child of the
         * document body (Ext.anchorTo requires this).
         *
         * The category is used to differentiate popups of different purposes.  If
         * the user requests a popup of category X, and a popup is already showing of
         * the same category, then this latter popup will be reused and moved.
         */
        showHideFloat: function(category, id, align, cls)
        {
            var windowId, windowEl, displayedId, displayedCls, contentId, linkEl;

            if (!align)
            {
                align = 'tr-br?';
            }

            if (!cls)
            {
                cls = 'popdown';
            }

            if(!initialised)
            {
                initialise();
            }

            showTime = new Date();

            windowId = getWindowId(category);
            windowEl = Ext.get(windowId);
            displayedId = idByCategory[category];
            displayedCls = clsByCategory[category];
            if(windowEl && displayedId === id)
            {
                unpress(id, displayedCls);
                windowEl.setDisplayed(false);
                delete idByCategory[category];
                delete clsByCategory[category];
                if (--displayedCategories === 0)
                {
                    Ext.getDoc().un('mousedown', onMouseDown);
                }
            }
            else
            {
                contentId = category + ID_SUFFIX_WINDOW_CONTENT;
                if(!windowEl)
                {
                    windowEl = Ext.DomHelper.append(document.body, '<div id="' + windowId + '" class="floating floating-widget" style="display: none;"><div id="' + contentId + '"></div></div>', true);
                }
                else if(windowEl.isDisplayed())
                {
                    unpress(displayedId);
                }

                idByCategory[category] = id;
                clsByCategory[category] = cls;
                if (++displayedCategories === 1)
                {
                    Ext.getDoc().on('mousedown', onMouseDown);
                }

                Ext.getDom(contentId).innerHTML = Ext.getDom(id).innerHTML;

                press(id, cls);
                if (!windowEl.isDisplayed())
                {
                    windowEl.setDisplayed(true);
                }

                linkEl = Ext.get(id + ID_SUFFIX_LINK);
                windowEl.anchorTo(linkEl, align);
            }
        },
        
        hideAll: function()
        {
            hideAll();
        }
    };
}());
