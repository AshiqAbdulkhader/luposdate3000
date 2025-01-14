/*
 * This file is part of the Luposdate3000 distribution (https://github.com/luposdate3000/luposdate3000).
 * Copyright (c) 2020-2021, Institute of Information Systems (Benjamin Warnke and contributors of LUPOSDATE3000), University of Luebeck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package lupos.triple_store_id_triple.index_IDTriple

import lupos.shared.BufferManagerPageWrapper
import lupos.shared.MyReadWriteLock
import lupos.shared.SanityCheck
import lupos.shared.operator.iterator.ColumnIterator
import kotlin.jvm.JvmField

internal abstract class NodeLeafColumnIterator(@JvmField var node: BufferManagerPageWrapper, @JvmField var nodeid: Int, @JvmField val lock: MyReadWriteLock, @JvmField val nodeManager: NodeManager) : ColumnIterator() {
    @JvmField
    var remaining = 0

    @JvmField
    var offset = NodeLeaf.START_OFFSET

    @JvmField
    var label = 3

    @JvmField
    var needsReset = true

    @Suppress("NOTHING_TO_INLINE")
    /*suspend*/ internal inline fun __init() {
        lock.readLock()
        remaining = NodeShared.getTripleCount(node)
    }

    @Suppress("NOTHING_TO_INLINE")
    /*suspend*/ internal inline fun _close() {
        if (label == 3) {
/* "__init" was never called*/
            label = 0
            if (nodeid != NodeManager.nodeNullPointer) {
                nodeManager.releaseNode(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:49"/*SOURCE_FILE_END*/, nodeid)
            }
        } else if (label != 0) {
            label = 0
            if (nodeid != NodeManager.nodeNullPointer) {
                nodeManager.releaseNode(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:54"/*SOURCE_FILE_END*/, nodeid)
            }
            lock.readUnlock()
        }
    }

    override /*suspend*/ fun close() {
        _close()
    }

    /*suspend*/ internal inline fun updateRemaining(crossinline setDone: () -> Unit = {}) {
        SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:65"/*SOURCE_FILE_END*/ }, { remaining > 0 })
        remaining--
        if (remaining == 0) {
            needsReset = true
            offset = NodeLeaf.START_OFFSET
            val nextnodeid = NodeShared.getNextNode(node)
            nodeManager.releaseNode(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:71"/*SOURCE_FILE_END*/, nodeid)
            nodeid = nextnodeid
            if (nodeid != NodeManager.nodeNullPointer) {
                nodeManager.getNodeLeaf(/*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:74"/*SOURCE_FILE_END*/, nodeid) {
                    SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:75"/*SOURCE_FILE_END*/ }, { node != it })
                    node = it
                }
                remaining = NodeShared.getTripleCount(node)
            } else {
                _close()
                setDone()
            }
        }
        SanityCheck.check({ /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_triple_store_id_triple/src/commonMain/kotlin/lupos/triple_store_id_triple/index_IDTriple/NodeLeafColumnIterator.kt:84"/*SOURCE_FILE_END*/ }, { remaining > 0 || label == 0 })
    }
}
