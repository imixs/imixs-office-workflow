/**
 * Enhanced Markdown Converter v3.0
 * Uses Marked.js + Turndown.js in controlled environment
 * No fallbacks needed - libraries are guaranteed to be available
 */

const ImixsMarkdownConverter = {
    
    // Turndown service instance
    turndownService: null,
    
    /**
     * Initialize the converter services
     */
    init: function() {
        // Configure Turndown for consistent output
        this.turndownService = new TurndownService({
            headingStyle: 'atx',        // Use # for headings
            hr: '---',                  // Use --- for horizontal rules
            bulletListMarker: '*',      // Use * for unordered lists
            codeBlockStyle: 'fenced',   // Use ``` for code blocks
            fence: '```',               // Code fence style
            emDelimiter: '*',           // Use * for italic
            strongDelimiter: '**',      // Use ** for bold
            linkStyle: 'inlined'        // Use [text](url) style
        });
        
        // Add GFM table support
        this.turndownService.use(turndownPluginGfm.tables);
        
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
    },
    
    /**
     * Convert markdown text to HTML using marked.js
     * @param {string} markdown - The markdown text
     * @returns {string} - The converted HTML
     */
    markdownToHtml: function(markdown) {
        return marked.parse(markdown);
    },
    
    /**
     * Convert HTML back to markdown using turndown.js
     * @param {string} html - The HTML content
     * @returns {string} - The converted markdown
     */
    htmlToMarkdown: function(html) {
        return this.turndownService.turndown(html);
    },
    
    /**
     * Check if text contains markdown syntax
     * @param {string} text - The text to check
     * @returns {boolean} - True if markdown syntax is detected
     */
    hasMarkdownSyntax: function(text) {
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
     * @param {string} text - The text to escape
     * @returns {string} - The escaped text
     */
    escapeMarkdown: function(text) {
        return text.replace(/([\\`*_{}[\]()#+-.!|])/g, '\\$1');
    },
    
    /**
     * Unescape markdown characters
     * @param {string} text - The text to unescape
     * @returns {string} - The unescaped text
     */
    unescapeMarkdown: function(text) {
        return text.replace(/\\([\\`*_{}[\]()#+-.!|])/g, '$1');
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    ImixsMarkdownConverter.init();
});