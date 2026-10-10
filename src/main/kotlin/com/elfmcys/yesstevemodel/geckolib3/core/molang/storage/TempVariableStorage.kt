package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import it.unimi.dsi.fastutil.longs.LongArrayList

class TempVariableStorage : ITempVariableStorage {
    private var baseOffset: Int = 0
    private var currentSize: Int = 0
    private var scopeStart: Int = 0
    private var scopeSize: Int = 0
    private var elements: Array<Any?> = arrayOfNulls(16)
    private val scopeStack = LongArrayList(4)
    private val listView = ElementListView()

    private fun ensureCapacity(minCapacity: Int) {
        val currentElements = elements
        if (currentElements.size < minCapacity) {
            var length = currentElements.size
            while (true) {
                val newLength = length * 2
                if (newLength < minCapacity) {
                    length = newLength
                } else {
                    elements = currentElements.copyOf(newLength)
                    return
                }
            }
        }
    }

    override fun getElement(i: Int): Any? {
        if (i < currentSize) {
            return elements[baseOffset + i]
        }
        return null
    }

    override fun setElement(i: Int, obj: Any?) {
        val i2 = i + 1
        if (currentSize < i2) {
            currentSize = i2
            ensureCapacity(baseOffset + i2)
        }
        elements[baseOffset + i] = obj
    }

    fun pushScope(list: List<*>): Boolean {
        if (scopeStack.size < MAX_DEPTH) {
            val i = baseOffset + currentSize
            val size = list.size
            val i2 = i + size
            ensureCapacity(i2)
            val currentElements = elements
            for (i3 in 0 until size) {
                currentElements[i + i3] = list[i3]
            }
            scopeStack.add(scopeSize.toLong() shl 32 or (scopeStart.toLong() and 0xFFFFFFFFL))
            scopeStart = i
            scopeSize = size
            baseOffset = i2
            currentSize = 0
            return true
        }
        return false
    }

    fun pushScopeWithArgs(executionContext: ExecutionContext<*>, function: Function.ArgumentCollection): Boolean {
        if (scopeStack.size < MAX_DEPTH) {
            val i = baseOffset + currentSize
            val i4 = function.size()
            val i2 = i + i4
            ensureCapacity(i2)
            currentSize += function.size()
            val currentElements = elements
            for (i3 in 0 until i4) {
                currentElements[i + i3] = function.getValue(executionContext, i3)
            }
            scopeStack.add(scopeSize.toLong() shl 32 or (scopeStart.toLong() and 0xFFFFFFFFL))
            scopeStart = i
            scopeSize = i4
            baseOffset = i2
            currentSize = 0
            return true
        }
        return false
    }

    fun popScope() {
        if (!scopeStack.isEmpty) {
            val jRemoveLong = scopeStack.removeLong(scopeStack.size - 1)
            val i = scopeStart
            val i2 = (jRemoveLong and 0xFFFFFFFFL).toInt()
            val i3 = (jRemoveLong ushr 32).toInt()
            val i4 = i2 + i3
            scopeStart = i2
            scopeSize = i3
            baseOffset = i4
            currentSize = i - i4
        }
    }

    fun asList(): List<Any?> = listView

    inner class ElementIterator : Iterator<Any?> {
        private var currentIndex: Int = scopeStart
        private val endIndex: Int = baseOffset

        override fun hasNext(): Boolean = currentIndex < endIndex

        override fun next(): Any? {
            if (currentIndex < endIndex) {
                return elements[currentIndex++]
            }
            return null
        }
    }

    inner class ElementListView : List<Any?> {
        override val size: Int
            get() = scopeSize

        override fun isEmpty(): Boolean = scopeSize == 0

        override fun get(index: Int): Any? {
            if (index in 0 until scopeSize) {
                return elements[scopeStart + index]
            }
            return null
        }

        override fun iterator(): Iterator<Any?> = ElementIterator()

        override fun contains(element: Any?): Boolean {
            throw UnsupportedOperationException()
        }

        override fun containsAll(elements: Collection<Any?>): Boolean {
            throw UnsupportedOperationException()
        }

        override fun indexOf(element: Any?): Int {
            throw UnsupportedOperationException()
        }

        override fun lastIndexOf(element: Any?): Int {
            throw UnsupportedOperationException()
        }

        override fun listIterator(): ListIterator<Any?> {
            throw UnsupportedOperationException()
        }

        override fun listIterator(index: Int): ListIterator<Any?> {
            throw UnsupportedOperationException()
        }

        override fun subList(fromIndex: Int, toIndex: Int): List<Any?> {
            throw UnsupportedOperationException()
        }
    }

    companion object {
        private const val MAX_DEPTH = 32
    }
}