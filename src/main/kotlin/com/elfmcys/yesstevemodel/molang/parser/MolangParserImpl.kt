package com.elfmcys.yesstevemodel.molang.parser

import com.elfmcys.yesstevemodel.molang.lexer.MolangLexer
import com.elfmcys.yesstevemodel.molang.lexer.TokenKind
import com.elfmcys.yesstevemodel.molang.parser.ast.*
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding

class MolangParserImpl(
    private val lexer: MolangLexer,
    private val binding: ObjectBinding
) : MolangParser {
    private var current: Any? = UNSET_FLAG

    override fun lexer(): MolangLexer = lexer

    override fun current(): Expression {
        if (current === UNSET_FLAG)
            throw IllegalStateException("No current parsed expression, call next() at least once!")
        return current as Expression
    }

    override fun next(): Expression? {
        val expr = next0()
        current = expr
        return expr
    }

    private fun parseSingle(lexer: MolangLexer): Expression {
        var token = lexer.current()
        when (token.kind) {
            TokenKind.FLOAT -> {
                lexer.next()
                return FloatExpression(token.value!!.toFloat())
            }

            TokenKind.STRING -> {
                lexer.next()
                return StringExpression(token.value!!)
            }

            TokenKind.TRUE -> {
                lexer.next()
                return FloatExpression.ONE
            }

            TokenKind.FALSE -> {
                lexer.next()
                return FloatExpression.ZERO
            }

            TokenKind.LPAREN -> {
                lexer.next()
                val expression = parseCompoundExpression(lexer, 0)
                token = lexer.current()
                if (token.kind != TokenKind.RPAREN) {
                    throw ParseException("Non closed expression", lexer.cursor())
                }
                lexer.next()
                return expression
            }

            TokenKind.LBRACE -> {
                lexer.next()
                token = lexer.current()
                val expressions = mutableListOf<Expression>()
                while (token.kind != TokenKind.RBRACE) {
                    expressions.add(parseCompoundExpression(lexer, 0))
                    val cur = lexer.current()
                    if (cur.kind == TokenKind.RBRACE) {
                        lexer.next()
                        return ExecutionScopeExpression(expressions)
                    }
                    if (cur.kind == TokenKind.EOF)
                        throw ParseException("Found the end before the execution scope closing token", lexer.cursor())
                    if (cur.kind == TokenKind.ERROR)
                        throw ParseException("Found an invalid token (error): ${cur.value}", lexer.cursor())
                    if (cur.kind != TokenKind.SEMICOLON) throw ParseException("Missing semicolon", lexer.cursor())
                    token = lexer.next()
                }
                lexer.next()
                return ExecutionScopeExpression(expressions)
            }

            TokenKind.BREAK -> {
                lexer.next()
                return StatementExpression(StatementExpression.Op.BREAK)
            }

            TokenKind.CONTINUE -> {
                lexer.next()
                return StatementExpression(StatementExpression.Op.CONTINUE)
            }

            TokenKind.IDENTIFIER -> {
                var lastTarget = binding.getProperty(token.value!!)
                    ?: throw ParseException("Failed to get property: ${token.value}", lexer.cursor())
                var expr = IdentifierExpression.get(token.value, lastTarget)
                token = lexer.next()
                if (token.kind == TokenKind.DOT) {
                    token = lexer.next()
                    if (token.kind != TokenKind.IDENTIFIER) {
                        throw ParseException("Unexpected token, expected a valid field token", lexer.cursor())
                    }
                    lastTarget = if (lastTarget is ObjectBinding) {
                        lastTarget.getProperty(token.value!!)
                    } else {
                        throw ParseException("Illegal access to : ${token.value}", lexer.cursor())
                    } ?: throw ParseException("Failed to get property: ${token.value}", lexer.cursor())

                    expr = IdentifierExpression.get(token.value, lastTarget)
                    lexer.next()
                }
                return expr
            }

            TokenKind.PLUS -> {
                lexer.next()
                return parseCompoundExpression(lexer, UnaryExpression.Op.PLUS.precedence)
            }

            TokenKind.SUB -> {
                lexer.next()
                return UnaryExpression(
                    UnaryExpression.Op.ARITHMETICAL_NEGATION,
                    parseCompoundExpression(lexer, UnaryExpression.Op.ARITHMETICAL_NEGATION.precedence)
                )
            }

            TokenKind.BANG -> {
                lexer.next()
                return UnaryExpression(
                    UnaryExpression.Op.LOGICAL_NEGATION,
                    parseCompoundExpression(lexer, UnaryExpression.Op.LOGICAL_NEGATION.precedence)
                )
            }

            TokenKind.RETURN -> {
                lexer.next()
                return UnaryExpression(
                    UnaryExpression.Op.RETURN,
                    parseCompoundExpression(lexer, UnaryExpression.Op.RETURN.precedence)
                )
            }

            else -> throw ParseException("Expected an expression.", lexer.cursor())
        }
    }

    private fun parseCompoundExpression(lexer: MolangLexer, lastPrecedence: Int): Expression {
        var expr = parseSingle(lexer)
        while (true) {
            val compoundExpr = parseCompound(lexer, expr, lastPrecedence)
            val cur = lexer.current()
            when {
                cur.kind == TokenKind.EOF || cur.kind == TokenKind.SEMICOLON -> {
                    return compoundExpr
                }

                compoundExpr == expr -> {
                    return expr
                }

                else -> expr = compoundExpr
            }
        }
    }

    private fun parseCompound(lexer: MolangLexer, left: Expression, lastPrecedence: Int): Expression {
        var current = lexer.current()
        if (left is CallExpression) {
            if (current.kind == TokenKind.LPAREN) {
                if (left.arguments() != CallExpression.EMPTY)
                    throw ParseException("Multiple '()' after function name", lexer.cursor())
                lexer.next()
                val arguments = mutableListOf<Expression>()
                current = lexer.current()
                if (current.kind != TokenKind.RPAREN) {
                    while (true) {
                        arguments.add(parseCompoundExpression(lexer, 0))
                        current = lexer.current()
                        when (current.kind) {
                            TokenKind.EOF -> {
                                throw ParseException("Found EOF before closing RPAREN", null)
                            }

                            TokenKind.RPAREN -> {
                                lexer.next()
                                break
                            }

                            else -> {
                                if (current.kind != TokenKind.COMMA) {
                                    throw ParseException("Expected a comma", lexer.cursor())
                                }
                                lexer.next()
                            }
                        }
                    }
                } else lexer.next()

                if (!left.function().validateArgumentSize(arguments.size))
                    throw ParseException("Illegal function arguments size", lexer.cursor())
                return CallExpression(left.function(), Function.ArgumentCollection(arguments))
            }

            if (!left.function().validateArgumentSize(left.arguments().size()))
                throw ParseException("Illegal function arguments size", lexer.cursor())
        }

        when (current.kind) {
            TokenKind.RPAREN, TokenKind.EOF -> return left
            TokenKind.LPAREN -> {
                if (lastPrecedence >= BinaryExpression.Op.MUL.precedence) return left
                val right = parseCompoundExpression(lexer, BinaryExpression.Op.MUL.precedence)
                return BinaryExpression(BinaryExpression.Op.MUL, left, right)
            }

            TokenKind.QUES -> {
                if (lastPrecedence > PRECEDENCE_QUES) return left
                lexer.next()
                val trueValue = parseCompoundExpression(lexer, PRECEDENCE_QUES)
                return if (lexer.current().kind == TokenKind.COLON) {
                    lexer.next()
                    TernaryConditionalExpression(left, trueValue, parseCompoundExpression(lexer, PRECEDENCE_QUES))
                } else BinaryExpression(BinaryExpression.Op.CONDITIONAL, left, trueValue)
            }

            TokenKind.LBRACKET -> {
                lexer.next()
                val indexExpression = parseCompoundExpression(lexer, 0)
                if (lexer.current().kind == TokenKind.RBRACKET) {
                    lexer.next()
                    return BinaryOperationExpression(left, indexExpression)
                }
                throw ParseException("Expect a ']' after array index", lexer.cursor())
            }

            else -> {}
        }

        if (current.kind == TokenKind.DOT) {
            current = lexer.next()
            if (current.kind == TokenKind.IDENTIFIER) {
                lexer.next()
                return StructAccessExpression(left, current.value!!)
            } else {
                throw ParseException("Expect a identifier after struct access operator", lexer.cursor())
            }
        }

        val op = when (current.kind) {
            TokenKind.AMPAMP -> BinaryExpression.Op.AND
            TokenKind.BARBAR -> BinaryExpression.Op.OR
            TokenKind.LT -> BinaryExpression.Op.LT
            TokenKind.LTE -> BinaryExpression.Op.LTE
            TokenKind.GT -> BinaryExpression.Op.GT
            TokenKind.GTE -> BinaryExpression.Op.GTE
            TokenKind.PLUS -> BinaryExpression.Op.ADD
            TokenKind.SUB -> BinaryExpression.Op.SUB
            TokenKind.STAR -> BinaryExpression.Op.MUL
            TokenKind.SLASH -> BinaryExpression.Op.DIV
            TokenKind.QUESQUES -> BinaryExpression.Op.NULL_COALESCE
            TokenKind.EQ -> BinaryExpression.Op.ASSIGN
            TokenKind.EQEQ -> BinaryExpression.Op.EQ
            TokenKind.BANGEQ -> BinaryExpression.Op.NEQ
            TokenKind.ARROW -> BinaryExpression.Op.ARROW
            else -> return left
        }

        val precedence = op.precedence
        if (lastPrecedence >= precedence) return left

        lexer.next()
        return BinaryExpression(op, left, parseCompoundExpression(lexer, precedence))
    }

    private fun next0(): Expression? {
        var token = lexer.next()
        if (token.kind == TokenKind.EOF) return null
        if (token.kind == TokenKind.ERROR)
            throw ParseException("Found an invalid token (error): ${token.value}", cursor())
        val expression = parseCompoundExpression(lexer, -10)
        token = lexer.current()
        if (token.kind != TokenKind.EOF && token.kind != TokenKind.SEMICOLON)
            throw ParseException("Expected a semicolon, but was $token", lexer.cursor())
        return expression
    }

    override fun close() {
        lexer.close()
    }

    companion object {
        const val PRECEDENCE_QUES: Int = 1400
        private val UNSET_FLAG: Any = Any()
    }
}