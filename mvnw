#!/bin/sh
# ----------------------------------------------------------------------------
# MiniJava Optimizer Maven Wrapper Script for Unix / POSIX
# ----------------------------------------------------------------------------
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "Error: Maven executable not found on PATH." >&2
    exit 1
fi
