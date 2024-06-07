# Markdown

The markdown component allows to edit and display Markdown formatted text. 

The item part `markdown` can be used in edit and read mode (display mode): 

```xml
   <item name="invoice.summary" 
      type="custom" 
      path="markdown"
      label="Summary" />
```

<img class="screenshot" src="markdown-01.png" />

In case `readonly` is set to `true` the component displays the content as HTML-

```xml
      <item type="custom" 
            path="spaceref"  
            label="Department:" 
            readonly="true" />
```

<img class="screenshot" src="markdown-02.png" />



The component is based on the [Marked Java Script Library](https://github.com/markedjs/marked).