# Imixs Markdown Editor

A lightweight WYSIWYG markdown editor for web applications that provides seamless conversion between Markdown and HTML with real-time editing capabilities.

## Overview

The Imixs Markdown Editor consists of two main components:

- **imixs-markdown-editor.js** - The main editor functionality
- **imixs-markdown-converter.js** - The markdown/HTML conversion engine

## Features

### ✨ Core Features

- **WYSIWYG Editing** - Edit markdown content visually as HTML
- **Bidirectional Conversion** - Convert between Markdown ↔ HTML seamlessly
- **Real-time Preview** - See formatted content while editing
- **Keyboard Shortcuts** - Quick formatting with standard shortcuts
- **Modular Design** - Separate editor and converter for flexibility

### 📝 Supported Markdown Elements

| Element          | Markdown Syntax       | HTML Output      |
| ---------------- | --------------------- | ---------------- |
| Headers          | `# H1` to `###### H6` | `<h1>` to `<h6>` |
| Bold             | `**bold**`            | `<strong>`       |
| Italic           | `*italic*`            | `<em>`           |
| Strikethrough    | `~~deleted~~`         | `<del>`          |
| Inline Code      | `` `code` ``          | `<code>`         |
| Code Blocks      | ` `code` `            | `<pre><code>`    |
| Links            | `[text](url)`         | `<a href="">`    |
| Images           | `![alt](url)`         | `<img>`          |
| Lists            | `* item` or `1. item` | `<ul>` / `<ol>`  |
| Blockquotes      | `> quote`             | `<blockquote>`   |
| Horizontal Rules | `---` or `***`        | `<hr>`           |

## Installation

### 1. Include JavaScript Files

```html
<head>
  <!-- Load converter first -->
  <script src="imixs-markdown-converter.js"></script>
  <!-- Then load editor -->
  <script src="imixs-markdown-editor.js"></script>
</head>
```

### 2. HTML Structure

```html
<body>
  <!-- Hidden textarea for markdown storage -->
  <textarea data-id="markdown_hidden_input" style="display: none;">
        # Your Markdown Content
        This will be converted to **WYSIWYG** format.
    </textarea
  >

  <!-- Control buttons -->
  <div class="controls">
    <button onclick="loadMarkdown()" class="primary">
      📥 Markdown → Editor
    </button>
    <button onclick="saveMarkdown()" class="primary">
      📤 Editor → Markdown
    </button>
    <button onclick="clearEditor()">🗑️ Clear</button>
  </div>

  <!-- Editor container -->
  <div id="editorjs"></div>
</body>
```

## Usage

### Basic Workflow

1. **Load Markdown**: Click "📥 Markdown → Editor" to load markdown from textarea into WYSIWYG editor
2. **Edit Content**: Use the visual editor to modify content with rich formatting
3. **Save Changes**: Click "📤 Editor → Markdown" to convert back to markdown format
4. **Clear Editor**: Use "🗑️ Clear" to empty the editor

### Keyboard Shortcuts

| Shortcut   | Action                          |
| ---------- | ------------------------------- |
| `Ctrl + B` | **Bold** selected text          |
| `Ctrl + I` | _Italic_ selected text          |
| `Ctrl + S` | Save editor content to markdown |
| `Ctrl + L` | Load markdown into editor       |

## API Reference

### Editor Functions

#### `loadMarkdown()`

Converts markdown from textarea to HTML in the editor.

#### `saveMarkdown()`

Converts editor HTML content back to markdown in textarea.

#### `clearEditor()`

Clears the editor content and shows placeholder.

#### `getEditorHtml()`

Returns the current HTML content of the editor.

```javascript
const htmlContent = getEditorHtml();
```

#### `setEditorHtml(html)`

Sets the editor content with provided HTML.

```javascript
setEditorHtml("<h1>New Content</h1><p>Updated text</p>");
```

#### `hasEditorContent()`

Checks if editor has actual content (not placeholder).

```javascript
if (hasEditorContent()) {
  console.log("Editor has content");
}
```

### Converter Functions

#### `ImixsMarkdownConverter.markdownToHtml(markdown)`

Converts markdown string to HTML.

```javascript
const html = ImixsMarkdownConverter.markdownToHtml("# Hello **World**");
// Returns: '<h1>Hello <strong>World</strong></h1>'
```

#### `ImixsMarkdownConverter.htmlToMarkdown(html)`

Converts HTML string to markdown.

```javascript
const markdown = ImixsMarkdownConverter.htmlToMarkdown(
  "<h1>Hello <strong>World</strong></h1>"
);
// Returns: '# Hello **World**'
```

#### `ImixsMarkdownConverter.escapeMarkdown(text)`

Escapes special markdown characters in text.

```javascript
const escaped = ImixsMarkdownConverter.escapeMarkdown(
  "Text with * special chars"
);
// Returns: 'Text with \\* special chars'
```

#### `ImixsMarkdownConverter.hasMarkdownSyntax(text)`

Checks if text contains markdown syntax.

```javascript
const hasMarkdown = ImixsMarkdownConverter.hasMarkdownSyntax("# Title");
// Returns: true
```

## Integration Examples

### Jakarta EE Integration

```java
// In your Jakarta EE servlet/controller
@RequestMapping("/markdown-editor")
public String showEditor(Model model) {
    String markdownContent = loadMarkdownFromDatabase();
    model.addAttribute("markdownContent", markdownContent);
    return "markdown-editor";
}
```

```html
<!-- In your JSP/Thymeleaf template -->
<textarea
  data-id="markdown_hidden_input"
  th:text="${markdownContent}"
></textarea>
```

### Form Submission

```html
<form action="/save-markdown" method="post">
  <textarea
    name="markdownContent"
    data-id="markdown_hidden_input"
    style="display: none;"
  >
  </textarea>

  <button type="button" onclick="saveMarkdown()">Update Textarea</button>
  <button type="submit">Submit Form</button>
</form>
```

## Browser Compatibility

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 12+
- ✅ Edge 79+

## Styling

The editor uses a `contenteditable` div with ID `editorjs`. You can style it with CSS:

```css
#editorjs {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
  font-family: Arial, sans-serif;
  line-height: 1.6;
}

#editorjs:focus {
  outline: 2px solid #007bff;
  outline-offset: -2px;
}
```

## Best Practices

### 1. Always Load Converter First

```html
<script src="imixs-markdown-converter.js"></script>
<script src="imixs-markdown-editor.js"></script>
```

### 2. Use Data Attributes for Textarea

```html
<textarea data-id="markdown_hidden_input">
```

### 3. Handle Form Submissions Properly

```javascript
document.getElementById("myForm").addEventListener("submit", function (e) {
  // Ensure markdown is saved before submission
  saveMarkdown();
});
```

### 4. Validate Content Before Processing

```javascript
if (hasEditorContent()) {
  saveMarkdown();
} else {
  alert("Editor is empty!");
}
```

## Troubleshooting

### Common Issues

**Q: Editor doesn't initialize**

- Ensure both JS files are loaded in correct order
- Check browser console for errors
- Verify `id="editorjs"` element exists

**Q: Markdown conversion fails**

- Ensure `ImixsMarkdownConverter` is available
- Check if textarea has `data-id="markdown_hidden_input"`
- Verify markdown syntax is supported

**Q: Keyboard shortcuts don't work**

- Ensure editor has focus
- Check if other scripts override key events
- Test in different browsers

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Changelog

### Version 1.0.0

- Initial release
- Basic markdown/HTML conversion
- WYSIWYG editing capabilities
- Keyboard shortcuts support
- Modular architecture
