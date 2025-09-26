/**
 * Imixs Markdown Converter v2.0
 * Utility functions to convert between Markdown and HTML
 * 
 * Fixed Issues:
 * - Corrected list processing order
 * - Proper unordered/ordered list recognition
 * - Improved cursor stability during conversions
 * - Better handling of nested structures
 */

const ImixsMarkdownConverter = {
    
    /**
     * Convert markdown text to HTML
     * @param {string} markdown - The markdown text
     * @returns {string} - The converted HTML
     */
    markdownToHtml: function(markdown) {
        let html = markdown;
         
        // Headers (h1-h6) - process from h6 to h1 to avoid conflicts
        html = html.replace(/^###### (.*$)/gm, '<h6>$1</h6>');
        html = html.replace(/^##### (.*$)/gm, '<h5>$1</h5>');
        html = html.replace(/^#### (.*$)/gm, '<h4>$1</h4>');
        html = html.replace(/^### (.*$)/gm, '<h3>$1</h3>');
        html = html.replace(/^## (.*$)/gm, '<h2>$1</h2>');
        html = html.replace(/^# (.*$)/gm, '<h1>$1</h1>');
        
        // Bold text
        html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        
        // Italic text  
        html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
        
        // Code blocks (triple backticks)
        html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
        
        // Inline code
        html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
        
        // Links
        html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
        
        // Images
        html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1">');
        
        // FIXED: Process lists with proper type detection
        html = this._convertLists(html);
        
        // Blockquotes
        html = html.replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>');
        
        // Horizontal rules
        html = html.replace(/^---$/gm, '<hr>');
        html = html.replace(/^\*\*\*$/gm, '<hr>');
        
        // Strikethrough
        html = html.replace(/~~(.*?)~~/g, '<del>$1</del>');
        
        // Line breaks and paragraphs
        html = html.split('\n\n').map(paragraph => {
            paragraph = paragraph.trim();
            if (!paragraph) return '';
            
            // Skip if already wrapped in HTML block tags
            if (paragraph.match(/^<(h[1-6]|ul|ol|blockquote|pre|hr|div)/)) {
                return paragraph;
            }
            
            // Wrap in paragraph tags
            return '<p>' + paragraph.replace(/\n/g, '<br>') + '</p>';
        }).filter(p => p).join('\n\n');
        
        return html;
    },
    
    /**
     * Fixed list conversion - processes lists line by line
     * @param {string} html - HTML content with markdown list syntax
     * @returns {string} - HTML with properly converted lists
     */
    _convertLists: function(html) {
        const lines = html.split('\n');
        const result = [];
        let i = 0;
        
        while (i < lines.length) {
            const line = lines[i].trim();
            
            // Skip empty lines
            if (!line) {
                result.push(lines[i]);
                i++;
                continue;
            }
            
            // Check for unordered list item: * or -
            const unorderedMatch = line.match(/^[\*\-] (.+)$/);
            if (unorderedMatch) {
                const [listHtml, nextIndex] = this._processListBlock(lines, i, 'ul', /^[\*\-] (.+)$/);
                result.push(listHtml);
                i = nextIndex;
                continue;
            }
            
            // Check for ordered list item: 1. 2. etc.
            const orderedMatch = line.match(/^\d+\. (.+)$/);
            if (orderedMatch) {
                const [listHtml, nextIndex] = this._processListBlock(lines, i, 'ol', /^\d+\. (.+)$/);
                result.push(listHtml);
                i = nextIndex;
                continue;
            }
            
            // Regular line, not a list
            result.push(lines[i]);
            i++;
        }
        
        return result.join('\n');
    },
    
    /**
     * Process a complete list block (multiple consecutive list items)
     * @param {string[]} lines - All lines
     * @param {number} startIndex - Starting line index
     * @param {string} listType - 'ul' or 'ol'
     * @param {RegExp} pattern - Pattern to match list items
     * @returns {[string, number]} - [Generated HTML, next index to process]
     */
    _processListBlock: function(lines, startIndex, listType, pattern) {
        const listItems = [];
        let currentIndex = startIndex;
        
        // Collect all consecutive list items
        while (currentIndex < lines.length) {
            const line = lines[currentIndex].trim();
            
            // Skip empty lines within list
            if (!line) {
                currentIndex++;
                continue;
            }
            
            const match = line.match(pattern);
            if (!match) {
                // Not a list item, end of list
                break;
            }
            
            const content = match[1];
            listItems.push(`  <li>${content}</li>`);
            currentIndex++;
        }
        
        // Generate the complete list HTML
        const listHtml = `<${listType}>\n${listItems.join('\n')}\n</${listType}>`;
        
        return [listHtml, currentIndex];
    },
    
    /**
     * Convert HTML back to markdown
     * @param {string} html - The HTML content
     * @returns {string} - The converted markdown
     */
    htmlToMarkdown: function(html) {
        // Create a temporary DOM element to properly parse HTML structure
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html.trim();
        
        let markdown = '';
        
        // Process each child node separately to maintain structure
        for (let node of tempDiv.childNodes) {
            markdown += this._convertNodeToMarkdown(node) + '\n\n';
        }
        
        // Clean up extra newlines and whitespace
        markdown = markdown.replace(/\n{3,}/g, '\n\n');
        markdown = markdown.replace(/^\s+|\s+$/g, '');
        
        return markdown;
    },
    
    /**
     * Convert a single DOM node to markdown
     * @param {Node} node - The DOM node to convert
     * @returns {string} - The converted markdown
     */
    _convertNodeToMarkdown: function(node) {
        if (node.nodeType === Node.TEXT_NODE) {
            return node.textContent.trim();
        }
        
        if (node.nodeType !== Node.ELEMENT_NODE) {
            return '';
        }
        
        const tagName = node.tagName.toLowerCase();
        const textContent = this._getTextContent(node);
        
        switch (tagName) {
            case 'h1':
                return `# ${textContent}`;
            case 'h2':
                return `## ${textContent}`;
            case 'h3':
                return `### ${textContent}`;
            case 'h4':
                return `#### ${textContent}`;
            case 'h5':
                return `##### ${textContent}`;
            case 'h6':
                return `###### ${textContent}`;
                
            case 'p':
                if (textContent.trim() === '' || textContent === '\u00A0') {
                    return ''; // Skip empty paragraphs
                }
                return this._convertInlineElements(node);
                
            case 'strong':
            case 'b':
                return `**${textContent}**`;
                
            case 'em':
            case 'i':
                return `*${textContent}*`;
                
            case 'del':
            case 'strike':
                return `~~${textContent}~~`;
                
            case 'code':
                return `\`${textContent}\``;
                
            case 'pre':
                const codeElement = node.querySelector('code');
                if (codeElement) {
                    return `\`\`\`\n${codeElement.textContent}\n\`\`\``;
                }
                return `\`\`\`\n${textContent}\n\`\`\``;
                
            case 'a':
                const href = node.getAttribute('href') || '';
                return `[${textContent}](${href})`;
                
            case 'img':
                const src = node.getAttribute('src') || '';
                const alt = node.getAttribute('alt') || '';
                return `![${alt}](${src})`;
                
            case 'ul':
                return this._convertListToMarkdown(node, '*');
                
            case 'ol':
                return this._convertListToMarkdown(node, '1.');
                
            case 'li':
                return this._convertInlineElements(node);
                
            case 'blockquote':
                return `> ${textContent}`;
                
            case 'hr':
                return '---';
                
            case 'br':
                return '\n';
                
            case 'div':
                // Process div children
                let divContent = '';
                for (let child of node.childNodes) {
                    const childMarkdown = this._convertNodeToMarkdown(child);
                    if (childMarkdown) {
                        divContent += childMarkdown + '\n\n';
                    }
                }
                return divContent.replace(/\n\n$/, '');
                
            default:
                // For unknown elements, process their children
                return this._convertInlineElements(node);
        }
    },
    
    /**
     * Convert inline elements within a parent element
     * @param {Element} element - The parent element
     * @returns {string} - The converted markdown
     */
    _convertInlineElements: function(element) {
        let result = '';
        
        for (let node of element.childNodes) {
            if (node.nodeType === Node.TEXT_NODE) {
                result += node.textContent;
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                const tagName = node.tagName.toLowerCase();
                const textContent = this._getTextContent(node);
                
                switch (tagName) {
                    case 'strong':
                    case 'b':
                        result += `**${textContent}**`;
                        break;
                    case 'em':
                    case 'i':
                        result += `*${textContent}*`;
                        break;
                    case 'del':
                    case 'strike':
                        result += `~~${textContent}~~`;
                        break;
                    case 'code':
                        result += `\`${textContent}\``;
                        break;
                    case 'a':
                        const href = node.getAttribute('href') || '';
                        result += `[${textContent}](${href})`;
                        break;
                    case 'br':
                        result += '\n';
                        break;
                    default:
                        result += textContent;
                }
            }
        }
        
        return result;
    },
    
    /**
     * Convert list element to markdown (IMPROVED)
     * @param {Element} listElement - The ul or ol element
     * @param {string} marker - List marker (* or 1.)
     * @returns {string} - The converted markdown
     */
    _convertListToMarkdown: function(listElement, marker) {
        let result = '';
        const listItems = listElement.querySelectorAll('li');
        
        listItems.forEach((li, index) => {
            const itemText = this._convertInlineElements(li);
            if (itemText.trim()) {
                const actualMarker = marker === '1.' ? `${index + 1}.` : marker;
                result += `${actualMarker} ${itemText}\n`;
            }
        });
        
        return result.replace(/\n$/, ''); // Remove trailing newline
    },
    
    /**
     * Get text content from element, handling nested elements properly
     * @param {Element} element - The element
     * @returns {string} - The text content
     */
    _getTextContent: function(element) {
        return element.textContent || element.innerText || '';
    },
    
    /**
     * Escape special markdown characters in text
     * @param {string} text - The text to escape
     * @returns {string} - The escaped text
     */
    escapeMarkdown: function(text) {
        return text.replace(/([\\`*_{}[\]()#+-.!])/g, '\\$1');
    },
    
    /**
     * Unescape markdown characters
     * @param {string} text - The text to unescape
     * @returns {string} - The unescaped text
     */
    unescapeMarkdown: function(text) {
        return text.replace(/\\([\\`*_{}[\]()#+-.!])/g, '$1');
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
            /!\[.*?\]\(.*?\)/   // Images
        ];
        
        return markdownPatterns.some(pattern => pattern.test(text));
    }
};