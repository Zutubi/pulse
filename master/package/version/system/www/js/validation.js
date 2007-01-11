/*
 * Really easy field validation with Prototype
 * http://tetlaw.id.au/view/blog/really-easy-field-validation-with-prototype
 * Andrew Tetlaw
 * Version 1.5.3 (2006-07-15)
 * 
 * Copyright (c) 2006 Andrew Tetlaw
 * http://www.opensource.org/licenses/mit-license.php
 */

//---( validator definition )---

Validator = Class.create();

Validator.prototype = {

    initialize : function(className, error, test, options)
    {
		this.options = Object.extend({}, options || {});
		this._test = test ? test : function(v,element){ return true };
		this.error = error ? error : 'Validation failed.';
		this.className = className;
	},
        
    test : function(v, element)
    {
		return this._test(v,element);
	}
}

//---( Validation definition )---

var Validation = Class.create();

Validation.prototype = {

    /**
     * Initialise the validation framework.
     */
    initialize : function(form, options)
    {
		this.options = Object.extend({
			onSubmit : true,        // default option value
			stopOnFirst : false,    // default option value
			immediate : false,      // default option value
			focusOnError : true,    // default option value
			useTitles : false,      // default option value
			onFormValidate : function(result, form) {},     // default callback
			onElementValidate : function(result, elm) {}    // default callback
		}, options || {});

        this.form = $(form);

        if(this.options.onSubmit)
        {
            // bind the onSubmit callback handler.
            Event.observe(this.form,'submit',this.onSubmit.bind(this),false);
        }
        
        if(this.options.immediate)
        {
			var useTitles = this.options.useTitles;
			var callback = this.options.onElementValidate;

            // bind a callback handler to the onBlur event for each of the form fields.
            Form.getElements(this.form).each(function(input)
            {
				Event.observe(input, 'blur', function(ev)
                {
                    Validation.validate(Event.element(ev),{useTitle : useTitles, onElementValidate : callback});
                });
			});
		}
	},

    /**
     * The onSubmit callback handler, triggered if the onSubmit option is 'true'
     */
    onSubmit : function(ev)
    {
		if(!this.validate())
        {
            Event.stop(ev);
        }
	},

    /**
     * Validate each of the forms fields, returning true if all of the form fields validate,
     * false otherwise.
     *
     */
    validate : function()
    {
		var result = false;
		var useTitles = this.options.useTitles;
		var callback = this.options.onElementValidate;
        
        // validate all of the forms elements.
        if(this.options.stopOnFirst)
        {
			result = Form.getElements(this.form).all( function(element) {
                return Validation.validate(element, {useTitle : useTitles, onElementValidate : callback}); 
            });
		}
        else
        {
			result = Form.getElements(this.form).collect( function(element) {
                return Validation.validate(element,{useTitle : useTitles, onElementValidate : callback});
            }).all();
		}
        
        if(!result && this.options.focusOnError)
        {
			Form.getElements(this.form).findAll(function(element)
            {
                return $(element).hasClassName('validation-failed');
            }).first().focus()
		}
		this.options.onFormValidate(result, this.form);
		return result;
	},

    /**
     * Reset the form to its unvalidated state.
     */                  
    reset : function()
    {
		Form.getElements(this.form).each(Validation.reset);
	}
}

