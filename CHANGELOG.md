<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-codeowners Changelog

## [Unreleased]
### Added
- Pattern overlap inspection: detect patterns that override other patterns earlier in file
- Support IDEA 2022.3

### Fixed
- Order of grouping by code owner and by file in search results

### Removed
- Support for IDEA versions older than 2022.3


## [v0.4.1](https://github.com/fan-tom/intellij-codeowners/tree/v0.4.1) (2022-11-03)
### Fixed
- Resolving files from file patterns when CODEOWNERS file not in the repository root
- Proper translation of file patterns starting with `**/` into regex on pattern cache cleanup

## [v0.4.0](https://github.com/fan-tom/intellij-codeowners/tree/v0.4.0) (2022-09-15)
### Added
- Support of file paths with spaces and `@` for GitHub syntax

### Fixed
- Incorrect parsing of paths without owners (reset ownership) for GitHub syntax
- Resolving files from file patterns

### Removed
- Support for IDEA versions older than 2022.1

## [v0.3.5](https://github.com/fan-tom/intellij-codeowners/tree/v0.3.5) (2022-07-05)
### Added
- Support codeowners unsetting for Github files, see https://github.community/t/codeowners-file-with-a-not-file-type-condition/1423/9
- Support IDEA 2022.2

### Fixed
- Speedup file references resolution (navigation through file tree using CTRL-click on CODEOWNERS file paths parts)

## [v0.3.4](https://github.com/fan-tom/intellij-codeowners/tree/v0.3.4) (2022-03-29)
### Added
- Structure view for Bitbucket files
- Comment/uncomment actions

## [v0.3.3](https://github.com/fan-tom/intellij-codeowners/tree/v0.3.3) (2022-03-20)
### Added
- GoTo Team declaration for BitBucket files
- Support IDEA 2022.1

## [v0.3.2](https://github.com/fan-tom/intellij-codeowners/tree/v0.3.2) (2021-12-13)
### Added
- Support `docs`, `.github`, `.bitibucket` dirs as CODEOWNERS file locations
- Support bitbucket config lines

## [v0.3.1](https://github.com/fan-tom/intellij-codeowners/tree/v0.3.1) (2021-11-25)
### Fixed
- Bitbucket filetype detection

## [v0.3.0](https://github.com/fan-tom/intellij-codeowners/tree/v0.3.0) (2021-08-09)
### Added
- Group by owner in usage find results

## [v0.2.1](https://github.com/fan-tom/intellij-codeowners/tree/v0.2.1) (2021-08-01)
### Fixed
- Support IDEA 2021.2

## [v0.2.0](https://github.com/fan-tom/intellij-codeowners/tree/v0.2.0) (2021-05-25)
### Added
- Navigate from status bar to the line in CODEOWNERS file to know where code ownership is assigned

## [v0.1.0-eap.1](https://github.com/fan-tom/intellij-codeowners/tree/v0.1.0) (2021-05-24)
### Added
- Files syntax highlight (lexical)
- Show owner of currently opened file in IDE status bar
- Group file changes by owners
- Navigation to entries in Project view
- Navigation to Github user/team by ctrl-click on owner