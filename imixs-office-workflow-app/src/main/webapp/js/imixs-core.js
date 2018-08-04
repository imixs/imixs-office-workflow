/*******************************************************************************
 * imixs-xml.js Copyright (C) 2015, http://www.imixs.org
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You can receive a copy of the GNU General Public License at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Project:  http://www.imixs.org
 * 
 * Contributors: Ralph Soika - Software Developer
 ******************************************************************************/

/**
 * This library provides the core module functionality
 * 
 * Version 1.0.1
 */

var IMIXS = IMIXS || {};

// namespace function (by Stoyan Stefanov - JavaScript Patterns)
IMIXS.namespace = function(ns_string) {
	var parts = ns_string.split('.'), parent = IMIXS, i;

	// strip redundant leading global
	if (parts[0] === "IMIXS") {
		parts = parts.slice(1);
	}

	for (i = 0; i < parts.length; i += 1) {
		// create a property if it dosn't exist yet
		if (typeof parent[parts[i]] === "undefined") {
			parent[parts[i]] = {};
		}
		parent = parent[parts[i]];
	}
	return parent;

};



IMIXS.namespace("org.imixs.core");

IMIXS.org.imixs.core = (function() {


	// private properties
	var _not_used,
	
	/* Imixs ItemCollection */
	 ItemCollection= function (itemarray) {

		if (!itemarray) {
			// if no itemarray is provided than create an empty one
			this.item = new Array();
		} else {
			if ($.isArray(itemarray)) {
				this.item = itemarray;
			} else {
				// we test now which object is provided - entity or an item[]....
				if (itemarray.entity && $.isArray(itemarray.entity.item)) {
					this.item = itemarray.entity.item;
				} else if ($.isArray(itemarray.item)) {
					this.item = itemarray.item;
				}
			}
		}

		/**
		 * This method is used to return the value array of a name item inside the
		 * current ItemCollection. If no item with this name exists the method adds
		 * a new element with this name.
		 */
		this.getItem = function(fieldName) {
			if (!this.item)
				return "";

			var resultKey = -1;

			$.each(this.item, function(index, aitem) {
				if (aitem && aitem.name == fieldName) {
					resultKey = index;
					return false;
				}
			});

			// check if field exists?
			if (resultKey == -1) {
				// create a new element
				valueObj = {
					"name" : fieldName,
					"value" : [ {
						"xsi:type" : "xs:string",
						"$" : ""
					} ]
				};
				this.item.push(valueObj);
				resultKey = this.item.length - 1;
			}

			var valueObj = this.item[resultKey].value[0];
			if (valueObj) {
				if (typeof (valueObj['$']) == "undefined")
					return valueObj;
				else
					return valueObj['$'];
			} else
				return "";

		}

		/**
		 * Adds a new item into the collection
		 */
		this.setItem = function(fieldname, value, xsiType) {
			if (!xsiType)
				xsiType = "xs:string";
			var valueObj = {
				"name" : fieldname,
				"value" : [ {
					"xsi:type" : xsiType,
					"$" : value
				} ]
			};
			this.item.push(valueObj);
		}

		/**
		 * formats a date output
		 */
		this.getItemDate = function(fieldName) {
			var value = this.getItem(fieldName);
			return $.datepicker.formatDate('dd. M yy', new Date(value));

		}

		/**
		 * Update the item array depending on the provided object type. The method
		 * accepts entity, item[] or XMLDocuments
		 */
		this.setEntity = function(data) {

			if (!data) {
				// if no itemarray is provided than create an empty one
				this.item = new Array();
			} else
			// test if xmlDocument
			if ($.isXMLDoc(data)) {
				// parse xml doc...
				var json = IMIXS.org.imixs.xml.xml2json(data);
				this.item = json.entity.item;
			} else
			// test if data is an item[]
			if ($.isArray(data)) {
				this.item = data;
			} else
			// test if data is entity
			if (data.entity && $.isArray(data.entity.item)) {
				this.item = data.entity.item;
			} else if ($.isArray(data.item)) {
				this.item = data.item;
			}
		}

	};
	
	
	// public API
	return {
		ItemCollection : ItemCollection
	};
	
}());



