# Kite IntelliJ Plugin - Development Commands

.PHONY: setup build clean run test grammar help

# Default target
help:
	@echo "Kite IntelliJ Plugin - Available commands:"
	@echo ""
	@echo "  make setup    - Clone repo with submodules (first time setup)"
	@echo "  make init     - Initialize submodules (if already cloned)"
	@echo "  make update   - Update grammar submodule to latest"
	@echo "  make grammar  - Generate ANTLR lexer and parser"
	@echo "  make build    - Build the plugin"
	@echo "  make run      - Run plugin in sandbox IDE"
	@echo "  make clean    - Clean build artifacts"
	@echo ""

# First time setup - use this when cloning
setup:
	git submodule update --init --recursive
	./gradlew generateAntlr

# Initialize submodules (if repo was cloned without --recurse-submodules)
init:
	git submodule update --init --recursive

# Update grammar submodule to latest commit
update:
	git submodule update --remote grammar
	@echo "Grammar updated. Don't forget to commit the change:"
	@echo "  git add grammar && git commit -m 'Update grammar submodule'"

# Generate ANTLR lexer and parser
grammar:
	./gradlew generateAntlr --rerun-tasks

# Build the plugin
build:
	./gradlew build

# Clean build
clean:
	./gradlew clean

# Run plugin in sandbox IDE
run:
	./gradlew runIde

# Full rebuild
rebuild: clean build
