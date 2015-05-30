/* DHTML-Bibliothek */

var DHTML = false, DOM = false, MSIE4 = false, NS4 = false, OP = false;

if (document.getElementById) {
  DHTML = true;
  DOM = true;
} else {
  if (document.all) {
    DHTML = true;
    MSIE4 = true;
  } else {
    if (document.layers) {
      DHTML = true;
      NS4 = true;
    }
  }
}
if (window.opera) {
  OP = true;
}

function getElement (Mode, Identifier, ElementNumber) {
  var Element, ElementList;
  if (DOM) {
    if (Mode.toLowerCase() == "id") {
      Element = document.getElementById(Identifier);
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    if (Mode.toLowerCase() == "name") {
      ElementList = document.getElementsByName(Identifier);
      Element = ElementList[ElementNumber];
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    if (Mode.toLowerCase() == "tagname") {
      ElementList = document.getElementsByTagName(Identifier);
      Element = ElementList[ElementNumber];
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    return false;
  }
  if (MSIE4) {
    if (Mode.toLowerCase() == "id" || Mode.toLowerCase() == "name") {
      Element = document.all(Identifier);
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    if (Mode.toLowerCase() == "tagname") {
      ElementList = document.all.tags(Identifier);
      Element = ElementList[ElementNumber];
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    return false;
  }
  if (NS4) {
    if (Mode.toLowerCase() == "id" || Mode.toLowerCase() == "name") {
      Element = document[Identifier];
      if (!Element) {
        Element = document.anchors[Identifier];
      }
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    if (Mode.toLowerCase() == "layerindex") {
      Element = document.layers[Identifier];
      if (!Element) {
        Element = false;
      }
      return Element;
    }
    return false;
  }
  return false;
}

function getAttribute (Mode, Identifier, ElementNumber, AttributeName) {
  var Attribute;
  var Element = getElement(Mode, Identifier, ElementNumber);
  if (!Element) {
    return false;
  }
  if (DOM || MSIE4) {
    Attribute = Element.getAttribute(AttributeName);
    return Attribute;
  }
  if (NS4) {
    Attribute = Element[AttributeName]
    if (!Attribute) {
       Attribute = false;
    }
    return Attribute;
  }
  return false;
}

function getContent (Mode, Identifier, ElementNumber) {
  var Content;
  var Element = getElement(Mode, Identifier, ElementNumber);
  if (!Element) {
    return false;
  }
  if (DOM && Element.firstChild) {
    if (Element.firstChild.nodeType == 3) {
      Content = Element.firstChild.nodeValue;
    } else {
      Content = "";
    }
    return Content;
  }
  if (MSIE4) {
    Content = Element.innerText;
    return Content;
  }
  return false;
}

function setContent (Mode, Identifier, ElementNumber, Text) {
  var Element = getElement(Mode, Identifier, ElementNumber);
  if (!Element) {
    return false;
  }
  if (DOM && Element.firstChild) {
    Element.firstChild.nodeValue = Text;
    return true;
  }
  if (MSIE4) {
    Element.innerText = Text;
    return true;
  }
  if (NS4) {
    Element.document.open();
    Element.document.write(Text);
    Element.document.close();
    return true;
  }
}