For overview, usage examples, release notes, version history, a changelog, bug reports,
downloads and all other Really Simple Resources, please use these links:

Google Code: http://code.google.com/p/reallysimplehistory/
Google Group: http://groups.google.com/group/ReallySimpleHistory

--

RELEASE NOTES: Really Simple History 0.6
Released: 12/03/2007

--

HOW TO DEPLOY REALLY SIMPLE HISTORY

1. Always test RSH from a local or remote web server. It will not function properly from a file: URL on your workstation - especially in IE.

2. Always include json2007.js (or json2005.js, or an alternate JSON parser), blank.html (the library will simply not work without it), rsh.js and initialization code as below.

3. No need to include rshTestPage.html or rshTestPageTop100.opml, which merely provide a test suite for you to play with.

4. If your supported browser list differs from RSH's, you should perform browser detection in your own code. RSH does nothing to disable itself in any browser.

5. A minified version of rsh.js is included as rsh.compressed.js. This file is suitable for production deployments and reduces the code footrpint from 24k to 12k. Minification was achieved using Dojo ShrinkSafe [http://shrinksafe.dojotoolkit.org/]. Application authors are encouraged to minify their JSON library of choice using a similar tool.

6. Additional usage examples and real-world samples are available at Google Code and the RSH Google Group.

--

DEFAULT INSTALL

--

<script type="text/javascript" src="json2007.js"></script>
<script type="text/javascript" src="rsh.js"></script>

<script type="text/javascript">
window.dhtmlHistory.create();

var yourListener = function(newLocation, historyData) {
	//do something;
}

window.onload = function() {
	dhtmlHistory.initialize();
	dhtmlHistory.addListener(yourListener);
};
</script>

--

PROTOTYPE USERS

--

<script type="text/javascript" src="prototype.js"></script>
<script type="text/javascript" src="rsh.js"></script>

<script type="text/javascript">
window.dhtmlHistory.create({
	toJSON: function(o) {
		return Object.toJSON(o);
	}
	, fromJSON: function(s) {
		return s.evalJSON();
	}
});

var yourListener = function(newLocation, historyData) {
	//do something;
}

window.onload = function() {
	dhtmlHistory.initialize();
	dhtmlHistory.addListener(yourListener);
};
</script>

--

USERS WHO DON'T WANT TO MODIFY OBJECT.PROTOTYPE, FUNCTION.PROTOTYPE, ETC.

--

<script type="text/javascript" src="json2005.js"></script>
<script type="text/javascript" src="rsh.js"></script>

<script type="text/javascript">
window.dhtmlHistory.create({
	toJSON: function(o) {
		return JSON.stringify(o);
	}
	, fromJSON: function(s) {
		return JSON.parse(s);
	}
});

var yourListener = function(newLocation, historyData) {
	//do something;
}

window.onload = function() {
	dhtmlHistory.initialize();
	dhtmlHistory.addListener(yourListener);
};
</script>

--

SUPPORTED AND UNSUPPORTED BROWSERS

--

Supported

    * IE6 (Windows)
    * IE7 (Windows)
    * Firefox/Mozilla/Netscape/Gecko-based browsers (Mac, Windows, Linux - all versions since 2005)
    * Opera 9.22-9.5 (Mac and Windows)
    * Safari 2.03, 2.04 and 3.03 (Mac) 

Unsupported

    * Safari 3.x (Windows): Falls prey to fundamental bugs in Apple's current beta release.
    * Non-Mozilla-based Linux browsers.
    * Any browser not listed above; test before you deploy.

--

KNOWN ISSUES

--

* blank.html must be served from the same domain as the other HTML pages in your application. [Issue 32]

* When calling dhtmlHistory.add(newLocation, historyData), newLocation must be escaped by your application code; otherwise RSH totally breaks down. A future version of RSH will automatically escape all relevant values, but for now you must escape and unescape in your own code. [Issue 31]

* IE6/IE7: When users manually type their own hashes into the location bar, history behaves erratically. [Issue 12]

* Safari 2.03+: Use of RSH triggers the "infinite loading icon" bug. This is really a Safari bug that affects many Ajax/DHTML apps, but it's worth noting. [Issue 11]

--

CHANGE LOG

--

0.6 FINAL

	* Added a minified version (shrunk with Dojo ShrinkSafe) to the download .zip file

0.6 RC 1

    * Added rock-solid support of Opera up to 9.5 beta for Mac and PC.
    * Added rock-solid support of Safari/Mac from 2.03 through 3.03.
    * Changed window.onunload event to a modern listener using addEventListener/attachEvent.
    * Performed tons of internal refactoring.
    * Moved to two-line initialization of dhtmlHistory and historyStorage (as opposed to the previous two init calls in the library and two more in your code).
    * Added support for an optional options bundle in the initialization call to trigger debug mode or override default JSON methods.
    * Provided better ompatibility with Prototype.js thanks to the aforementioned JSON override capability.
    * Added additional choice in JSON parsers; we now ship with a default 2007 JSON parser that alters core object prototypes and an older 2005 version that doesn't.
    * Provided better user-agent sniffing that's more resistant to UA spoofing.
    * Removed a bug introduced in 0.6 beta that threw an error when you hit a virgin (hashless) page state.
    * Added graceful swallowing of errors in non-debug mode.
    * Replaced equality operators with JSLint-friendly identity operators where appropriate.
    * Cleaned up a little cruft from the 0.4 and 0.6 beta versions. 

0.6 Beta

    * Added support for IE7
    * Added support for Opera/Mac and Opera/Win (though some bugs remain)
    * Added support for Safari 2/Mac (though some bugs remain)
    * Modernized the JSON parser and moved it to a separate file
    * Provided bridge methods for JSON calls to make it easier to swap out JSON parsers
    * Rebuilt original test pages into a single test page that allows you to peek behind the scenes at hidden, hacked-in DOM elements
    * Refactored many private and public methods for better support of more browsers
    * Changed name of private historyStorage.init method to historyStorage.setup to avoid confucion with dhtmlHistory.initialize
    * Removed unused isInternational() method
    * Removed blocks of deprecated, commented-out code

