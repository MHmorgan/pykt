AI Assistant Guidelines
=======================

This file contains guidelines for the AI assistant to follow when working on this project.

Explain your actions and decisions clearly and always provide the reasoning behind your choices.

General Guidelines
------------------

- Local variable names should follow these conventions:
    - Use common abbreviations.
    - Prefer short, concise one-word names.
    - Use longer descriptive names if it is required for clarity.
    - If a variable is used far from its declaration, a descriptive name should be used.
    - Don't use names which overlap with generic extension functions from the Kotlin standard library.
- Avoid complicated function calls (e.g., `foo(bar(baz()))`), use intermediate variables to clarify intent.
- All methods should be documented with KDoc comments, explaining their purpose, usage and pitfalls (if any).
- Keep comments in the code to a minimum.