Object.extend(Validation, {

    /**
     * element: the element or its id.
     * options: configuration options.
     */
    validate : function(element, options)
    {
		options = Object.extend({
			useTitle : false,
			onElementValidate : function(result, element) {}
		}, options || {});
        // resolve the element if the element is an id.
        element = $(element);
		var className = element.classNames();

        // for each of the classnames available on this element, run the associated validation.
        // return true if they are all successful, false otherwise.
        return className.all(function(value) {
			var test = Validation.test(value,element,options.useTitle);
			options.onElementValidate(test, element);
			return test;
		});
    },

    /**
     *
     */
    test : function(name, element, useTitle)
    {
		var v = Validation.get(name);
		var prop = '__advice'+name.camelize();
        
        // $F(element) -> returns the form element value.
        // Only validate visible elements.
        if(Validation.isVisible(element) && !v.test($F(element), element))
        {
			if(!element[prop])
            {
				var advice = Validation.getAdvice(name, element);
				if(typeof advice == 'undefined')
                {
/*
                    // if the advice is undefined, then create a default.
                    var errorMsg = useTitle ? ((element && element.title) ? element.title : v.error) : v.error;
					advice = '<div class="validation-advice" id="advice-' + name + '-' + Validation.getElmID(element) +'" style="display:none">' + errorMsg + '</div>'
					switch (element.type.toLowerCase()) {
						case 'checkbox':
						case 'radio':
							var p = element.parentNode;
							if(p)
                            {
								new Insertion.Bottom(p, advice);
							}
                            else
                            {
								new Insertion.After(element, advice);
							}
							break;
						default:
							new Insertion.After(element, advice);
				    }
*/
                    var tableRow = element;
                    while (tableRow.tagName != 'TR')
                    {
                        tableRow = tableRow.parentNode;
                    }
                    //Element.addClassName(tableRow, 'error-label');

                    // insert the advice row immediately above.
//                    var advice = this.getAdvice(name, element);
/*
                    if(typeof advice == 'undefined')
                    {
*/
                    new Insertion.Before(tableRow, this.createAdvice(name, element));
/*
                    }
                    else
                    {
                        advice.show();
                    }
*/

                    advice = $('advice-' + name + '-' + Validation.getElmID(element));
				}
				if(typeof Effect == 'undefined')
                {
                    advice.style.display = 'block';
				}
                else
                {
					new Effect.Appear(advice, {duration : 1 });
				}
			}
            element[prop] = true;
            element.removeClassName('validation-passed');
            element.addClassName('validation-failed');

//            this.showAdvice(name, element);

            return false;
		}
        else // validation is successful or not required, set the state of the field accordingly.
        {
            var advice = Validation.getAdvice(name, element);
			if(typeof advice != 'undefined')
            {
                advice.hide();
            }

            element[prop] = '';
			element.removeClassName('validation-failed');
			element.addClassName('validation-passed');

//            this.hideAdvice(name, element);

            return true;
		}
	},

    /**
     * Returns true if the specified element is visible.  An element is considered visible if all of its parents
     * up to the body node are also visible.
     *
     */
    isVisible : function(element)
    {
		while(element.tagName != 'BODY')
        {
			if(!$(element).visible())
            {
                return false;
            }
			element = element.parentNode;
		}
		return true;
	},

    /**
     * Retrieve (if available) the element that represents the 'error advice' to be displayed to the user.
     */
    getAdvice : function(name, element) {
		return Try.these(
			function(){ return $('advice-' + name + '-' + Validation.getElmID(element)) },
			function(){ return $('advice-' + Validation.getElmID(element)) }
		);
	},

    /**
     * Retrieve the id of the form element. If the id does not exist, fallback to its name.
     */
    getElmID : function(element)
    {
		return element.id ? element.id : element.name;
	},

    /**
     * Reset the element to its unvalidated state.
     */
    reset : function(element)
    {
		element = $(element);
		var classNames = element.classNames();
		classNames.each(function(value)
        {
			var prop = '__advice'+value.camelize();
			if(element[prop])
            {
				var advice = Validation.getAdvice(value, element);
				advice.hide();
				element[prop] = '';
			}
			element.removeClassName('validation-failed');
			element.removeClassName('validation-passed');
		});
	},

/*
    showAdvice: function(name, element)
    {
        // create a table row containing the error message and insert it.

        var tableRow = element;
        while (tableRow.tagName != 'TR')
        {
            tableRow = tableRow.parentNode;
        }
        Element.addClassName(tableRow, 'error-label');

        // insert the advice row immediately above.
        var advice = this.getAdvice(name, element);
        if(typeof advice == 'undefined')
        {
            advice = this.createAdvice(name, element);
            new Insertion.Before(tableRow, advice);
        }
        else
        {
            advice.show();
        }
    },
*/

    createAdvice: function(name, element)
    {
        var adviceId = 'advice-' + name + '-' + Validation.getElmID(element);
        return '<tr id=\'' + adviceId + '\'><th colspan=\'2\' class=\'error-label\'>Error text here</th></tr>';
    },

/*
    hideAdvice: function(name, element)
    {
        // search for the table row containing the error message, and remove it if located.

        var tableRow = element;
        while (tableRow.tagName != 'TR')
        {
            tableRow = tableRow.parentNode;
        }
        Element.removeClassName(tableRow, 'error-label');

        var advice = this.getAdvice(name, element);
        if(typeof advice != 'undefined')
        {
            advice.hide();
        }
    },
*/

    /**
     * Register a new validator.
     *
     * className: the class name that triggers the validation
     * error: the error message
     * test: the validation test.
     * options
     */
    add : function(className, error, test, options)
    {
		var nv = {};
		nv[className] = new Validator(className, error, test, options);
		Object.extend(Validation.methods, nv);
	},

    /**
     *
     */
    addAllThese : function(validators) {
		var nv = {};
		$A(validators).each(function(value) {
				nv[value[0]] = new Validator(value[0], value[1], value[2], (value.length > 3 ? value[3] : {}));
			});
		Object.extend(Validation.methods, nv);
	},

    /**
     * Get the named validator.  This returns a default noop validator if no validation has been registered
     * to this name.                
     */
    get : function(name)
    {
		return  Validation.methods[name] ? Validation.methods[name] : new Validator();
	},
	methods : {}
});

