# Imixs Markdown Editor

A professional WYSIWYG markdown editor for Jakarta EE web applications with seamless toggle functionality between visual editing and source code view.

## Overview

The Imixs Markdown Editor provides a modern editing experience with instant markdown conversion, table support, and production-ready integration for business applications.

### Core Components

- **imixs-office-markdowneditor.js** - Main editor with toggle functionality and instant conversion
- **imixs-theme-markdown.css** - Professional styling with toolbar integration

## Key Features

### Instant Conversion System

- **Space-triggered conversion**: Type `# ` for headers, `* ` for lists
- **Real-time formatting**: Bold, italic, and code conversion while typing
- **Block elements**: Headers, lists, blockquotes, code blocks

### Toggle Interface

- **WYSIWYG Mode**: Visual editing with rich formatting
- **Source Mode**: Direct markdown editing with syntax highlighting
- **Auto-sync**: Content automatically synchronized between modes
- **Toolbar Integration**: Professional FontAwesome icon controls

### Enhanced Markdown Support

- **GitHub Flavored Markdown**: Tables, strikethrough, task lists
- **Robust Conversion**: Powered by Marked.js and Turndown.js
- **Smart Paste**: Automatic detection and conversion of pasted markdown
- **Extended Syntax**: All CommonMark features plus GFM extensions

## Dependencies

### Required Libraries

```html
<!-- Markdown processing -->
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>

<!-- HTML to Markdown conversion -->
<script src="https://unpkg.com/turndown/dist/turndown.js"></script>
<script src="https://unpkg.com/turndown-plugin-gfm/dist/turndown-plugin-gfm.js"></script>

<!-- FontAwesome for toolbar icons -->
<link
  rel="stylesheet"
  href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
/>
```

## Installation

### 1. Include Required Files

```html
<head>
  <!-- Dependencies (load first) -->
  <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
  <script src="https://unpkg.com/turndown/dist/turndown.js"></script>
  <script src="https://unpkg.com/turndown-plugin-gfm/dist/turndown-plugin-gfm.js"></script>
  <link
    rel="stylesheet"
    href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
  />

  <!-- Imixs Markdown Editor -->
  <link rel="stylesheet" href="office-theme-markdown.css" />
  <script src="imixs-office-.markdownediotr.js"></script>
</head>
```

### 2. HTML Structure

```html
<body>
  <!-- Editor Header with Toolbar -->
  <div class="editor-header">
    <span class="editor-title">Content Editor</span>
    <div class="editor-toolbar">
      <div
        class="toolbar-icon active"
        onclick="switchToEditor()"
        id="editorToggle"
        title="WYSIWYG Editor"
      >
        <i class="fas fa-edit"></i>
      </div>
      <div
        class="toolbar-icon"
        onclick="switchToSource()"
        id="sourceToggle"
        title="Markdown Source"
      >
        <i class="fas fa-code"></i>
      </div>
      <div class="toolbar-separator"></div>
      <div class="toolbar-icon" onclick="saveContent()" title="Save Content">
        <i class="fas fa-save"></i>
      </div>
      <div class="toolbar-icon" onclick="clearContent()" title="Clear All">
        <i class="fas fa-trash"></i>
      </div>
    </div>
  </div>

  <!-- Editor Container -->
  <div class="editor-container">
    <div id="editorjs"></div>
    <textarea
      data-id="markdown_data"
      placeholder="Enter your markdown here..."
    ></textarea>
  </div>
</body>
```

## API Reference

### Toggle Functions

#### `switchToEditor()`

Switches to WYSIWYG editor mode and syncs content from textarea.

#### `switchToSource()`

Switches to markdown source mode and syncs content from editor.

#### `saveContent()`

Saves current content and triggers 'imixs-markdown-saved' event.

#### `clearContent()`

Clears both editor and textarea content.

#### `getCurrentMode()`

Returns current mode: 'editor' or 'source'.

#### `getEditorHtml()` / `setEditorHtml(html)`

Get/set editor HTML content directly.

### Converter API

#### `ImixsMarkdownConverter.markdownToHtml(markdown)`

Converts markdown to HTML using Marked.js with GFM support.

#### `ImixsMarkdownConverter.htmlToMarkdown(html)`

Converts HTML to markdown using Turndown.js with table support.

## Supported Markdown Elements

| Element       | Syntax                | Features                               |
| ------------- | --------------------- | -------------------------------------- |
| Headers       | `# H1` to `###### H6` | Instant conversion after space         |
| Bold          | `**bold**`            | Real-time conversion after closing `*` |
| Italic        | `*italic*`            | Real-time conversion after closing `*` |
| Code          | `` `code` ``          | Inline and block code support          |
| Lists         | `* item`, `1. item`   | Instant conversion, nested support     |
| Tables        | `\| col \| col \|`    | Full GFM table support                 |
| Links         | `[text](url)`         | Auto-detection on paste                |
| Images        | `![alt](url)`         | Drag & drop support                    |
| Blockquotes   | `> quote`             | Instant conversion after space         |
| Strikethrough | `~~text~~`            | GFM extension support                  |

## Jakarta EE Integration

### JSF Form Integration

```html
<!-- JSF Form -->
<h:form id="contentForm">
  <!-- Editor HTML structure here -->

  <h:inputTextarea
    id="markdownContent"
    value="#{contentBean.markdownText}"
    data-id="markdown_data"
    style="display: none;"
  />

  <h:commandButton
    value="Save Content"
    action="#{contentBean.saveContent}"
    onclick="saveContent(); return true;"
  />
</h:form>
```

### Bean Integration

```java
@Named
@ViewScoped
public class ContentBean implements Serializable {

    private String markdownText;

    public void saveContent() {
        // markdownText automatically updated via form submission
        // Process the markdown content
        processMarkdown(markdownText);
    }

    // Getters and setters
}
```

## Keyboard Shortcuts

| Shortcut   | Action               |
| ---------- | -------------------- |
| `Ctrl + B` | Bold selected text   |
| `Ctrl + I` | Italic selected text |
| `Ctrl + S` | Save content         |
| `Ctrl + L` | Load content         |

## Events

### Custom Events

```javascript
// Listen for save events
document.addEventListener("imixs-markdown-saved", function (e) {
  console.log("Content saved in mode:", e.detail.mode);
});

// Listen for clear events
document.addEventListener("imixs-markdown-cleared", function (e) {
  console.log("Content cleared");
});
```

## Configuration

### Data Attribute

The editor looks for a textarea with `data-id="markdown_data"`. This can be customized by modifying the selector in the JavaScript files.

### Auto-initialization

The editor automatically:

1. Initializes on DOM ready
2. Loads content from textarea
3. Starts in WYSIWYG mode
4. Enables auto-sync between modes

## Browser Support

- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## Troubleshooting

### Common Issues

**Editor not initializing**

- Check console for missing dependencies
- Verify script loading order
- Ensure `id="editorjs"` element exists

**Toggle not working**

- Verify toolbar HTML structure
- Check for `data-id="markdown_data"` on textarea
- Ensure FontAwesome is loaded

**Content not syncing**

- Check browser console for JavaScript errors
- Verify ImixsMarkdownConverter is available
- Test auto-sync by switching modes

## License

MIT License - Open source and free for commercial use.

## Contributing

This editor was developed through iterative refinement focusing on:

- Professional UX for business applications
- Robust markdown processing with table support
- Clean, maintainable code architecture
- Jakarta EE integration patterns

Contributions welcome for additional features or improvements.
