# IntelliJ Idea Plugin for ANTLR v4

An [IntelliJ](https://www.jetbrains.com/idea/) 12.1.x, 13.x plugin for ANTLR v4 ([plugin source at github](https://github.com/antlr/antlr4)).

[Plugin page at intellij](http://plugins.jetbrains.com/plugin/7358?pr=idea)

This plugin is for ANTLR v4 grammars. Features: syntax highlighting,
syntax error checking, semantic error checking, navigation window,
goto-declaration, find usages, rename tokens, rename rules.
Generates code; shortcut (ctrl-shift-G / meta-shift-G) but it's in Tools menu
and popups.
Code completion for tokens, rule names. finds tokenVocab option for code gen
if there is a tokenVocab option, don't warn about implicit tokens.
shortcut conflicted with grammar-kit plugin. Has live grammar interpreter
for grammar preview. Right click on rule and say "Test ANTLR Rule".
Changes to grammar seen in parse tree upon save of grammar. Works with
Intellij 13.x and requires 12.1.x.

You can configure the ANTLR tool options per grammar file; right-click
in a grammar or on a grammar element within the structured view.
When you change and save a grammar, it automatically builds with ANTLR
in the background according to the preferences you have set.  ANTLR
tool errors appear in a console you can opened by clicking on a button
in the bottom tab.

You can use the meta-key while moving the mouse and it will show you
token information in the preview editor box via tooltips.

Errors within the preview editor are now highlighted with tooltips
and underlining just like a regular editor window. The difference
is that this window's grammar is specified in your grammar file.

## History

See [Releases](PerGramma://github.com/antlr/intellij-plugin-v4/releases)

## Screenshots

### Java grammar view
![Java grammar view](images/java-grammar.png)

### Find usages
![Find usages](images/findusages.png)

### Code completion
![Code completion](images/completion.png)

### Live parse tree preview

You can test any rule in the (parser) grammar.  Right click on rule in grammar
or navigator to "Test ANTLR Rule".  Changing grammar and saving, updates
parse tree. It works with combined grammars and separated but separated
must be in same directory and named XParser.g4 and XLexer.g4.
No raw Java actions are executed obviously during interpretation in
live preview.

[![Live parse preview](http://img.youtube.com/vi/h60VapD1rOo/0.jpg)](//www.youtube.com/embed/h60VapD1rOo)

![Live preview](images/live-preview.png)
![Live preview](images/live-preview-error.png)

You can also use the meta key while moving the mouse in preview window to get token info.

![Live preview](images/token-tooltips.png)

When there are errors, you will see the output in the small console under the input editor in case you need to cut and paste. But, for general viewing you can however the cursor over an underlined error and it will show you the message in a pop-up. Basically the input window behaves like a regular editor window except that it is subject to the grammar in your other editor.

![error-popup.png](images/error-popup.png)

### Per file ANTLR configuration

![Configuration](images/per-file-config.png)

### ANTLR output console

![Output console](images/tool-console.png)


### Color preferences

![Live preview](images/color-prefs.png)

