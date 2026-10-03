package com.elfmcys.yesstevemodel.molang.parser.ast

interface ExpressionVisitor<R> {
    fun visit(expression: Expression): R
    fun visitFloat(expression: FloatExpression): R = visit(expression)
    fun visitString(expression: StringExpression): R = visit(expression)
    fun visitIdentifier(expression: IdentifierExpression): R = visit(expression)
    fun visitVariable(expression: VariableExpression): R = visit(expression)
    fun visitAssignableVariable(expression: AssignableVariableExpression): R = visit(expression)
    fun visitStruct(expression: StructAccessExpression): R = visit(expression)
    fun visitTernaryConditional(expression: TernaryConditionalExpression): R = visit(expression)
    fun visitUnary(expression: UnaryExpression): R = visit(expression)
    fun visitExecutionScope(expression: ExecutionScopeExpression): R = visit(expression)
    fun visitBinary(expression: BinaryExpression): R = visit(expression)
    fun visitCall(expression: CallExpression): R = visit(expression)
    fun visitStatement(expression: StatementExpression): R = visit(expression)
    fun visitBinaryOperation(expression: BinaryOperationExpression): R = visit(expression)
}