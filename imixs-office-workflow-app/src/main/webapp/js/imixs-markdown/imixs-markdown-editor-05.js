/**
 * Imixs Markdown Editor
 * WYSIWYG editor functionality for markdown content
 */

// Global editor instance
let editor = null;

/**
 * Initialize the editor when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeEditor();
}); 

/**
 * Initialize the contenteditable editor
 */
function initializeEditor() {
    editor = document.getElementById('editorjs');
    if (!editor) return;

    if (typeof marked == 'undefined') {
        console.log("Missing library marked.js - make sure library is loaded on startup!");
        return;
    }
    
    // Make the editor contenteditable
    editor.contentEditable = true;
    editor.style.outline = 'none';
    
    // Add placeholder text
    if (!editor.innerHTML.trim()) {
        editor.innerHTML = '<p>Klicke auf "Markdown → Editor" um zu beginnen...</p>';
        editor.style.color = '#999';
    }
    
    // Clear placeholder on focus
    editor.addEventListener('focus', function() {
        if (editor.style.color === 'rgb(153, 153, 153)') {
            editor.innerHTML = '';
            editor.style.color = '#000';
        }
    });
    
    // Handle keyboard shortcuts
    editor.addEventListener('keydown', handleKeyboardShortcuts);
    
    // Handle live markdown conversion
    editor.addEventListener('keydown', handleLiveMarkdownConversion);
    
    // Handle instant markdown conversion
    editor.addEventListener('input', handleInstantMarkdownConversion);
    
    // Handle paste events for markdown conversion
    editor.addEventListener('paste', handlePasteMarkdown);
    
    console.log('Imixs Markdown Editor initialized');
}

/**
 * Load markdown from textarea and convert to HTML in editor
 */
function loadMarkdown() {
    const textarea = document.querySelector('textarea[data-id="markdown_hidden_input"]');
    if (!textarea || !editor) return;
    
    const markdownText = textarea.value.trim();
    if (!markdownText) {
        editor.innerHTML = '<p>Kein Markdown-Inhalt gefunden...</p>';
        editor.style.color = '#999';
        return;
    }
    
    // Convert markdown to HTML
    htmlContent = marked.parse(markdownText);

    editor.innerHTML = htmlContent;
    editor.style.color = '#000';
    
    console.log('Markdown loaded and converted to HTML');
}

/**
 * Save editor content back to markdown in textarea
 */
function saveMarkdown() {
    if (!editor) return;
    
    const textarea = document.querySelector('textarea[data-id="markdown_hidden_input"]');
    if (!textarea) return;
    
    // Check if converter is available
    if (typeof ImixsMarkdownConverter === 'undefined') {
        console.error('ImixsMarkdownConverter not found! Please include imixs-markdown-converter.js');
        return;
    }
    
    // Convert HTML back to markdown
    const markdownText = ImixsMarkdownConverter.htmlToMarkdown(editor.innerHTML);
    textarea.value = markdownText;
    
    console.log('Editor content saved as markdown');
}

/**
 * Clear the editor content
 */
function clearEditor() {
    if (!editor) return;
    
    editor.innerHTML = '<p>Editor geleert...</p>';
    editor.style.color = '#999';
    
    console.log('Editor cleared');
}

/**
 * Get the current editor content as HTML
 * @returns {string} - The HTML content
 */
function getEditorHtml() {
    return editor ? editor.innerHTML : '';
}

/**
 * Set the editor content with HTML
 * @param {string} html - The HTML content to set
 */
function setEditorHtml(html) {
    if (!editor) return;
    
    editor.innerHTML = html;
    editor.style.color = '#000';
}

/**
 * Check if editor has content (not just placeholder)
 * @returns {boolean} - True if editor has actual content
 */
function hasEditorContent() {
    if (!editor) return false;
    
    const content = editor.innerHTML.trim();
    const isPlaceholder = editor.style.color === 'rgb(153, 153, 153)';
    
    return content && !isPlaceholder;
}

// =============================================================================
// PASTE MARKDOWN CONVERSION
// =============================================================================

/**
 * Handle paste events and convert markdown if detected
 * @param {ClipboardEvent} e - The paste event
 */
