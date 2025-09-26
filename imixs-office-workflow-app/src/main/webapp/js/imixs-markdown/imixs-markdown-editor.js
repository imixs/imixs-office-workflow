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
    editor.addEventListener('input', handleInlineMarkdownConversion);
    
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
    
    // Check if converter is available
    if (typeof ImixsMarkdownConverter === 'undefined') {
        console.error('ImixsMarkdownConverter not found! Please include imixs-markdown-converter.js');
        return;
    }
    
    // Convert markdown to HTML
    const htmlContent = ImixsMarkdownConverter.markdownToHtml(markdownText);
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

/**
 * Handle inline markdown conversion for bold, italic, code and immediate header conversion
 * @param {InputEvent} e - The input event
 */
function handleInlineMarkdownConversion(e) {
    const selection = window.getSelection();
    if (selection.rangeCount === 0) return;
    
    const range = selection.getRangeAt(0);
    const textNode = range.startContainer;
    
    if (textNode.nodeType !== Node.TEXT_NODE) return;
    
    const text = textNode.textContent;
    const cursorPos = range.startOffset;
    
    // Check for immediate header conversion when space is typed
    if (e.data === ' ') {
        const currentLineText = getCurrentLineTextFromNode(textNode, cursorPos);
        const headerMatch = currentLineText.match(/^(#{1,6})$/);
        
        if (headerMatch) {
            e.preventDefault ? e.preventDefault() : (e.returnValue = false);
            convertToHeaderImmediate(headerMatch[1].length);
            return;
        }
        
        // Check for list markers
        const listMatch = currentLineText.match(/^(\*|\-|\d+\.)$/);
        if (listMatch) {
            e.preventDefault ? e.preventDefault() : (e.returnValue = false);
            convertToListImmediate(listMatch[1]);
            return;
        }
        
        // Check for blockquote
        if (currentLineText === '>') {
            e.preventDefault ? e.preventDefault() : (e.returnValue = false);
            convertToBlockquoteImmediate();
            return;
        }
    }
    
    // Check for completed markdown patterns before cursor
    checkAndConvertInlineMarkdown(textNode, text, cursorPos);
}

/**
 * Get current line text from text node up to cursor position
 * @param {Text} textNode - The text node
 * @param {number} cursorPos - Current cursor position
 * @returns {string} - The current line text before cursor
 */
function getCurrentLineTextFromNode(textNode, cursorPos) {
    const text = textNode.textContent;
    
    // Find the start of current line
    let lineStart = 0;
    for (let i = cursorPos - 1; i >= 0; i--) {
        if (text[i] === '\n') {
            lineStart = i + 1;
            break;
        }
    }
    
    // Get text from line start to cursor
    return text.substring(lineStart, cursorPos);
}

/**
 * Convert to header immediately when space is typed after #
 * @param {number} level - Header level (1-6)
 */
function convertToHeaderImmediate(level) {
    const selection = window.getSelection();
    const range = selection.getRangeAt(0);
    let currentElement = range.startContainer;
    
    // Navigate to parent element
    while (currentElement && currentElement.nodeType === Node.TEXT_NODE) {
        currentElement = currentElement.parentElement;
    }
    
    if (!currentElement || currentElement === editor) {
        currentElement = document.createElement('p');
        editor.appendChild(currentElement);
    }
    
    // Create new header element
    const headerElement = document.createElement(`h${level}`);
    headerElement.textContent = ''; // Start empty for user to type
    
    // Replace current element
    currentElement.parentNode.replaceChild(headerElement, currentElement);
    
    // Set cursor in header for immediate typing
    setCursorAtEnd(headerElement);
}

/**
 * Convert to list immediately when space is typed after marker
 * @param {string} marker - List marker (* - or 1.)
 */
function convertToListImmediate(marker) {
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
        // Remove current element and add new list item
        currentElement.remove();
    } else {
        // Create new list
        listElement = document.createElement(listType);
        currentElement.parentNode.replaceChild(listElement, currentElement);
    }
    
    // Create list item
    const listItem = document.createElement('li');
    listItem.textContent = ''; // Start empty
    listElement.appendChild(listItem);
    
    setCursorAtEnd(listItem);
}

/**
 * Convert to blockquote immediately when space is typed after >
 */
function convertToBlockquoteImmediate() {
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
    blockquote.textContent = ''; // Start empty
    
    currentElement.parentNode.replaceChild(blockquote, currentElement);
    
    setCursorAtEnd(blockquote);
}

/**
 * Set cursor at the end of an element
 * @param {Element} element - The target element
 */
function setCursorAtEnd(element) {
    const range = document.createRange();
    const selection = window.getSelection();
    
    // If element is empty, add a text node
    if (element.childNodes.length === 0) {
        element.appendChild(document.createTextNode(''));
    }
    
    range.selectNodeContents(element);
    range.collapse(false); // Collapse to end
    selection.removeAllRanges();
    selection.addRange(range);
    
    element.focus();
}

/**
 * Get current line text where cursor is positioned
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

/**
 * Check and convert inline markdown patterns
 * @param {Text} textNode - The text node
 * @param {string} text - The text content
 * @param {number} cursorPos - Current cursor position
 */
function checkAndConvertInlineMarkdown(textNode, text, cursorPos) {
    // Bold pattern **text**
    const boldPattern = /\*\*([^*]+)\*\*(\s|$)/g;
    let match;
    
    while ((match = boldPattern.exec(text)) !== null) {
        const matchEnd = match.index + match[0].length;
        if (matchEnd <= cursorPos) {
            replaceTextWithElement(textNode, match.index, matchEnd, 'strong', match[1]);
            return; // Process one at a time
        }
    }
    
    // Italic pattern *text*
    const italicPattern = /(?<!\*)\*([^*\s][^*]*[^*\s]|\S)\*(?!\*)/g;
    while ((match = italicPattern.exec(text)) !== null) {
        const matchEnd = match.index + match[0].length;
        if (matchEnd <= cursorPos) {
            replaceTextWithElement(textNode, match.index, matchEnd, 'em', match[1]);
            return;
        }
    }
    
    // Inline code pattern `code`
    const codePattern = /`([^`]+)`(\s|$)/g;
    while ((match = codePattern.exec(text)) !== null) {
        const matchEnd = match.index + match[0].length;
        if (matchEnd <= cursorPos) {
            replaceTextWithElement(textNode, match.index, matchEnd, 'code', match[1]);
            return;
        }
    }
}

/**
 * Replace text range with HTML element
 * @param {Text} textNode - The text node
 * @param {number} start - Start position
 * @param {number} end - End position
 * @param {string} tagName - HTML tag name
 * @param {string} content - Element content
 */
function replaceTextWithElement(textNode, start, end, tagName, content) {
    const parent = textNode.parentElement;
    const text = textNode.textContent;
    
    // Split text into parts
    const beforeText = text.substring(0, start);
    const afterText = text.substring(end);
    
    // Create new elements
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
    
    // Replace the text node
    parent.replaceChild(fragment, textNode);
    
    // Set cursor after the new element
    const range = document.createRange();
    const selection = window.getSelection();
    range.setStartAfter(element);
    range.collapse(true);
    selection.removeAllRanges();
    selection.addRange(range);
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