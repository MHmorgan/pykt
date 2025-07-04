package dev.hirth.pykt.sexp

/**
 * Parses S-expressions from tokens with support for variable definitions and references.
 */
internal class SexpParserWithVariables(private val tokens: List<SexpToken>) {
    private var position = 0
    private val symbolTable = mutableMapOf<String, Sexp>()

    fun parse(): List<Sexp> {
        // First pass: parse all S-expressions
        val allExpressions = mutableListOf<Sexp>()
        while (position < tokens.size && tokens[position] != SexpToken.EOF) {
            allExpressions.add(parseExpression())
        }

        // Second pass: process define statements and build symbol table
        val nonDefineExpressions = mutableListOf<Sexp>()
        for (expr in allExpressions) {
            if (isDefineStatement(expr)) {
                processDefineStatement(expr)
            } else {
                nonDefineExpressions.add(expr)
            }
        }

        // Third pass: resolve variables in non-define expressions
        return nonDefineExpressions.map { resolveVariables(it) }
    }

    fun parseOne(): Sexp {
        val results = parse()
        
        return if (results.isEmpty()) {
            throw SexpParseException("No expressions found after processing define statements")
        } else {
            // Always wrap results in a list when using variable parsing,
            // since we're typically parsing multi-expression input
            Sexp.List(results)
        }
    }

    private fun parseExpression(): Sexp {
        when (val token = tokens[position]) {
            is SexpToken.LeftParen -> {
                return parseList()
            }

            is SexpToken.Atom -> {
                position++
                return Sexp.Atom(token.value)
            }

            is SexpToken.RightParen -> {
                throw SexpParseException("Unexpected ')' at position $position")
            }

            is SexpToken.EOF -> {
                throw SexpParseException("Unexpected end of input")
            }
        }
    }

    private fun parseList(): Sexp.List {
        if (tokens[position] != SexpToken.LeftParen) {
            throw SexpParseException("Expected '(' at position $position")
        }

        position++ // Skip '('
        val elements = mutableListOf<Sexp>()

        while (position < tokens.size && tokens[position] != SexpToken.RightParen) {
            if (tokens[position] == SexpToken.EOF) {
                throw SexpParseException("Unexpected end of input, expected ')'")
            }
            elements.add(parseExpression())
        }

        if (position >= tokens.size || tokens[position] != SexpToken.RightParen) {
            throw SexpParseException("Expected ')' at position $position")
        }

        position++ // Skip ')'
        return Sexp.List(elements)
    }

    private fun isDefineStatement(expr: Sexp): Boolean {
        return expr is Sexp.List && 
               expr.elements.isNotEmpty() && 
               expr.elements[0] is Sexp.Atom && 
               (expr.elements[0] as Sexp.Atom).value == "define"
    }

    private fun processDefineStatement(expr: Sexp) {
        val list = expr as Sexp.List
        if (list.elements.size != 3) {
            throw SexpParseException("Define statement must have exactly 3 elements: (define name value)")
        }

        val nameElement = list.elements[1]
        if (nameElement !is Sexp.Atom) {
            throw SexpParseException("Define statement variable name must be an atom")
        }

        val valueElement = list.elements[2]
        // Recursively resolve variables in the value at definition time
        val resolvedValue = resolveVariables(valueElement)
        symbolTable[nameElement.value] = resolvedValue
    }

    private fun resolveVariables(expr: Sexp): Sexp {
        return when (expr) {
            is Sexp.Atom -> {
                symbolTable[expr.value] ?: expr
            }
            is Sexp.List -> {
                if (expr.elements.isEmpty()) {
                    expr
                } else {
                    // Don't resolve variables in the first element (operator/key position)
                    val first = expr.elements.first()
                    val rest = expr.elements.drop(1).map { resolveVariables(it) }
                    Sexp.List(listOf(first) + rest)
                }
            }
        }
    }
}

/**
 * Parses S-expressions from tokens.
 */
internal class SexpParser(private val tokens: List<SexpToken>) {
    private var position = 0