function handlePasteMarkdown(e) {
    e.preventDefault();
    
    // Get pasted text from clipboard
    const pastedText = (e.clipboardData || window.clipboardData).getData('text');
    
    if (!pastedText) return;
    
    // Clear placeholder if present
    if (editor.style.color === 'rgb(153, 153, 153)') {
        editor.innerHTML = '';
        editor.style.color = '#000';
    }
    
    // Check if the pasted content contains markdown syntax
    if (typeof ImixsMarkdownConverter !== 'undefined' && 
        ImixsMarkdownConverter.hasMarkdownSyntax(pastedText)) {
        
        // Convert markdown to HTML
        // const htmlContent = ImixsMarkdownConverter.markdownToHtml(pastedText);
        // Für Load/Paste
        var htmlContent="";
       // if (typeof marked !== 'undefined') {
            htmlContent = marked.parse(pastedText);
        // } else {
        //     // Fallback auf unseren Converter
        //     htmlContent = ImixsMarkdownConverter.markdownToHtml(pastedText);
        // }


        
        // Insert converted HTML
        insertConvertedMarkdown(htmlContent);
        
        console.log('Pasted content converted from markdown to HTML');
    } else {
        // Plain text - use default browser paste behavior but clean up
        insertPlainTextPaste(pastedText);
        
        console.log('Pasted plain text content');
    }
}

/**
 * Insert converted markdown HTML at cursor position
 * @param {string} htmlContent - The converted HTML content
 */
function insertConvertedMarkdown(htmlContent) {
    const selection = window.getSelection();
    
    // If no selection or editor is empty, replace entire content
    if (selection.rangeCount === 0 || !editor.textContent.trim()) {
        editor.innerHTML = htmlContent;
        // Set cursor at end
        const range = document.createRange();
        range.selectNodeContents(editor);
        range.collapse(false);
        selection.removeAllRanges();
        selection.addRange(range);
        return;
    }
    
    // Insert at current cursor position
    const range = selection.getRangeAt(0);
    range.deleteContents(); // Remove selected content if any
    
    // Create temporary container
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = htmlContent;
    
    // Insert content as document fragment
    const fragment = document.createDocumentFragment();
    while (tempDiv.firstChild) {
        fragment.appendChild(tempDiv.firstChild);
    }
    
    range.insertNode(fragment);
    
    // Position cursor after inserted content
    range.collapse(false);
    selection.removeAllRanges();
    selection.addRange(range);
}

/**
 * Insert plain text with proper formatting
 * @param {string} text - Plain text to insert
 */
function insertPlainTextPaste(text) {
    const selection = window.getSelection();
    
    // If editor is empty, wrap in paragraph
    if (!editor.textContent.trim() || selection.rangeCount === 0) {
        const p = document.createElement('p');
        p.textContent = text;
        editor.innerHTML = '';
        editor.appendChild(p);
        
        // Set cursor at end of paragraph
        const range = document.createRange();
        range.selectNodeContents(p);
        range.collapse(false);
        selection.removeAllRanges();
        selection.addRange(range);
        return;
    }
    
    // Insert at cursor position
    const range = selection.getRangeAt(0);
    range.deleteContents();
    
    // Handle multi-line text
    const lines = text.split('\n');
    if (lines.length === 1) {
        // Single line - insert as text node
        const textNode = document.createTextNode(text);
        range.insertNode(textNode);
        range.setStartAfter(textNode);
    } else {
        // Multi-line - create paragraphs
        const fragment = document.createDocumentFragment();
        
        lines.forEach((line, index) => {
            if (index === 0) {
                // First line - insert as text
                fragment.appendChild(document.createTextNode(line));
            } else {
                // Subsequent lines - wrap in paragraphs
                const p = document.createElement('p');
                p.textContent = line || '\u00A0'; // Non-breaking space for empty lines
                fragment.appendChild(p);
            }
        });
        
        range.insertNode(fragment);
    }
    
    range.collapse(false);
    selection.removeAllRanges();
    selection.addRange(range);
}

// =============================================================================
// KEYBOARD SHORTCUTS
// =============================================================================

/**
 * Handle keyboard shortcuts in the editor
 * @param {KeyboardEvent} e - The keyboard event
 */
function handleKeyboardShortcuts(e) {
    // Ctrl+B for bold
    if (e.ctrlKey && e.key === 'b') {
        e.preventDefault();
        document.execCommand('bold', false, null);
    }
    
    // Ctrl+I for italic
    if (e.ctrlKey && e.key === 'i') {
        e.preventDefault();
        document.execCommand('italic', false, null);
    }
    
    // Ctrl+S for save
    if (e.ctrlKey && e.key === 's') {
        e.preventDefault();
        saveMarkdown();
    }
    
    // Ctrl+L for load
    if (e.ctrlKey && e.key === 'l') {
        e.preventDefault();
        loadMarkdown();
    }
}

