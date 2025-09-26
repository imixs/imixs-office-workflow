/**
 * Imixs Markdown Editor - Optimized Version
 * WYSIWYG editor functionality for markdown content
 * Uses marked.js + ImixsMarkdownConverter (Turndown.js)
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

    // Verify required libraries
    if (typeof marked === 'undefined') {
        console.error('Missing library marked.js - make sure library is loaded on startup!');
        return;
    }
    
    if (typeof ImixsMarkdownConverter === 'undefined') {
        console.error('Missing ImixsMarkdownConverter - make sure library is loaded on startup!');
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
    editor.addEventListener('focus', clearPlaceholderOnFocus);
    
    // Handle keyboard shortcuts
    editor.addEventListener('keydown', handleKeyboardShortcuts);
    
    // Handle instant markdown conversion
    editor.addEventListener('input', handleInstantMarkdownConversion);
    
    // Handle paste events for markdown conversion
    editor.addEventListener('paste', handlePasteMarkdown);
    
    console.log('Imixs Markdown Editor initialized (optimized version)');
}

/**
 * Clear placeholder when editor gets focus
 */
function clearPlaceholderOnFocus() {
    if (editor.style.color === 'rgb(153, 153, 153)') {
        editor.innerHTML = '';
        editor.style.color = '#000';
    }
}

// =============================================================================
// CORE FUNCTIONS
// =============================================================================

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
    
    // Convert markdown to HTML using marked.js
    const htmlContent = marked.parse(markdownText);
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
    
    // Convert HTML back to markdown using Turndown.js
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
// KEYBOARD SHORTCUTS
// =============================================================================

/**
 * Handle keyboard shortcuts in the editor
 * @param {KeyboardEvent} e - The keyboard event
 */
function handleKeyboardShortcuts(e) {
    if (!e.ctrlKey) return;
    
    switch(e.key) {
        case 'b':
            e.preventDefault();
            document.execCommand('bold', false, null);
            break;
        case 'i':
            e.preventDefault();
            document.execCommand('italic', false, null);
            break;
        case 's':
            e.preventDefault();
            saveMarkdown();
            break;
        case 'l':
            e.preventDefault();
            loadMarkdown();
            break;
    }
}

// =============================================================================
// PASTE HANDLING
// =============================================================================

/**
 * Handle paste events and convert markdown if detected
 * @param {ClipboardEvent} e - The paste event
 */