    fun parse(): List<Sexp> {
        val results = mutableListOf<Sexp>()

        while (position < tokens.size && tokens[position] != SexpToken.EOF) {
            results.add(parseExpression())
        }

        return results
    }

    fun parseOne(): Sexp {
        if (position >= tokens.size || tokens[position] == SexpToken.EOF) {
            throw SexpParseException("Unexpected end of input")
        }

        val result = parseExpression()

        // Check for trailing content
        if (position < tokens.size && tokens[position] != SexpToken.EOF) {
            throw SexpParseException("Unexpected content after S-expression")
        }

        return result
    }

    private fun parseExpression(): Sexp {
        when (val token = tokens[position]) {
            is SexpToken.LeftParen -> {
                return parseList()
            }

            is SexpToken.Atom -> {
                position++
                return Sexp.Atom(token.value)
            }

            is SexpToken.RightParen -> {
                throw SexpParseException("Unexpected ')' at position $position")
            }

            is SexpToken.EOF -> {
                throw SexpParseException("Unexpected end of input")
            }
        }
    }

    private fun parseList(): Sexp.List {
        if (tokens[position] != SexpToken.LeftParen) {
            throw SexpParseException("Expected '(' at position $position")
        }

        position++ // Skip '('
        val elements = mutableListOf<Sexp>()

        while (position < tokens.size && tokens[position] != SexpToken.RightParen) {
            if (tokens[position] == SexpToken.EOF) {
                throw SexpParseException("Unexpected end of input, expected ')'")
            }
            elements.add(parseExpression())
        }

        if (position >= tokens.size || tokens[position] != SexpToken.RightParen) {
            throw SexpParseException("Expected ')' at position $position")
        }

        position++ // Skip ')'
        return Sexp.List(elements)
    }
}

/**
 * Parses S-expressions from a string.
 */
class SexpReader {

    /**
     * Parses a single S-expression from the input string.
     *
     * @param input The input string containing the S-expression
     * @return The parsed S-expression
     * @throws SexpParseException if parsing fails
     */
    fun readOne(input: String): Sexp {
        val tokens = SexpTokenizer(input).tokenize()
        return SexpParser(tokens).parseOne()
    }

    /**
     * Parses multiple S-expressions from the input string.
     *
     * @param input The input string containing S-expressions
     * @return A list of parsed S-expressions
     * @throws SexpParseException if parsing fails
     */
    fun readMany(input: String): List<Sexp> {
        val tokens = SexpTokenizer(input).tokenize()
        return SexpParser(tokens).parse()
    }

    /**
     * Parses S-expressions from the input string, calling the callback for each one.
     *
     * @param input The input string containing S-expressions
     * @param callback Function called for each parsed S-expression
     * @throws SexpParseException if parsing fails
     */
    fun readWithCallback(input: String, callback: (Sexp) -> Unit) {
        readMany(input).forEach(callback)
    }

    /**
     * Parses a single S-expression from the input string with variable support.
     *
     * @param input The input string containing the S-expression
     * @return The parsed S-expression with variables resolved
     * @throws SexpParseException if parsing fails
     */
    fun readOneWithVariables(input: String): Sexp {
        val tokens = SexpTokenizer(input).tokenize()
        return SexpParserWithVariables(tokens).parseOne()
    }

    /**
     * Parses multiple S-expressions from the input string with variable support.
     *
     * @param input The input string containing S-expressions
     * @return A list of parsed S-expressions with variables resolved
     * @throws SexpParseException if parsing fails
     */
    fun readManyWithVariables(input: String): List<Sexp> {
        val tokens = SexpTokenizer(input).tokenize()
        return SexpParserWithVariables(tokens).parse()
    }

    /**
     * Parses S-expressions from the input string with variable support, calling the callback for each one.
     *
     * @param input The input string containing S-expressions
     * @param callback Function called for each parsed S-expression
     * @throws SexpParseException if parsing fails
     */
    fun readWithVariablesAndCallback(input: String, callback: (Sexp) -> Unit) {
        readManyWithVariables(input).forEach(callback)
    }
}

/**
 * Default S-expression reader instance.
 */
val sexpReader = SexpReader()