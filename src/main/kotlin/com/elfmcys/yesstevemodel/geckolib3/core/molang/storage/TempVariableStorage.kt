package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import it.unimi.dsi.fastutil.longs.LongArrayList
import org.jetbrains.annotations.NotNull
import java.util.*

open class TempVariableStorage : ITempVariableStorage {
    var baseOffset: Int = 0
    var currentSize: Int = 0
    var scopeStart: Int = 0
    var scopeSize: Int = 0
    var elements: Array<Any> = arrayOfNulls<Any>(16)
    val scopeStack: LongArrayList = LongArrayList(4)
    val listView: ElementListView = ElementListView()
    open fun ensureCapacity(i: Int) {
        var objArr: Array<Any> = this.elements
        if (objArr.length < i) {
            var length: Int = objArr.length
            while (true) {
                var i2: Int = length * 2
                if (i2 < i) {
                    length = i2
                } else {
                    this.elements = Arrays.copyOf(objArr, i2)
                    return
                }
            }
        }
    }
    open fun getElement(i: Int): Any {
        if (i < this.currentSize) {
            return this.elements[this.baseOffset + i]
        }
        return null
    }
    open fun setElement(i: Int, obj: Any) {
        var i2: Int = i + 1
        if (this.currentSize < i2) {
            this.currentSize = i2
            ensureCapacity(this.baseOffset + i2)
        }
        this.elements[this.baseOffset + i] = obj
    }
    open fun pushScope(list: MutableList<*>): Boolean {
        if (this.scopeStack.size() < 32) {
            var i: Int = this.baseOffset + this.currentSize
            var size: Int = list.size()
            var i2: Int = i + size
            ensureCapacity(i2)
            var objArr: Array<Any> = this.elements
            var i3 = 0
            while (i3 < size) {
                objArr[i + i3] = list.get(i3)
                i3++
            }
            this.scopeStack.add(((this.scopeSize as Long) shl 32 or this.scopeStart))
            this.scopeStart = i
            this.scopeSize = size
            this.baseOffset = i2
            this.currentSize = 0
            return true
        }
        return false
    }
    open fun pushScopeWithArgs(executionContext: ExecutionContext<*>, function: Function): Boolean {
        if (this.scopeStack.size() < 32) {
            var i: Int = this.baseOffset + this.currentSize
            var i4: Int = function.size()
            var i2: Int = i + i4
            ensureCapacity(i2)
            this.currentSize += function.size()
            var i3 = 0
            while (i3 < i4) {
                this.elements[i + i3] = function.getValue(executionContext, i3)
                i3++
            }
            this.scopeStack.add(((this.scopeSize as Long) shl 32 or this.scopeStart))
            this.scopeStart = i
            this.scopeSize = i4
            this.baseOffset = i2
            this.currentSize = 0
            return true
        }
        return false
    }
    open fun popScope() {
        var longArrayList: LongArrayList = this.scopeStack
        if (!longArrayList.isEmpty()) {
            var jRemoveLong: Long = longArrayList.removeLong(longArrayList.size() - 1)
            var i: Int = this.scopeStart
            var i2: Int = ((jRemoveLong and 4294967295L) as Int)
            var i3: Int = (jRemoveLong shr 32 as Int)
            var i4: Int = i2 + i3
            this.scopeStart = i2
            this.scopeSize = i3
            this.baseOffset = i4
            this.currentSize = i - i4
        }
    }
    open fun asList(): MutableList<Any> {
        return this.listView
    }
    open class ElementIterator : Iterator<Any> {
        var currentIndex: Int = 0
        var endIndex: Int = 0
        constructor() {
            this.currentIndex = this@TempVariableStorage.scopeStart
            this.endIndex = this@TempVariableStorage.baseOffset
        }
        open fun hasNext(): Boolean {
            return this.currentIndex < this.endIndex
        }
        open fun next(): Any {
            if (this.currentIndex < this.endIndex) {
                var objArr: Array<Any> = this@TempVariableStorage.elements
                var i: Int = this.currentIndex
                this.currentIndex = i + 1
                return objArr[i]
            }
            return null
        }
    }
    open class ElementListView : MutableList<Any> {
        constructor() {
        }
        open fun size(): Int {
            return this@TempVariableStorage.scopeSize
        }
        open fun isEmpty(): Boolean {
            return this@TempVariableStorage.scopeSize == 0
        }
        open fun get(i: Int): Any {
            if (i >= 0 && i < this@TempVariableStorage.scopeSize) {
                return this@TempVariableStorage.elements[this@TempVariableStorage.scopeStart + i]
            }
            return null
        }
        open fun iterator(): Iterator<Any> {
            return this@TempVariableStorage
        }
        open fun contains(obj: Any): Boolean {
            throw UnsupportedOperationException()
        }
        open fun toArray(): Array<Any> {
            throw UnsupportedOperationException()
        }
        open fun toArray(tArr: Array<T>): Array<T> {
            throw UnsupportedOperationException()
        }
        open fun add(obj: Any): Boolean {
            throw UnsupportedOperationException()
        }
        open fun remove(obj: Any): Boolean {
            throw UnsupportedOperationException()
        }
        open fun containsAll(collection: Collection<*>): Boolean {
            throw UnsupportedOperationException()
        }
        open fun addAll(collection: Collection<*>): Boolean {
            throw UnsupportedOperationException()
        }
        open fun addAll(i: Int, collection: Collection<Any>): Boolean {
            throw UnsupportedOperationException()
        }
        open fun removeAll(collection: Collection<*>): Boolean {
            throw UnsupportedOperationException()
        }
        open fun retainAll(collection: Collection<*>): Boolean {
            throw UnsupportedOperationException()
        }
        open fun clear() {
            throw UnsupportedOperationException()
        }
        open fun set(i: Int, obj: Any): Any {
            throw UnsupportedOperationException()
        }
        open fun add(i: Int, obj: Any) {
            throw UnsupportedOperationException()
        }
        open fun remove(i: Int): Any {
            throw UnsupportedOperationException()
        }
        open fun indexOf(obj: Any): Int {
            throw UnsupportedOperationException()
        }
        open fun lastIndexOf(obj: Any): Int {
            throw UnsupportedOperationException()
        }
        open fun listIterator(): ListIterator<Any> {
            throw UnsupportedOperationException()
        }
        open fun listIterator(i: Int): ListIterator<Any> {
            throw UnsupportedOperationException()
        }
        open fun subList(i: Int, i2: Int): MutableList<Any> {
            throw UnsupportedOperationException()
        }
    }
    companion object {
        @JvmField var MAX_DEPTH: Int = 32
    }
}