function handlePasteMarkdown(e) {
    e.preventDefault();
    
    const pastedText = (e.clipboardData || window.clipboardData).getData('text');
    if (!pastedText) return;
    
    // Clear placeholder if present
    if (editor.style.color === 'rgb(153, 153, 153)') {
        editor.innerHTML = '';
        editor.style.color = '#000';
    }
    
    // Check if the pasted content contains markdown syntax
    if (ImixsMarkdownConverter.hasMarkdownSyntax(pastedText)) {
        const htmlContent = marked.parse(pastedText);
        insertConvertedMarkdown(htmlContent);
        console.log('Pasted content converted from markdown to HTML');
    } else {
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
        setCursorAtEnd();
        return;
    }
    
    // Insert at current cursor position
    const range = selection.getRangeAt(0);
    range.deleteContents();
    
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = htmlContent;
    
    const fragment = document.createDocumentFragment();
    while (tempDiv.firstChild) {
        fragment.appendChild(tempDiv.firstChild);
    }
    
    range.insertNode(fragment);
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
        setCursorAtEndOfElement(p);
        return;
    }
    
    // Insert at cursor position
    const range = selection.getRangeAt(0);
    range.deleteContents();
    
    const lines = text.split('\n');
    if (lines.length === 1) {
        const textNode = document.createTextNode(text);
        range.insertNode(textNode);
        range.setStartAfter(textNode);
    } else {
        const fragment = document.createDocumentFragment();
        
        lines.forEach((line, index) => {
            if (index === 0) {
                fragment.appendChild(document.createTextNode(line));
            } else {
                const p = document.createElement('p');
                p.textContent = line || '\u00A0';
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
// INSTANT MARKDOWN CONVERSION
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
    
    // Handle space-triggered conversions
    if (e.data === ' ') {
        handleSpaceTriggeredConversions(textNode, text, cursorPos, e);
    }
    
    // Handle closing character conversions (bold, italic, code)
    if (e.data === '*' || e.data === '`') {
        handleClosingCharacterConversions(textNode, text, cursorPos);
    }
}

/**
 * Handle conversions triggered by space character
 */
function handleSpaceTriggeredConversions(textNode, text, cursorPos, e) {
    const currentLineText = getCurrentLineTextFromNodeToPosition(textNode, cursorPos);
    
    // Header detection
    const headerMatch = currentLineText.match(/^(#{1,6})\s$/);
    if (headerMatch) {
        e.preventDefault();
        convertToHeaderInstantly(headerMatch[1].length, textNode);
        return;
    }
    
    // Unordered list detection
    const unorderedListMatch = currentLineText.match(/^(\*|\-)\s$/);
    if (unorderedListMatch) {
        e.preventDefault();
        convertToListInstantly('ul', textNode);
        return;
    }
    
    // Ordered list detection
    const orderedListMatch = currentLineText.match(/^(\d+\.)\s$/);
    if (orderedListMatch) {
        e.preventDefault();
        convertToListInstantly('ol', textNode);
        return;
    }
    
    // Blockquote detection
    const blockquoteMatch = currentLineText.match(/^>\s$/);
    if (blockquoteMatch) {
        e.preventDefault();
        convertToBlockquoteInstantly(textNode);
        return;
    }
    
    // Code block detection
    const codeBlockMatch = currentLineText.match(/^```\s$/);
    if (codeBlockMatch) {
        e.preventDefault();
        convertToCodeBlockInstantly(textNode);
        return;
    }
}

/**
 * Handle conversions triggered by closing characters
 */
function handleClosingCharacterConversions(textNode, text, cursorPos) {
    const beforeCursor = text.substring(0, cursorPos);
    const lastChar = text.charAt(cursorPos - 1);
    
    if (lastChar === '*') {
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
    
    if (lastChar === '`') {
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
 */
function convertToHeaderInstantly(level, textNode) {
    const headerElement = document.createElement(`h${level}`);
    headerElement.appendChild(document.createTextNode(''));
    
    const parentElement = findOrCreateBlockParent(textNode);
    parentElement.parentNode.insertBefore(headerElement, parentElement);
    parentElement.remove();
    
    setCursorAtEndOfElement(headerElement);
}

/**
 * Convert to list instantly
 */
function convertToListInstantly(listType, textNode) {
    const parentElement = findOrCreateBlockParent(textNode);
    
    // Check if we're already in a list of the same type
    const existingList = parentElement.closest(listType);
    let listElement;
    
    if (existingList) {
        listElement = existingList;
        parentElement.remove();
    } else {
        listElement = document.createElement(listType);
        parentElement.parentNode.insertBefore(listElement, parentElement);
        parentElement.remove();
    }
    
    const listItem = document.createElement('li');
    listItem.appendChild(document.createTextNode(''));
    listElement.appendChild(listItem);
    
    setCursorAtEndOfElement(listItem);
}

/**
 * Convert to blockquote instantly
 */
function convertToBlockquoteInstantly(textNode) {
    const blockquote = document.createElement('blockquote');
    blockquote.appendChild(document.createTextNode(''));
    
    const parentElement = findOrCreateBlockParent(textNode);
    parentElement.parentNode.insertBefore(blockquote, parentElement);
    parentElement.remove();
    
    setCursorAtEndOfElement(blockquote);
}

/**
 * Convert to code block instantly
 */
function convertToCodeBlockInstantly(textNode) {
    const preElement = document.createElement('pre');
    const codeElement = document.createElement('code');
    codeElement.appendChild(document.createTextNode(''));
    preElement.appendChild(codeElement);
    
    const parentElement = findOrCreateBlockParent(textNode);
    parentElement.parentNode.insertBefore(preElement, parentElement);
    parentElement.remove();
    
    setCursorAtEndOfElement(codeElement);
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

/**
 * Find or create a block parent element for a text node
 */
function findOrCreateBlockParent(textNode) {
    let parentElement = textNode.parentElement;
    
    while (parentElement && !isBlockElement(parentElement)) {
        parentElement = parentElement.parentElement;
    }
    
    if (!parentElement || parentElement === editor) {
        parentElement = document.createElement('p');
        textNode.parentNode.insertBefore(parentElement, textNode);
        parentElement.appendChild(textNode);
    }
    
    return parentElement;
}

/**
 * Get current line text from text node up to specific position
 */
function getCurrentLineTextFromNodeToPosition(textNode, endPos) {
    const text = textNode.textContent;
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
 * Check if element is a block-level element
 */
function isBlockElement(element) {
    const blockElements = ['P', 'DIV', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 
                          'UL', 'OL', 'LI', 'BLOCKQUOTE', 'PRE', 'HR'];
    return blockElements.includes(element.tagName);
}

/**
 * Replace text range with HTML element
 */
function replaceTextRangeWithElement(textNode, start, end, tagName, content) {
    const parent = textNode.parentElement;
    const text = textNode.textContent;
    
    const beforeText = text.substring(0, start);
    const afterText = text.substring(end);
    
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
    
    parent.replaceChild(fragment, textNode);
    
    // Position cursor after the new element
    const range = document.createRange();
    range.setStartAfter(element);
    range.collapse(true);
    
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
}

/**
 * Set cursor at end of element
 */
function setCursorAtEndOfElement(element) {
    const range = document.createRange();
    const selection = window.getSelection();
    
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
 * Set cursor at end of editor
 */
function setCursorAtEnd() {
    const range = document.createRange();
    range.selectNodeContents(editor);
    range.collapse(false);
    
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
}