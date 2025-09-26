"use strict";

/**
 * Imixs Markdown Editor Module
 * Enhanced Markdown Converter + WYSIWYG editor functionality 
 * Uses marked.js + Turndown.js in controlled environment
 */
IMIXS.namespace("org.imixs.workflow.markdowneditor");

// Global module variables
let editor = null;
let currentMode = 'editor'; // 'editor' or 'source'
let turndownService = null;

/**
 * Initialize when DOM is ready
 */
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the markdown editor module
    IMIXS.org.imixs.workflow.markdowneditor.init();
    
    // Close help tooltip on outside click
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.help-tooltip')) {
            const helpTooltip = document.getElementById('helpTooltip');
            if (helpTooltip) {
                helpTooltip.style.display = 'none';
            }
        }
    });
});

// Define core module
IMIXS.org.imixs.workflow.markdowneditor = (function () {
    
    if (!IMIXS.org.imixs.core) {
        console.error("ERROR - missing dependency: imixs-core.js");
    }
    
    var imixs = IMIXS.org.imixs.core,
    
    /**
     * Initialize the markdown editor and converter
     */
    init = function() {
        // Initialize converter first
        initConverter();
        // Then initialize editor
        initEditor();
        
        console.log('Imixs Markdown Editor Module initialized');
    },
    
    /**
     * Initialize the markdown converter services
     */
    initConverter = function() {
        // Verify required libraries
        if (typeof TurndownService === 'undefined') {
            console.error('Missing library Turndown.js - make sure library is loaded on startup!');
            return false;
        }
        
        if (typeof marked === 'undefined') {
            console.error('Missing library marked.js - make sure library is loaded on startup!');
            return false;
        }
        
        // Configure Turndown for consistent output
        turndownService = new TurndownService({
            headingStyle: 'atx',        // Use # for headings
            hr: '---',                  // Use --- for horizontal rules
            bulletListMarker: '*',      // Use * for unordered lists
            codeBlockStyle: 'fenced',   // Use ``` for code blocks
            fence: '```',               // Code fence style
            emDelimiter: '*',           // Use * for italic
            strongDelimiter: '**',      // Use ** for bold
            linkStyle: 'inlined'        // Use [text](url) style
        });
        
        // Add GFM table support if available
        if (typeof turndownPluginGfm !== 'undefined') {
            turndownService.use(turndownPluginGfm.tables);
        }
        
        // Configure Marked.js once
        marked.setOptions({
            gfm: true,          // GitHub Flavored Markdown
            tables: true,       // Enable table support
            breaks: false,      // Don't convert \n to <br>
            pedantic: false,    // Don't be strict about original markdown
            sanitize: false,    // Don't sanitize HTML (we trust our input)
            smartLists: true,   // Use smarter list behavior
            smartypants: false  // Don't use smart quotes
        });
        
        console.log('Enhanced Markdown Converter v3.0 initialized (Marked.js + Turndown.js)');
        return true;
    },
    
    /**
     * Initialize the contenteditable editor
     */
    initEditor = function() {
        editor = document.getElementById('editorjs');
        if (!editor) {
            console.warn('Editor element #editorjs not found');
            return false;
        }

        // Make the editor contenteditable
        editor.contentEditable = true;
        editor.style.outline = 'none';
        
        // Add placeholder text
        if (!editor.innerHTML.trim()) {
            editor.innerHTML = '<p>Click "Markdown → Editor" to begin...</p>';
            editor.style.color = '#999';
        }
        
        // Bind event handlers
        bindEditorEvents();
        
        // Initialize toggle system with delay
        setTimeout(() => {
            initToggleSystem();
        }, 100);
        
        console.log('Imixs Markdown Editor initialized (optimized version)');
        return true;
    },
    
    /**
     * Bind all editor event handlers
     */
    bindEditorEvents = function() {
        if (!editor) return;
        
        // Clear placeholder on focus
        editor.addEventListener('focus', clearPlaceholderOnFocus);
        
        // Handle keyboard shortcuts
        editor.addEventListener('keydown', handleKeyboardShortcuts);
        
        // Handle instant markdown conversion
        editor.addEventListener('input', handleInstantMarkdownConversion);
        
        // Handle paste events for markdown conversion
        editor.addEventListener('paste', handlePasteMarkdown);
        
        // Auto-sync on blur
        editor.addEventListener('blur', function() {
            if (currentMode === 'editor') {
                saveEditorToMarkdown();
            }
        });
    },
    
    /**
     * Initialize the toggle system and auto-load content
     */
    initToggleSystem = function() {
        loadMarkdownToEditor();
        enableAutoSync();
        console.log('Toggle system initialized and content auto-loaded');
    },
    
    /**
     * Clear placeholder when editor gets focus
     */
    clearPlaceholderOnFocus = function() {
        if (editor.style.color === 'rgb(153, 153, 153)') {
            editor.innerHTML = '';
            editor.style.color = '#000';
        }
    },
    
    // =============================================================================
    // CORE CONVERTER FUNCTIONS
    // =============================================================================
    
    /**
     * Convert markdown text to HTML using marked.js
     */
    markdownToHtml = function(markdown) {
        return marked.parse(markdown);
    },
    
    /**
     * Convert HTML back to markdown using turndown.js
     */
    htmlToMarkdown = function(html) {
        return turndownService.turndown(html);
    },
    
    /**
     * Check if text contains markdown syntax
     */
    hasMarkdownSyntax = function(text) {
        const markdownPatterns = [
            /^#{1,6} /m,        // Headers
            /\*\*.*?\*\*/,      // Bold
            /\*.*?\*/,          // Italic
            /`.*?`/,            // Inline code
            /```[\s\S]*?```/,   // Code blocks
            /^\* /m,            // Unordered list
            /^\d+\. /m,         // Ordered list
            /^\> /m,            // Blockquote
            /\[.*?\]\(.*?\)/,   // Links
            /!\[.*?\]\(.*?\)/,  // Images
            /^\|.*\|.*$/m,      // Tables
            /^---$/m,           // Horizontal rules
            /~~.*?~~/           // Strikethrough
        ];
        
        return markdownPatterns.some(pattern => pattern.test(text));
    },
    
    /**
     * Escape special markdown characters in text
     */
    escapeMarkdown = function(text) {
        return text.replace(/([\\`*_{}[\]()#+-.!|])/g, '\\$1');
    },
    
    /**
     * Unescape markdown characters
     */
    unescapeMarkdown = function(text) {
        return text.replace(/\\([\\`*_{}[\]()#+-.!|])/g, '$1');
    },
    
    // =============================================================================
    // EDITOR CORE FUNCTIONS
    // =============================================================================
    
    /**
     * Load markdown from textarea into editor
     */
    loadMarkdownToEditor = function() {
        const textarea = document.querySelector('textarea[data-id="markdown_data"]');
        if (!textarea || !editor) return;
        
        const markdownText = textarea.value.trim();
        if (!markdownText) {
            editor.innerHTML = '<p>Start typing or switch to source mode...</p>';
            editor.style.color = '#999';
            return;
        }
        
        // Convert markdown to HTML using marked.js
        const htmlContent = markdownToHtml(markdownText);
        editor.innerHTML = htmlContent;
        editor.style.color = '#000';
        
        console.log('Content loaded from textarea to editor');
    },
    
    /**
     * Save editor content to textarea as markdown
     */
    saveEditorToMarkdown = function() {
        if (!editor) return;
        
        const textarea = document.querySelector('textarea[data-id="markdown_data"]');
        if (!textarea) return;
        
        // Convert HTML back to markdown using Turndown.js
        const markdownText = htmlToMarkdown(editor.innerHTML);
        textarea.value = markdownText;
        
        console.log('Content saved from editor to textarea');
    },
    
    /**
     * Clear the editor content
     */
    clearEditor = function() {
        if (!editor) return;
        
        editor.innerHTML = '<p>Editor cleared...</p>';
        editor.style.color = '#999';
        
        console.log('Editor cleared');
    },
    
    // =============================================================================
    // TOGGLE SYSTEM FUNCTIONS  
    // =============================================================================
    
    /**
     * Switch to WYSIWYG Editor mode
     */
    switchToEditor = function() {
        if (currentMode === 'editor') return;

        // Sync content from textarea to editor first
        loadMarkdownToEditor();

        // Show editor, hide textarea
        const editorElement = document.getElementById('editorjs');
        const textarea = document.querySelector('textarea[data-id="markdown_data"]');
        
        if (editorElement) editorElement.style.display = 'block';
        if (textarea) textarea.style.display = 'none';

        // Update toggle buttons if they exist
        const editorToggle = document.getElementById('editorToggle');
        const sourceToggle = document.getElementById('sourceToggle');
        if (editorToggle) editorToggle.classList.add('active');
        if (sourceToggle) sourceToggle.classList.remove('active');

        currentMode = 'editor';
        console.log('Switched to Editor mode');
    },
    
    /**
     * Switch to Markdown Source mode
     */
    switchToSource = function() {
        if (currentMode === 'source') return;

        // Sync content from editor to textarea first
        saveEditorToMarkdown();

        // Hide editor, show textarea
        const editorElement = document.getElementById('editorjs');
        const textarea = document.querySelector('textarea[data-id="markdown_data"]');
        
        if (editorElement) editorElement.style.display = 'none';
        if (textarea) textarea.style.display = 'block';

        // Update toggle buttons if they exist
        const editorToggle = document.getElementById('editorToggle');
        const sourceToggle = document.getElementById('sourceToggle');
        if (sourceToggle) sourceToggle.classList.add('active');
        if (editorToggle) editorToggle.classList.remove('active');

        currentMode = 'source';
        console.log('Switched to Source mode');
    },
    
    /**
     * Save content (triggered by save button or external calls)
     */
    saveContent = function() {
        if (currentMode === 'editor') {
            saveEditorToMarkdown();
        }
        // In source mode, content is already in textarea

        console.log('Content saved');
        
        // Fire custom event
        const event = new CustomEvent('imixs-markdown-saved', { 
            detail: { mode: currentMode } 
        });
        document.dispatchEvent(event);
    },
    
    /**
     * Clear all content
     */
    clearContent = function() {
        // Clear both editor and textarea
        if (editor) {
            editor.innerHTML = '<p>Content cleared...</p>';
            editor.style.color = '#999';
        }
        
        const textarea = document.querySelector('textarea[data-id="markdown_data"]');
        if (textarea) {
            textarea.value = '';
        }

        console.log('All content cleared');
        
        // Fire custom event
        const event = new CustomEvent('imixs-markdown-cleared');
        document.dispatchEvent(event);
    },
    
    /**
     * Get current editor mode
     */
    getCurrentMode = function() {
        return currentMode;
    },
    
    /**
     * Enable auto-sync content when switching modes
     */
    enableAutoSync = function() {
        // Auto-sync textarea on blur (if in source mode)
        const textarea = document.querySelector('textarea[data-id="markdown_data"]');
        if (textarea) {
            textarea.addEventListener('blur', function() {
                if (currentMode === 'source') {
                    console.log('Textarea content updated');
                }
            });
        }

        console.log('Auto-sync enabled');
    },
    
    /**
     * Get the current editor content as HTML
     */
    getEditorHtml = function() {
        return editor ? editor.innerHTML : '';
    },
    
    /**
     * Set the editor content with HTML
     */
    setEditorHtml = function(html) {
        if (!editor) return;
        
        editor.innerHTML = html;
        editor.style.color = '#000';
    },
    
    /**
     * Check if editor has content (not just placeholder)
     */
    hasEditorContent = function() {
        if (!editor) return false;
        
        const content = editor.innerHTML.trim();
        const isPlaceholder = editor.style.color === 'rgb(153, 153, 153)';
        
        return content && !isPlaceholder;
    },
    
    // =============================================================================
    // EVENT HANDLERS
    // =============================================================================
    
    /**
     * Handle keyboard shortcuts in the editor
     */
    handleKeyboardShortcuts = function(e) {
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
                saveContent();
                break;
            case 'l':
                e.preventDefault();
                switchToEditor();
                break;
        }
    },
    
    /**
     * Handle paste events and convert markdown if detected
     */
    handlePasteMarkdown = function(e) {
        e.preventDefault();
        
        const pastedText = (e.clipboardData || window.clipboardData).getData('text');
        if (!pastedText) return;
        
        // Clear placeholder if present
        if (editor.style.color === 'rgb(153, 153, 153)') {
            editor.innerHTML = '';
            editor.style.color = '#000';
        }
        
        // Check if the pasted content contains markdown syntax
        if (hasMarkdownSyntax(pastedText)) {
            const htmlContent = markdownToHtml(pastedText);
            insertConvertedMarkdown(htmlContent);
            console.log('Pasted content converted from markdown to HTML');
        } else {
            insertPlainTextPaste(pastedText);
            console.log('Pasted plain text content');
        }
    },
    
    /**
     * Enhanced input handler for instant markdown conversion
     */
    handleInstantMarkdownConversion = function(e) {
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
    },
    
    /**
     * Toggle help tooltip
     */
    toggleHelp = function() {
        const tooltip = document.getElementById('helpTooltip');
        if (tooltip) {
            tooltip.style.display = tooltip.style.display === 'block' ? 'none' : 'block';
        }
    },
    
    // =============================================================================
    // UTILITY FUNCTIONS (Helper methods - keeping them internal)
    // =============================================================================
    
    insertConvertedMarkdown = function(htmlContent) {
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
    },
    
    insertPlainTextPaste = function(text) {
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
    },
    
    handleSpaceTriggeredConversions = function(textNode, text, cursorPos, e) {
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
    },
    
    handleClosingCharacterConversions = function(textNode, text, cursorPos) {
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
    },
    
    // Additional utility functions for instant conversions...
    convertToHeaderInstantly = function(level, textNode) {
        const headerElement = document.createElement(`h${level}`);
        headerElement.appendChild(document.createTextNode(''));
        
        const parentElement = findOrCreateBlockParent(textNode);
        parentElement.parentNode.insertBefore(headerElement, parentElement);
        parentElement.remove();
        
        setCursorAtEndOfElement(headerElement);
    },
    
    convertToListInstantly = function(listType, textNode) {
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
    },
    
    convertToBlockquoteInstantly = function(textNode) {
        const blockquote = document.createElement('blockquote');
        blockquote.appendChild(document.createTextNode(''));
        
        const parentElement = findOrCreateBlockParent(textNode);
        parentElement.parentNode.insertBefore(blockquote, parentElement);
        parentElement.remove();
        
        setCursorAtEndOfElement(blockquote);
    },
    
    convertToCodeBlockInstantly = function(textNode) {
        const preElement = document.createElement('pre');
        const codeElement = document.createElement('code');
        codeElement.appendChild(document.createTextNode(''));
        preElement.appendChild(codeElement);
        
        const parentElement = findOrCreateBlockParent(textNode);
        parentElement.parentNode.insertBefore(preElement, parentElement);
        parentElement.remove();
        
        setCursorAtEndOfElement(codeElement);
    },
    
    // More utility functions...
    findOrCreateBlockParent = function(textNode) {
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
    },
    
    getCurrentLineTextFromNodeToPosition = function(textNode, endPos) {
        const text = textNode.textContent;
        let lineStart = 0;
        
        for (let i = endPos - 1; i >= 0; i--) {
            if (text[i] === '\n') {
                lineStart = i + 1;
                break;
            }
        }
        
        return text.substring(lineStart, endPos);
    },
    
    isBlockElement = function(element) {
        const blockElements = ['P', 'DIV', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 
                              'UL', 'OL', 'LI', 'BLOCKQUOTE', 'PRE', 'HR'];
        return blockElements.includes(element.tagName);
    },
    
    replaceTextRangeWithElement = function(textNode, start, end, tagName, content) {
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
    },
    
    setCursorAtEndOfElement = function(element) {
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
    },
    
    setCursorAtEnd = function() {
        const range = document.createRange();
        range.selectNodeContents(editor);
        range.collapse(false);
        
        const selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(range);
    };

    // Public API - expose only the methods that should be accessible from outside
    return {
        init: init,
        
        // Converter methods
        markdownToHtml: markdownToHtml,
        htmlToMarkdown: htmlToMarkdown,
        
        // Editor control methods
        switchToEditor: switchToEditor,
        switchToSource: switchToSource,
        saveContent: saveContent,
        clearContent: clearContent,
        clearEditor: clearEditor,
        getCurrentMode: getCurrentMode,
        
        // Content access methods
        getEditorHtml: getEditorHtml,
        setEditorHtml: setEditorHtml,
        hasEditorContent: hasEditorContent,
        
        // Utility methods
        toggleHelp: toggleHelp
    };
}());

// Define public namespace alias for easier access
var imixsMarkdownEditor = IMIXS.org.imixs.workflow.markdowneditor;