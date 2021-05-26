# intellij-codeowners

![Build](https://github.com/fan-tom/intellij-codeowners/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16811.svg)](https://plugins.jetbrains.com/plugin/16811)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16811.svg)](https://plugins.jetbrains.com/plugin/16811)

Introduction
------------

<!-- Plugin description -->

**CODEOWNERS** is a plugin for CODEOWNERS files in your project.

Features:
---------

- Files syntax highlight (lexical)
- Show owner of currently opened file in IDE status bar
- Group file changes by owners
- Comments support
- Navigation to entries in Project view
- Navigation to Github user/team by ctrl-click on owner
- Navigate from status bar to the line in CODEOWNERS file to know where code ownership is assigned

TODO:
-----
- Proper syntax-aware highlighting
- GoTo team declaration in [Bitbucket][bitbucket-syntax] files
- Entries inspection (duplicated, covered, unused, incorrect syntax) with quick-fix actions
- Support spaces in file paths
- Tests

Supported syntaxes:
- [Github][github-syntax]
- [Bitbucket][bitbucket-syntax]

[github-syntax]: https://docs.github.com/en/github/creating-cloning-and-archiving-repositories/about-code-owners#codeowners-syntax
[bitbucket-syntax]: https://mibexsoftware.atlassian.net/wiki/spaces/CODEOWNERS/pages/222822413/Usage

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "CODEOWNERS"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/fan-tom/intellij-codeowners/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


