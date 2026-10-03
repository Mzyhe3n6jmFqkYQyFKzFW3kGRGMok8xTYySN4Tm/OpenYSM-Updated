package com.elfmcys.yesstevemodel.molang.lexer

import java.io.Reader

class MolangLexerImpl(private val reader: Reader) : MolangLexer {
    private val cursor: Cursor = Cursor()
    private var next: Int = reader.read()
    private var lastToken: Token? = null
    private var token: Token? = null

    override fun cursor(): Cursor = cursor

    override fun current(): Token {
        return token ?: throw IllegalStateException("No current token, please call next() at least once")
    }

    override fun next(): Token {
        lastToken = token
        val nextToken = next0()
        token = nextToken
        return nextToken
    }

    override fun close() {
        reader.close()
    }

    private fun next0(): Token {
        var c = next
        if (c == -1) {
            return Token(TokenKind.EOF, null, cursor.index(), cursor.index() + 1)
        }

        while (c == ' '.code || c == '\t'.code || c == '\n'.code || c == '\r'.code) {
            c = read()
        }

        if (c == -1) {
            return Token(TokenKind.EOF, null, cursor.index(), cursor.index() + 1)
        }

        val start = cursor.index()
        if (c == '.'.code && lastToken?.kind == TokenKind.RPAREN) {
            read()
            return Token(TokenKind.DOT, null, start, cursor.index())
        }

        val isLastIdentifier = lastToken?.kind == TokenKind.IDENTIFIER
        if (Characters.isDigit(c) || (!isLastIdentifier && c == '.'.code)) {
            val builder = StringBuilder(8)
            if (!isLastIdentifier) {
                builder.appendCodePoint(c)
                c = read()
                while (Characters.isDigit(c)) {
                    builder.appendCodePoint(c)
                    c = read()
                }
            } else {
                builder.append('0')
            }

            if (c == '.'.code) {
                builder.append('.')
                c = read()
                while (Characters.isDigit(c)) {
                    builder.appendCodePoint(c)
                    c = read()
                }
            }

            return Token(TokenKind.FLOAT, builder.toString(), start, cursor.index())
        } else if (Characters.isValidForWordStart(c)) {
            val builder = StringBuilder()
            do {
                builder.appendCodePoint(c)
                c = read()
            } while (Characters.isValidForWordContinuation(c))

            val word = builder.toString().lowercase()
            val kind = when (word) {
                "break" -> TokenKind.BREAK
                "continue" -> TokenKind.CONTINUE
                "return" -> TokenKind.RETURN
                "true" -> TokenKind.TRUE
                "false" -> TokenKind.FALSE
                else -> TokenKind.IDENTIFIER
            }

            return Token(
                kind,
                if (kind == TokenKind.IDENTIFIER) word else null,
                start,
                cursor.index()
            )
        } else if (c == '\''.code) {
            val value = StringBuilder(16)
            while (true) {
                c = read()
                if (c == -1) {
                    return Token(TokenKind.ERROR, "Found end-of-file before closing quote", start, cursor.index())
                } else if (c == '\''.code) {
                    break
                } else {
                    value.appendCodePoint(c)
                }
            }
            read()
            return Token(TokenKind.STRING, value.toString(), start, cursor.index())
        } else {
            val tokenKind: TokenKind
            var value: String? = null
            var c1 = -2

            when (c.toChar()) {
                '!' -> {
                    c1 = read()
                    if (c1 == '='.code) {
                        read()
                        tokenKind = TokenKind.BANGEQ
                    } else {
                        tokenKind = TokenKind.BANG
                    }
                }
                '&' -> {
                    c1 = read()
                    if (c1 == '&'.code) {
                        read()
                        tokenKind = TokenKind.AMPAMP
                    } else {
                        tokenKind = TokenKind.ERROR
                        value = "Unexpected token '${c1.toChar()}', expected '&' (Molang doesn't support bitwise operators)"
                    }
                }
                '|' -> {
                    c1 = read()
                    if (c1 == '|'.code) {
                        read()
                        tokenKind = TokenKind.BARBAR
                    } else {
                        tokenKind = TokenKind.ERROR
                        value = "Unexpected token '${c1.toChar()}', expected '|' (Molang doesn't support bitwise operators)"
                    }
                }
                '<' -> {
                    c1 = read()
                    if (c1 == '='.code) {
                        read()
                        tokenKind = TokenKind.LTE
                    } else {
                        tokenKind = TokenKind.LT
                    }
                }
                '>' -> {
                    c1 = read()
                    if (c1 == '='.code) {
                        read()
                        tokenKind = TokenKind.GTE
                    } else {
                        tokenKind = TokenKind.GT
                    }
                }
                '=' -> {
                    c1 = read()
                    if (c1 == '='.code) {
                        read()
                        tokenKind = TokenKind.EQEQ
                    } else {
                        tokenKind = TokenKind.EQ
                    }
                }
                '-' -> {
                    c1 = read()
                    if (c1 == '>'.code) {
                        read()
                        tokenKind = TokenKind.ARROW
                    } else {
                        tokenKind = TokenKind.SUB
                    }
                }
                '?' -> {
                    c1 = read()
                    if (c1 == '?'.code) {
                        read()
                        tokenKind = TokenKind.QUESQUES
                    } else {
                        tokenKind = TokenKind.QUES
                    }
                }
                '/' -> tokenKind = TokenKind.SLASH
                '*' -> tokenKind = TokenKind.STAR
                '+' -> tokenKind = TokenKind.PLUS
                ',' -> tokenKind = TokenKind.COMMA
                '.' -> tokenKind = TokenKind.DOT
                '(' -> tokenKind = TokenKind.LPAREN
                ')' -> tokenKind = TokenKind.RPAREN
                '{' -> tokenKind = TokenKind.LBRACE
                '}' -> tokenKind = TokenKind.RBRACE
                ':' -> tokenKind = TokenKind.COLON
                '[' -> tokenKind = TokenKind.LBRACKET
                ']' -> tokenKind = TokenKind.RBRACKET
                ';' -> tokenKind = TokenKind.SEMICOLON
                else -> {
                    tokenKind = TokenKind.ERROR
                    value = "Unexpected token '${c.toChar()}': invalid token"
                }
            }

            if (c1 == -2) {
                read()
            }

            return Token(tokenKind, value, start, cursor.index())
        }
    }

    private fun read(): Int {
        val c = reader.read()
        cursor.push(c)
        next = c
        return c
    }
}