// dependency: zutubi/namespace.js
// dependency: zutubi/core/package.js

if (window.Zutubi.config === undefined)
{
    window.Zutubi.config = (function($)
    {
        var saveHooksByType = {};

        function _coerceInt(properties, name)
        {
            var value, newValue;
            if (properties.hasOwnProperty(name))
            {
                value = properties[name];
                if (value === "")
                {
                    newValue = null;
                }
                else
                {
                    newValue = Number(value);
                }

                properties[name] = newValue;
            }
        }

        return {
            normalisedPath: function(path)
            {
                if (!path)
                {
                    return "";
                }

                if (path.length > 0 && path[0] === "/")
                {
                    path = path.substring(1);
                }
                if (path.length > 0 && path[path.length - 1] === "/")
                {
                    path = path.substring(0, path.length - 1);
                }

                return path;
            },

            subPath: function(path, begin, end)
            {
                var elements = path.split("/");

                if (typeof end === "undefined")
                {
                    end = elements.length;
                }

                elements = elements.slice(begin, end);
                return elements.join("/");
            },

            parentPath: function(path)
            {
                var i = path.lastIndexOf("/");
                if (i >= 0)
                {
                    return path.substring(0, i);
                }

                return null;
            },

            baseName: function(path)
            {
                var i = path.lastIndexOf("/");
                if (i >= 0)
                {
                    return path.substring(i + 1);
                }

                return path;
            },

            templateOwner: function(path)
            {
                var normalisedPath = Zutubi.config.normalisedPath(path);
                if (normalisedPath.indexOf("/") < 0)
                {
                    return null;
                }

                return Zutubi.config.subPath(normalisedPath, 1, 2);
            },

            encodePath: function(path)
            {
                var pieces, encodedPath, i;

                pieces = path.split('/');
                encodedPath = '';

                for (i = 0; i < pieces.length; i++)
                {
                    if (encodedPath.length > 0)
                    {
                        encodedPath += '/';
                    }

                    encodedPath += encodeURIComponent(pieces[i]);
                }

                return encodedPath;
            },

            coerceProperties: function(properties, propertyTypes)
            {
                var i,
                    propertyType;

                if (propertyTypes)
                {
                    for (i = 0; i < propertyTypes.length; i++)
                    {
                        propertyType = propertyTypes[i];
                        if (propertyType.shortType === "int")
                        {
                            _coerceInt(properties, propertyType.name);
                        }
                    }
                }
            },

            getValidationErrors: function(jqXHR)
            {
                var details;

                if (jqXHR.status === 422)
                {
                    try
                    {
                        details = JSON.parse(jqXHR.responseText);
                        if (details.type === "com.zutubi.pulse.master.rest.errors.ValidationException")
                        {
                            return details;
                        }
                    }
                    catch (e)
                    {
                        // Do nothing.
                    }
                }

                return null;
            },

            checkConfig: function(path, type, form, checkForm)
            {
                var properties = form.getValues(),
                    checkProperties = checkForm.getValues(),
                    url = path ? "/api/action/check/" + Zutubi.config.encodePath(path) : "/setup-api/setup/check";

                form.clearMessages();

                Zutubi.config.coerceProperties(properties, type.simpleProperties);
                Zutubi.config.coerceProperties(checkProperties, type.checkType.simpleProperties);

                Zutubi.core.ajax({
                    type: "POST",
                    maskAll: true,
                    url: url,
                    data: {
                        main: {kind: "composite", properties: properties, type: {symbolicName: type.symbolicName}},
                        check: {kind: "composite", properties: checkProperties}
                    },
                    success: function (data)
                    {
                        var message = data.success ? "configuration ok" : (data.message || "check failed");
                        checkForm.showStatus(data.success, message);
                    },
                    error: function (jqXHR)
                    {
                        var details = Zutubi.config.getValidationErrors(jqXHR);

                        if (details)
                        {
                            if (details.key === "main")
                            {
                                form.showValidationErrors(details.validationErrors);
                            }
                            else
                            {
                                checkForm.showValidationErrors(details.validationErrors);
                            }
                        }
                        else
                        {
                            Zutubi.core.reportError("Could not check configuration: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    }
                });
            },

            /**
             * The default save function.
             *
             * @param options a single object argument containing:
             *          - path: path of the item being saved
             *          - composite: the item being saved (as it was before the user edited it)
             *          - properties: properties to save (already coerced to correct types)
             *          - success: callback to invoke on successful save, expects a delta model
             *          - invalid: callback to invoke if the passed properties fail validation,
             *            passed a validationErrors object (maps field->[errors])
             *          - cancel: callback to invoke if the user backs out of the save
             */
            saveConfig: function(options)
            {
                Zutubi.core.ajax({
                    type: "PUT",
                    maskAll: true,
                    url: "/api/config/" + Zutubi.config.encodePath(options.path) + "?depth=1",
                    data: {kind: "composite", properties: options.properties},
                    success: function (data)
                    {
                        options.success(data);
                    },
                    error: function (jqXHR)
                    {
                        var details = Zutubi.config.getValidationErrors(jqXHR);

                        if (details)
                        {
                            options.invalid(details.validationErrors);
                        }
                        else
                        {
                            Zutubi.core.reportError("Could not save configuration: " + Zutubi.core.ajaxError(jqXHR));
                        }
                    }
                });
            },

            /**
             * Registers a custom function to handle saving a composite type.
             *
             * @param type symbolic name of the type to handle saving for
             * @param hookFn a function to handle the save, passed the same options as saveConfig
             */
            registerSaveHook: function(type, hookFn)
            {
                saveHooksByType[type] = hookFn;
            },

            saveHookForType: function(type)
            {
                return saveHooksByType[type] || Zutubi.config.saveConfig;
            }
        };
    }(jQuery));
}
