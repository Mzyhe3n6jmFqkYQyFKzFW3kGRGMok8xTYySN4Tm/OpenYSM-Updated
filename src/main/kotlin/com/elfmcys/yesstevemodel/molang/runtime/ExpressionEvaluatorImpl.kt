package com.elfmcys.yesstevemodel.molang.runtime

import com.elfmcys.yesstevemodel.molang.parser.ast.*
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions

class ExpressionEvaluatorImpl<TEntity>(
    private val entity: TEntity
) : ExpressionEvaluator<TEntity>, ExpressionVisitor<Any?> {
    private var returnValue: Any? = null
    private var op: StatementExpression.Op? = null
    private var cnt: Int = 0
    private var working: Int = 0

    override fun entity(): TEntity = entity

    override fun eval(expression: Expression): Any? {
        return try {
            expression.visit(this)
        } finally {
            returnValue = null
            op = null
        }
    }

    override fun evalAsFloat(expression: Expression): Float {
        return try {
            evalFloat(expression)
        } finally {
            returnValue = null
            op = null
        }
    }

    override fun evalAsBoolean(expression: Expression): Boolean {
        return try {
            evalBool(expression)
        } finally {
            returnValue = null
            op = null
        }
    }

    private fun evalFloat(expr: Expression): Float {
        if (expr is FloatExpression) {
            return expr.value()
        }
        if (expr is BinaryExpression) {
            when (expr.op()) {
                BinaryExpression.Op.ADD -> return evalFloat(expr.left()) + evalFloat(expr.right())
                BinaryExpression.Op.SUB -> return evalFloat(expr.left()) - evalFloat(expr.right())
                BinaryExpression.Op.MUL -> return evalFloat(expr.left()) * evalFloat(expr.right())
                BinaryExpression.Op.DIV -> {
                    val d = evalFloat(expr.right())
                    if (d == 0.0f) return 0.0f
                    return evalFloat(expr.left()) / d
                }

                BinaryExpression.Op.LT -> return if (evalFloat(expr.left()) < evalFloat(expr.right())) 1.0f else 0.0f
                BinaryExpression.Op.LTE -> return if (evalFloat(expr.left()) <= evalFloat(expr.right())) 1.0f else 0.0f
                BinaryExpression.Op.GT -> return if (evalFloat(expr.left()) > evalFloat(expr.right())) 1.0f else 0.0f
                BinaryExpression.Op.GTE -> return if (evalFloat(expr.left()) >= evalFloat(expr.right())) 1.0f else 0.0f
                BinaryExpression.Op.AND -> return if (evalBool(expr.left()) && evalBool(expr.right())) 1.0f else 0.0f
                BinaryExpression.Op.OR -> return if (evalBool(expr.left()) || evalBool(expr.right())) 1.0f else 0.0f
                else -> {}
            }
        }
        if (expr is UnaryExpression) {
            return when (expr.op()) {
                UnaryExpression.Op.ARITHMETICAL_NEGATION -> -evalFloat(expr.expression())
                UnaryExpression.Op.PLUS -> evalFloat(expr.expression())
                UnaryExpression.Op.LOGICAL_NEGATION -> if (evalBool(expr.expression())) 0.0f else 1.0f
                UnaryExpression.Op.RETURN -> evalFloat(expr.expression())
            }
        }
        if (expr is TernaryConditionalExpression) {
            return if (evalBool(expr.condition())) {
                evalFloat(expr.trueExpression())
            } else {
                evalFloat(expr.falseExpression())
            }
        }
        return ValueConversions.asFloat(expr.visit(this))
    }

    private fun evalBool(expr: Expression): Boolean {
        if (expr is FloatExpression) {
            return expr.value() != 0.0f
        }
        if (expr is BinaryExpression) {
            when (expr.op()) {
                BinaryExpression.Op.AND -> return evalBool(expr.left()) && evalBool(expr.right())
                BinaryExpression.Op.OR -> return evalBool(expr.left()) || evalBool(expr.right())
                BinaryExpression.Op.LT -> return evalFloat(expr.left()) < evalFloat(expr.right())
                BinaryExpression.Op.LTE -> return evalFloat(expr.left()) <= evalFloat(expr.right())
                BinaryExpression.Op.GT -> return evalFloat(expr.left()) > evalFloat(expr.right())
                BinaryExpression.Op.GTE -> return evalFloat(expr.left()) >= evalFloat(expr.right())
                BinaryExpression.Op.ADD -> return evalFloat(expr.left()) + evalFloat(expr.right()) != 0.0f
                BinaryExpression.Op.SUB -> return evalFloat(expr.left()) - evalFloat(expr.right()) != 0.0f
                BinaryExpression.Op.MUL -> {
                    val l = evalFloat(expr.left())
                    return l != 0.0f && evalFloat(expr.right()) != 0.0f
                }

                BinaryExpression.Op.DIV -> {
                    val r = evalFloat(expr.right())
                    return r != 0.0f && evalFloat(expr.left()) / r != 0.0f
                }

                else -> {}
            }
        }
        if (expr is UnaryExpression) {
            return when (expr.op()) {
                UnaryExpression.Op.LOGICAL_NEGATION -> !evalBool(expr.expression())
                UnaryExpression.Op.ARITHMETICAL_NEGATION -> evalBool(expr.expression())
                UnaryExpression.Op.PLUS -> evalBool(expr.expression())
                UnaryExpression.Op.RETURN -> evalBool(expr.expression())
            }
        }
        if (expr is TernaryConditionalExpression) {
            return if (evalBool(expr.condition())) {
                evalBool(expr.trueExpression())
            } else {
                evalBool(expr.falseExpression())
            }
        }
        return ValueConversions.asBoolean(expr.visit(this))
    }

    override fun evalAll(iterable: Iterable<Expression>, returnLast: Boolean): Any? {
        if (returnLast) {
            working++
        }
        var objValueOf: Any? = DOUBLE_ZERO
        try {
            if (iterable is List<Expression>) {
                val size = iterable.size
                for (i in 0 until size) {
                    objValueOf = iterable[i].visit(this)
                    val obj = popReturnValue()
                    if (obj != null) {
                        objValueOf = obj
                        break
                    }
                }
            } else {
                for (expression in iterable) {
                    objValueOf = expression.visit(this)
                    val obj = popReturnValue()
                    if (obj != null) {
                        objValueOf = obj
                        break
                    }
                }
            }
            return objValueOf
        } finally {
            returnValue = null
            op = null
            if (returnLast) {
                working--
            }
        }
    }

    fun <TNewEntity> createChild(newEntity: TNewEntity): ExpressionEvaluatorImpl<TNewEntity> {
        return ExpressionEvaluatorImpl(newEntity)
    }

    private fun popReturnValue(): Any? {
        val obj = returnValue
        if (working == 0) {
            returnValue = null
        }
        return obj
    }

    override fun visitCall(expression: CallExpression): Any? {
        return expression.function().evaluate(this, expression.arguments())
    }

    override fun visitFloat(expression: FloatExpression): Any {
        return expression.boxed()
    }

    override fun visitExecutionScope(expression: ExecutionScopeExpression): Any? {
        var result: Any? = null
        val expressions = expression.expressions()
        val size = expressions.size
        for (i in 0 until size) {
            result = expressions[i].visit(this)
            val obj = popReturnValue()
            if (obj != null) {
                return obj
            }
            if (cnt > 0 && op != null) {
                return null
            }
        }
        return result
    }

    private fun buildExecutionScope(executionScope: ExecutionScopeExpression): Boolean {
        cnt++
        try {
            val expressions = executionScope.expressions()
            val size = expressions.size
            for (i in 0 until size) {
                expressions[i].visit(this)
                if (popReturnValue() != null) {
                    return true
                }
                val currentOp = op
                op = null
                if (currentOp == StatementExpression.Op.CONTINUE) {
                    break
                }
                if (currentOp == StatementExpression.Op.BREAK) {
                    cnt--
                    return true
                }
            }
            cnt--
            return false
        } finally {
            cnt--
        }
    }

    fun loopFunciton(executionScope: ExecutionScopeExpression, n: Int) {
        var i = 0
        while (i < n && !buildExecutionScope(executionScope)) {
            i++
        }
    }

    fun forEachFunction(
        executionScope: ExecutionScopeExpression,
        variableAccess: AssignableVariable,
        iterable: Iterable<*>
    ) {
        val it = iterable.iterator()
        while (it.hasNext()) {
            variableAccess.assign(this, it.next())
            if (buildExecutionScope(executionScope)) {
                return
            }
        }
    }

    override fun visitIdentifier(expression: IdentifierExpression): Any {
        throw RuntimeException("Unknown identifier type")
    }

    override fun visitVariable(expression: VariableExpression): Any? {
        return expression.target().evaluate(this)
    }

    override fun visitAssignableVariable(expression: AssignableVariableExpression): Any? {
        return expression.target().evaluate(this)
    }

    override fun visitStruct(expression: StructAccessExpression): Any? {
        val value = expression.left().visit(this)
        return if (value is Struct) {
            value[expression.path()]
        } else {
            null
        }
    }

    override fun visitBinary(expression: BinaryExpression): Any? {
        return BINARY_EVALUATORS[expression.op().index()].eval(
            this,
            expression.left(),
            expression.right()
        )
    }

    override fun visitBinaryOperation(expression: BinaryOperationExpression): Any? {
        val left = expression.getLeft().visit(this)
        val right = expression.getRight().visit(this)
        if (right is Number) {
            var index = right.toInt()
            if (index < 0) {
                index = 0
            }
            if (left is List<*>) {
                if (left.size > index) {
                    return left[index]
                }
                return null
            }
            return null
        }
        return null
    }

    override fun visitUnary(expression: UnaryExpression): Any {
        val value = expression.expression().visit(this)
        return when (expression.op()) {
            UnaryExpression.Op.LOGICAL_NEGATION -> !ValueConversions.asBoolean(value)
            UnaryExpression.Op.ARITHMETICAL_NEGATION -> -ValueConversions.asFloat(value)
            UnaryExpression.Op.PLUS -> ValueConversions.asFloat(value)
            UnaryExpression.Op.RETURN -> {
                returnValue = value
                DOUBLE_ZERO
            }
        }
    }

    override fun visitStatement(expression: StatementExpression): Any? {
        op = when (expression.op) {
            StatementExpression.Op.BREAK -> StatementExpression.Op.BREAK
            StatementExpression.Op.CONTINUE -> StatementExpression.Op.CONTINUE
        }
        return null
    }

    override fun visitString(expression: StringExpression): Any {
        return expression
    }

    override fun visitTernaryConditional(expression: TernaryConditionalExpression): Any? {
        val cond = expression.condition().visit(this)
        return if (ValueConversions.asBoolean(cond)) {
            expression.trueExpression().visit(this)
        } else {
            expression.falseExpression().visit(this)
        }
    }

    override fun visit(expression: Expression): Any {
        throw UnsupportedOperationException("Unsupported expression type: $expression")
    }

    private fun interface Evaluator {
        fun eval(evaluator: ExpressionEvaluatorImpl<*>, a: Expression, b: Expression): Any?
    }

    companion object {
        private const val DOUBLE_ZERO: Double = 0.0
        private const val FLOAT_ZERO: Float = 0.0f

        private val BINARY_EVALUATORS: Array<Evaluator> = arrayOf(
            // 0: AND
            Evaluator { evaluator, a, b ->
                if (!ValueConversions.asBoolean(a.visit(evaluator))) return@Evaluator false
                if (ValueConversions.asBoolean(b.visit(evaluator))) true else false
            },
            // 1: OR
            Evaluator { evaluator, a, b ->
                if (ValueConversions.asBoolean(a.visit(evaluator))) return@Evaluator true
                if (ValueConversions.asBoolean(b.visit(evaluator))) true else false
            },
            // 2: LT
            Evaluator { evaluator, a, b ->
                val av = ValueConversions.asFloat(a.visit(evaluator))
                val bv = ValueConversions.asFloat(b.visit(evaluator))
                av < bv
            },
            // 3: LTE
            Evaluator { evaluator, a, b ->
                val av = ValueConversions.asFloat(a.visit(evaluator))
                val bv = ValueConversions.asFloat(b.visit(evaluator))
                av <= bv
            },
            // 4: GT
            Evaluator { evaluator, a, b ->
                val av = ValueConversions.asFloat(a.visit(evaluator))
                val bv = ValueConversions.asFloat(b.visit(evaluator))
                av > bv
            },
            // 5: GTE
            Evaluator { evaluator, a, b ->
                val av = ValueConversions.asFloat(a.visit(evaluator))
                val bv = ValueConversions.asFloat(b.visit(evaluator))
                av >= bv
            },
            // 6: ADD
            Evaluator { evaluator, a, b ->
                val aVal = a.visit(evaluator)
                val bVal = b.visit(evaluator)
                ValueConversions.asFloat(aVal) + ValueConversions.asFloat(bVal)
            },
            // 7: SUB
            Evaluator { evaluator, a, b ->
                val av = ValueConversions.asFloat(a.visit(evaluator))
                val bv = ValueConversions.asFloat(b.visit(evaluator))
                av - bv
            },
            // 8: MUL
            Evaluator { evaluator, a, b ->
                val av = ValueConversions.asFloat(a.visit(evaluator))
                val bv = ValueConversions.asFloat(b.visit(evaluator))
                av * bv
            },
            // 9: DIV
            Evaluator { evaluator, a, b ->
                val dividend = ValueConversions.asFloat(a.visit(evaluator))
                val divisor = ValueConversions.asFloat(b.visit(evaluator))
                if (divisor == 0.0f) FLOAT_ZERO else dividend / divisor
            },
            // 10: ARROW
            Evaluator { evaluator, a, b ->
                val `val` = a.visit(evaluator) ?: return@Evaluator null
                val child = evaluator.createChild(`val`)
                val res = b.visit(child)
                evaluator.returnValue = child.returnValue
                res
            },
            // 11: NULL_COALESCE
            Evaluator { evaluator, a, b ->
                val `val` = a.visit(evaluator)
                `val` ?: b.visit(evaluator)
            },
            // 12: ASSIGN
            Evaluator { evaluator, a, b ->
                var `val` = b.visit(evaluator)
                when (a) {
                    is AssignableVariableExpression -> {
                        val `var` = a.target()
                        if (`val` is Struct) {
                            `val` = `val`.copy()
                        }
                        `var`.assign(evaluator, `val`)
                    }

                    is StructAccessExpression -> {
                        if (`val` is Struct) {
                            return@Evaluator `val`
                        }
                        val value = a.left().visit(evaluator)
                        when {
                            value is Struct -> {
                                value[a.path()] = `val`
                            }

                            a.left() is AssignableVariableExpression -> {
                                val variable = (a.left() as AssignableVariableExpression).target()
                                val struct: Struct = HashMapStruct()
                                struct[a.path()] = `val`
                                variable.assign(evaluator, struct)
                            }
                        }
                    }
                }
                `val`
            },
            // 13: CONDITIONAL
            Evaluator { evaluator, a, b ->
                val condition = a.visit(evaluator)
                if (ValueConversions.asBoolean(condition)) {
                    b.visit(evaluator)
                } else {
                    null
                }
            },
            // 14: EQ
            Evaluator { evaluator, a, b ->
                val left = a.visit(evaluator)
                val right = b.visit(evaluator)
                if (left === right) return@Evaluator true
                if (left is Number || right is Number) {
                    return@Evaluator ValueConversions.asFloat(right) == ValueConversions.asFloat(left)
                }
                if (left == null || right == null) return@Evaluator false
                if (left is StringExpression) return@Evaluator left == right
                if (right is StringExpression) return@Evaluator right == left
                left == right
            },
            // 15: NEQ
            Evaluator { evaluator, a, b ->
                val left = a.visit(evaluator)
                val right = b.visit(evaluator)
                if (left === right) return@Evaluator false
                if (left is Number || right is Number) {
                    return@Evaluator ValueConversions.asFloat(right) != ValueConversions.asFloat(left)
                }
                if (left == null || right == null) return@Evaluator true
                if (left is StringExpression) return@Evaluator left != right
                if (right is StringExpression) return@Evaluator right != left
                left != right
            }
        )
    }
}