package dev.hirth.pykt.sexp

/**
 * Represents an S-expression, which can be either an atom or a list.
 * 
 * S-expressions are a notation for representing nested list data,
 * commonly used in Lisp-like languages and configuration files.
 */
sealed class Sexp {
    
    /**
     * An atomic S-expression containing a string value.
     */
    data class Atom(val value: String) : Sexp() {
        override fun toString(): String = when {
            needsQuotes(value) -> "\"${value.replace("\"", "\\\"")}\""
            else -> value
        }
        
        private fun needsQuotes(s: String): Boolean {
            return s.isEmpty() || 
                   s.any { it.isWhitespace() || it in "()\"" } ||
                   s.startsWith(';') // Comments
        }
    }
    
    /**
     * A list S-expression containing zero or more nested S-expressions.
     */
    data class List(val elements: kotlin.collections.List<Sexp>) : Sexp() {
        constructor(vararg elements: Sexp) : this(elements.toList())
        
        override fun toString(): String = 
            "(${elements.joinToString(" ")})"
    }
    
    companion object {
        /**
         * Creates an atom S-expression from a string.
         */
        fun atom(value: String): Atom = Atom(value)
        
        /**
         * Creates a list S-expression from elements.
         */
        fun list(vararg elements: Sexp): List = List(elements.toList())
        
        /**
         * Creates a list S-expression from a collection.
         */
        fun list(elements: Collection<Sexp>): List = List(elements.toList())
    }
}

/**
 * Exception thrown when S-expression parsing fails.
 */
class SexpParseException(message: String, val position: Int = -1, cause: Throwable? = null) 
    : Exception(message, cause)