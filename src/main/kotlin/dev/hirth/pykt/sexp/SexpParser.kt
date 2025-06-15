package dev.hirth.pykt.sexp

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
}

/**
 * Default S-expression reader instance.
 */
val sexpReader = SexpReader()