// =============================================================================
// ENTER KEY MARKDOWN CONVERSION (Legacy)
// =============================================================================

/**
 * Handle live markdown conversion on Enter key
 * @param {KeyboardEvent} e - The keyboard event
 */
function handleLiveMarkdownConversion(e) {
    if (e.key === 'Enter') {
        const selection = window.getSelection();
        if (selection.rangeCount === 0) return;
        
        const range = selection.getRangeAt(0);
        const currentElement = range.startContainer.nodeType === Node.TEXT_NODE 
            ? range.startContainer.parentElement 
            : range.startContainer;
        
        // Get current line text
        const currentText = getCurrentLineText();
        if (!currentText) return;
        
        // Check for markdown patterns
        const headerMatch = currentText.match(/^(#{1,6})\s+(.+)$/);
        const listMatch = currentText.match(/^(\*|\-|\d+\.)\s+(.+)$/);
        const blockquoteMatch = currentText.match(/^>\s+(.+)$/);
        
        if (headerMatch) {
            e.preventDefault();
            convertToHeader(headerMatch[1].length, headerMatch[2]);
        } else if (listMatch) {
            e.preventDefault();
            convertToListItem(listMatch[1], listMatch[2]);
        } else if (blockquoteMatch) {
            e.preventDefault();
            convertToBlockquote(blockquoteMatch[1]);
        }
    }
}

// =============================================================================
// INSTANT MARKDOWN CONVERSION (Primary)
// =============================================================================

/**
 * Enhanced input handler for instant markdown conversion
 * @param {InputEvent} e - The input event
 */
function handleInstantMarkdownConversion(e) {
    const selection = window.getSelection();
    if (selection.rangeCount === 0) return;
    
    const range = selection.getRangeAt(0);
    const textNode = range.startContainer;
    
    if (textNode.nodeType !== Node.TEXT_NODE) return;
    
    const text = textNode.textContent;
    const cursorPos = range.startOffset;
    
    // Check for space character input - this triggers instant conversions
    if (e.data === ' ') {
        handleSpaceTriggeredConversions(textNode, text, cursorPos, e);
    }
    
    // Handle other markdown patterns (bold, italic, code) on their closing characters
    if (e.data === '*' || e.data === '`') {
        handleClosingCharacterConversions(textNode, text, cursorPos);
    }
}

/**
 * Handle conversions triggered by space character
 * @param {Text} textNode - The text node
 * @param {string} text - The text content
 * @param {number} cursorPos - Current cursor position
 * @param {InputEvent} e - The input event
 */
function handleSpaceTriggeredConversions(textNode, text, cursorPos, e) {
    // Get the text from line start to cursor (including the space just typed)
    const currentLineText = getCurrentLineTextFromNodeToPosition(textNode, cursorPos);
    
    // Header detection: # ## ### #### ##### ######
    const headerMatch = currentLineText.match(/^(#{1,6})\s$/);
    if (headerMatch) {
        e.preventDefault();
        convertToHeaderInstantly(headerMatch[1].length, textNode);
        return;
    }
    
    // Unordered list detection: * - 
    const unorderedListMatch = currentLineText.match(/^(\*|\-)\s$/);
    if (unorderedListMatch) {
        e.preventDefault();
        convertToListInstantly('ul', textNode);
        return;
    }
    
    // Ordered list detection: 1. 2. 99.
    const orderedListMatch = currentLineText.match(/^(\d+\.)\s$/);
    if (orderedListMatch) {
        e.preventDefault();
        convertToListInstantly('ol', textNode);
        return;
    }
    
    // Blockquote detection: >
    const blockquoteMatch = currentLineText.match(/^>\s$/);
    if (blockquoteMatch) {
        e.preventDefault();
        convertToBlockquoteInstantly(textNode);
        return;
    }
    
    // Code block detection: ```
    const codeBlockMatch = currentLineText.match(/^```\s$/);
    if (codeBlockMatch) {
        e.preventDefault();
        convertToCodeBlockInstantly(textNode);
        return;
    }
}

/**
 * Handle conversions triggered by closing characters (* ` etc.)
 * @param {Text} textNode - The text node
 * @param {string} text - The text content
 * @param {number} cursorPos - Current cursor position
 */
function handleClosingCharacterConversions(textNode, text, cursorPos) {
    const beforeCursor = text.substring(0, cursorPos);
    
    if (text.charAt(cursorPos - 1) === '*') {
        // Check for bold: **text**
        const boldMatch = beforeCursor.match(/\*\*([^*]+)\*\*$/);
        if (boldMatch) {
            const matchStart = cursorPos - boldMatch[0].length;
            replaceTextRangeWithElement(textNode, matchStart, cursorPos, 'strong', boldMatch[1]);
            return;
        }
        
        // Check for italic: *text*
        const italicMatch = beforeCursor.match(/(?<!\*)\*([^*\s][^*]*[^*\s]|\S)\*$/);
        if (italicMatch) {
            const matchStart = cursorPos - italicMatch[0].length;
            replaceTextRangeWithElement(textNode, matchStart, cursorPos, 'em', italicMatch[1]);
            return;
        }
    }
    
    if (text.charAt(cursorPos - 1) === '`') {
        // Check for inline code: `code`
        const codeMatch = beforeCursor.match(/`([^`]+)`$/);
        if (codeMatch) {
            const matchStart = cursorPos - codeMatch[0].length;
            replaceTextRangeWithElement(textNode, matchStart, cursorPos, 'code', codeMatch[1]);
            return;
        }
    }
}

// =============================================================================
// INSTANT CONVERSION FUNCTIONS
// =============================================================================

/**
 * Convert to header instantly when space is typed after #
 * @param {number} level - Header level (1-6)
 * @param {Text} textNode - The current text node
 */
function convertToHeaderInstantly(level, textNode) {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    
    // Get the parent element that contains this text node
    let parentElement = textNode.parentElement;
    
    // Handle case where we're in a deep nested structure
    while (parentElement && !isBlockElement(parentElement)) {
        parentElement = parentElement.parentElement;
    }
    
    if (!parentElement || parentElement === editor) {
        parentElement = document.createElement('p');
        textNode.parentNode.insertBefore(parentElement, textNode);
        parentElement.appendChild(textNode);
    }
    
    // Remove the markdown syntax from the text node
    const text = textNode.textContent;
    const cursorPos = range.startOffset;
    const lineStart = findLineStart(text, cursorPos);
    
    // Remove the "### " part, keeping the rest of the line
    const beforeLine = text.substring(0, lineStart);
    const afterCursor = text.substring(cursorPos);
    const cleanText = beforeLine + afterCursor;
    
    // Create header element
    const headerElement = document.createElement(`h${level}`);
    
    // If there was text after the markdown syntax, move it to header
    if (afterCursor.trim()) {
        headerElement.textContent = afterCursor;
        textNode.textContent = cleanText.substring(0, lineStart);
    } else {
        // Empty header, ready for typing
        headerElement.appendChild(document.createTextNode(''));
    }
    
    // Replace the parent element with header
    parentElement.parentNode.insertBefore(headerElement, parentElement);
    parentElement.remove();
    
    // Set cursor at end of header
    setCursorAtEndOfElement(headerElement);
}

/**
 * Convert to list instantly
 * @param {string} listType - 'ul' or 'ol'
 * @param {Text} textNode - The current text node
 */
function convertToListInstantly(listType, textNode) {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    
    let parentElement = textNode.parentElement;
    while (parentElement && !isBlockElement(parentElement)) {
        parentElement = parentElement.parentElement;
    }
    
    if (!parentElement || parentElement === editor) {
        parentElement = document.createElement('p');
        textNode.parentNode.insertBefore(parentElement, textNode);
        parentElement.appendChild(textNode);
    }
    
    // Check if we're already in a list of the same type
    const existingList = parentElement.closest(listType);
    let listElement;
    
    if (existingList) {
        listElement = existingList;
        parentElement.remove(); // Remove the current paragraph/element
    } else {
        // Create new list
        listElement = document.createElement(listType);
        parentElement.parentNode.insertBefore(listElement, parentElement);
        parentElement.remove();
    }
    
    // Create list item
    const listItem = document.createElement('li');
    listItem.appendChild(document.createTextNode(''));
    listElement.appendChild(listItem);
    
    // Set cursor in list item
    setCursorAtEndOfElement(listItem);
}

/**
 * Convert to blockquote instantly
 * @param {Text} textNode - The current text node
 */
function convertToBlockquoteInstantly(textNode) {
    const selection = window.getSelection();
    let parentElement = textNode.parentElement;
    
    while (parentElement && !isBlockElement(parentElement)) {
        parentElement = parentElement.parentElement;
    }
    
    if (!parentElement || parentElement === editor) {
        parentElement = document.createElement('p');
        textNode.parentNode.insertBefore(parentElement, textNode);
        parentElement.appendChild(textNode);
    }
    
    // Create blockquote
    const blockquote = document.createElement('blockquote');
    blockquote.appendChild(document.createTextNode(''));
    
    parentElement.parentNode.insertBefore(blockquote, parentElement);
    parentElement.remove();
    
    setCursorAtEndOfElement(blockquote);
}

/**
 * Convert to code block instantly
 * @param {Text} textNode - The current text node
 */
function convertToCodeBlockInstantly(textNode) {
    let parentElement = textNode.parentElement;
    
    while (parentElement && !isBlockElement(parentElement)) {
        parentElement = parentElement.parentElement;
    }
    
    if (!parentElement || parentElement === editor) {
        parentElement = document.createElement('p');
        textNode.parentNode.insertBefore(parentElement, textNode);
        parentElement.appendChild(textNode);
    }
    
    // Create pre > code structure
    const preElement = document.createElement('pre');
    const codeElement = document.createElement('code');
    codeElement.appendChild(document.createTextNode(''));
    preElement.appendChild(codeElement);
    
    parentElement.parentNode.insertBefore(preElement, parentElement);
    parentElement.remove();
    
    setCursorAtEndOfElement(codeElement);
}

// =============================================================================
// LEGACY CONVERSION FUNCTIONS (for Enter key)
// =============================================================================

/**
 * Convert current line to header
 * @param {number} level - Header level (1-6)
 * @param {string} text - Header text
 */
function convertToHeader(level, text) {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let currentElement = range.startContainer;
    
    // Find the parent element to replace
    while (currentElement && currentElement.nodeType === Node.TEXT_NODE) {
        currentElement = currentElement.parentElement;
    }
    
    if (!currentElement || currentElement === editor) {
        currentElement = document.createElement('p');
        currentElement.textContent = `${'#'.repeat(level)} ${text}`;
        editor.appendChild(currentElement);
    }
    
    // Create new header element
    const headerElement = document.createElement(`h${level}`);
    headerElement.textContent = text;
    
    // Replace current element
    currentElement.parentNode.replaceChild(headerElement, currentElement);
    
    // Create new paragraph for continuation
    const newParagraph = document.createElement('p');
    newParagraph.innerHTML = '<br>';
    headerElement.parentNode.insertBefore(newParagraph, headerElement.nextSibling);
    
    // Set cursor in new paragraph
    setCursorAtStart(newParagraph);
}

/**
 * Convert current line to list item
 * @param {string} marker - List marker (* - or 1.)
 * @param {string} text - List item text
 */
function convertToListItem(marker, text) {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let currentElement = range.startContainer;
    
    while (currentElement && currentElement.nodeType === Node.TEXT_NODE) {
        currentElement = currentElement.parentElement;
    }
    
    if (!currentElement || currentElement === editor) {
        currentElement = document.createElement('p');
        editor.appendChild(currentElement);
    }
    
    // Determine list type
    const isOrdered = /^\d+\./.test(marker);
    const listType = isOrdered ? 'ol' : 'ul';
    
    // Check if we're already in a list
    const parentList = currentElement.closest('ul, ol');
    let listElement;
    
    if (parentList && parentList.tagName.toLowerCase() === listType) {
        listElement = parentList;
    } else {
        // Create new list
        listElement = document.createElement(listType);
        currentElement.parentNode.replaceChild(listElement, currentElement);
    }
    
    // Create list item
    const listItem = document.createElement('li');
    listItem.textContent = text;
    listElement.appendChild(listItem);
    
    // Create new list item for continuation
    const newListItem = document.createElement('li');
    newListItem.innerHTML = '<br>';
    listElement.appendChild(newListItem);
    
    setCursorAtStart(newListItem);
}

/**
 * Convert current line to blockquote
 * @param {string} text - Blockquote text
 */
function convertToBlockquote(text) {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let currentElement = range.startContainer;
    
    while (currentElement && currentElement.nodeType === Node.TEXT_NODE) {
        currentElement = currentElement.parentElement;
    }
    
    if (!currentElement || currentElement === editor) {
        currentElement = document.createElement('p');
        editor.appendChild(currentElement);
    }
    
    // Create blockquote
    const blockquote = document.createElement('blockquote');
    blockquote.textContent = text;
    
    currentElement.parentNode.replaceChild(blockquote, currentElement);
    
    // Create new paragraph
    const newParagraph = document.createElement('p');
    newParagraph.innerHTML = '<br>';
    blockquote.parentNode.insertBefore(newParagraph, blockquote.nextSibling);
    
    setCursorAtStart(newParagraph);
}

// =============================================================================
// TEXT REPLACEMENT FUNCTIONS
// =============================================================================

/**
 * Replace text range with HTML element (improved version)
 * @param {Text} textNode - The text node
 * @param {number} start - Start position
 * @param {number} end - End position
 * @param {string} tagName - HTML tag name
 * @param {string} content - Element content
 */
function replaceTextRangeWithElement(textNode, start, end, tagName, content) {
    const parent = textNode.parentElement;
    const text = textNode.textContent;
    
    // Store cursor position for restoration
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    
    // Split text
    const beforeText = text.substring(0, start);
    const afterText = text.substring(end);
    
    // Create fragment
    const fragment = document.createDocumentFragment();
    
    if (beforeText) {
        fragment.appendChild(document.createTextNode(beforeText));
    }
    
    const element = document.createElement(tagName);
    element.textContent = content;
    fragment.appendChild(element);
    
    if (afterText) {
        fragment.appendChild(document.createTextNode(afterText));
    }
    
    // Replace
    parent.replaceChild(fragment, textNode);
    
    // Restore cursor after the new element
    const newRange = document.createRange();
    newRange.setStartAfter(element);
    newRange.collapse(true);
    
    selection.removeAllRanges();
    selection.addRange(newRange);
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

/**
 * Get current line text from text node up to specific position
 * @param {Text} textNode - The text node
 * @param {number} endPos - End position
 * @returns {string} - The current line text
 */
function getCurrentLineTextFromNodeToPosition(textNode, endPos) {
    const text = textNode.textContent;
    
    // Find the start of current line
    let lineStart = 0;
    for (let i = endPos - 1; i >= 0; i--) {
        if (text[i] === '\n') {
            lineStart = i + 1;
            break;
        }
    }
    
    return text.substring(lineStart, endPos);
}

/**
 * Get current line text where cursor is positioned (for Enter key handling)
 * @returns {string} - The current line text
 */
function getCurrentLineText() {
    const selection = window.getSelection();
    if (selection.rangeCount === 0) return '';
    
    const range = selection.getRangeAt(0);
    let currentElement = range.startContainer;
    
    // Navigate to the containing element
    while (currentElement && currentElement.nodeType === Node.TEXT_NODE) {
        currentElement = currentElement.parentElement;
    }
    
    if (!currentElement) return '';
    
    // Get text content of current block element
    const text = currentElement.textContent || currentElement.innerText || '';
    return text.trim();
}

/**
 * Check if element is a block-level element
 * @param {Element} element - Element to check
 * @returns {boolean} - True if block element
 */
function isBlockElement(element) {
    const blockElements = ['P', 'DIV', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 
                          'UL', 'OL', 'LI', 'BLOCKQUOTE', 'PRE', 'HR'];
    return blockElements.includes(element.tagName);
}

/**
 * Find start of current line in text
 * @param {string} text - The text content
 * @param {number} cursorPos - Current cursor position
 * @returns {number} - Line start position
 */
function findLineStart(text, cursorPos) {
    for (let i = cursorPos - 1; i >= 0; i--) {
        if (text[i] === '\n') {
            return i + 1;
        }
    }
    return 0;
}

/**
 * Set cursor at end of element
 * @param {Element} element - Target element
 */
function setCursorAtEndOfElement(element) {
    const range = document.createRange();
    const selection = window.getSelection();
    
    // Make sure element has content to position cursor
    if (element.childNodes.length === 0) {
        element.appendChild(document.createTextNode(''));
    }
    
    range.selectNodeContents(element);
    range.collapse(false);
    
    selection.removeAllRanges();
    selection.addRange(range);
    
    element.focus();
}

/**
 * Set cursor at the start of an element
 * @param {Element} element - The target element
 */
function setCursorAtStart(element) {
    const range = document.createRange();
    const selection = window.getSelection();
    
    range.setStart(element, 0);
    range.collapse(true);
    selection.removeAllRanges();
    selection.addRange(range);
    
    element.focus();
}