// register a default IsEmpty validator 
Validation.add('IsEmpty', '', function(v)
{
				return  ((v == null) || (v.length == 0)); // || /^\s+$/.test(v));
});

Validation.addAllThese([
	['required', 'This is a required field.', function(v) {
				return !Validation.get('IsEmpty').test(v);
			}],
	['validate-number', 'Please enter a valid number in this field.', function(v) {
				return Validation.get('IsEmpty').test(v) || (!isNaN(v) && !/^\s+$/.test(v));
			}],
	['validate-digits', 'Please use numbers only in this field. please avoid spaces or other characters such as dots or commas.', function(v) {
				return Validation.get('IsEmpty').test(v) ||  !/[^\d]/.test(v);
			}],
	['validate-alpha', 'Please use letters only (a-z) in this field.', function (v) {
				return Validation.get('IsEmpty').test(v) ||  /^[a-zA-Z]+$/.test(v)
			}],
	['validate-alphanum', 'Please use only letters (a-z) or numbers (0-9) only in this field. No spaces or other characters are allowed.', function(v) {
				return Validation.get('IsEmpty').test(v) ||  !/\W/.test(v)
			}],
	['validate-date', 'Please enter a valid date.', function(v) {
				var test = new Date(v);
				return Validation.get('IsEmpty').test(v) || !isNaN(test);
			}],
	['validate-email', 'Please enter a valid email address. For example fred@domain.com .', function (v) {
				return Validation.get('IsEmpty').test(v) || /\w{1,}[@][\w\-]{1,}([.]([\w\-]{1,})){1,3}$/.test(v)
			}],
	['validate-url', 'Please enter a valid URL.', function (v) {
				return Validation.get('IsEmpty').test(v) || /^(http|https|ftp):\/\/(([A-Z0-9][A-Z0-9_-]*)(\.[A-Z0-9][A-Z0-9_-]*)+)(:(\d+))?\/?/i.test(v)
			}],
	['validate-date-au', 'Please use this date format: dd/mm/yyyy. For example 17/03/2006 for the 17th of March, 2006.', function(v) {
				if(Validation.get('IsEmpty').test(v)) return true;
				var regex = /^(\d{2})\/(\d{2})\/(\d{4})$/;
				if(!regex.test(v)) return false;
				var d = new Date(v.replace(regex, '$2/$1/$3'));
				return ( parseInt(RegExp.$2, 10) == (1+d.getMonth()) ) && 
							(parseInt(RegExp.$1, 10) == d.getDate()) && 
							(parseInt(RegExp.$3, 10) == d.getFullYear() );
			}],
	['validate-currency-dollar', 'Please enter a valid $ amount. For example $100.00 .', function(v) {
				// [$]1[##][,###]+[.##]
				// [$]1###+[.##]
				// [$]0.##
				// [$].##
				return Validation.get('IsEmpty').test(v) ||  /^\$?\-?([1-9]{1}[0-9]{0,2}(\,[0-9]{3})*(\.[0-9]{0,2})?|[1-9]{1}\d*(\.[0-9]{0,2})?|0(\.[0-9]{0,2})?|(\.[0-9]{1,2})?)$/.test(v)
			}],
	['validate-one-required', 'Please select one of the above options.', function (v,elm) {
				var p = elm.parentNode;
				var options = p.getElementsByTagName('INPUT');
				return $A(options).any(function(elm) {
					return $F(elm);
				});
			}]
]);