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
package lupos.operator.factory
import lupos.operator.arithmetik.AOPBase
import lupos.operator.arithmetik.noinput.AOPConstant
import lupos.operator.base.OPBase
import lupos.operator.base.OPBaseCompound
import lupos.operator.base.Query
import lupos.operator.physical.multiinput.POPJoinCartesianProduct
import lupos.operator.physical.multiinput.POPJoinHashMap
import lupos.operator.physical.multiinput.POPJoinMerge
import lupos.operator.physical.multiinput.POPJoinMergeOptional
import lupos.operator.physical.multiinput.POPJoinMergeSingleColumn
import lupos.operator.physical.multiinput.POPMinus
import lupos.operator.physical.multiinput.POPUnion
import lupos.operator.physical.noinput.POPGraphOperation
import lupos.operator.physical.noinput.POPModifyData
import lupos.operator.physical.noinput.POPNothing
import lupos.operator.physical.noinput.POPValues
import lupos.operator.physical.partition.POPMergePartition
import lupos.operator.physical.partition.POPMergePartitionCount
import lupos.operator.physical.partition.POPMergePartitionOrderedByIntId
import lupos.operator.physical.partition.POPSplitPartition
import lupos.operator.physical.singleinput.POPBind
import lupos.operator.physical.singleinput.POPFilter
import lupos.operator.physical.singleinput.POPGroup
import lupos.operator.physical.singleinput.POPMakeBooleanResult
import lupos.operator.physical.singleinput.POPModify
import lupos.operator.physical.singleinput.POPSort
import lupos.operator.physical.singleinput.modifiers.POPLimit
import lupos.operator.physical.singleinput.modifiers.POPOffset
import lupos.operator.physical.singleinput.modifiers.POPReduced
import lupos.shared.DictionaryValueHelper
import lupos.shared.EOperatorIDExt
import lupos.shared.Partition
import lupos.shared.SanityCheck
import lupos.shared.dynamicArray.ByteArrayWrapper
import lupos.shared.inline.dynamicArray.ByteArrayWrapperExt
import lupos.shared.operator.IOPBase
import lupos.shared.operator.noinput.IAOPConstant
import lupos.shared.operator.noinput.IAOPVariable
import lupos.triple_store_manager.POPTripleStoreIterator

public class BinaryMetadataHandler(
    internal val idToOffset: MutableMap<Int, Int>,
    internal val idToHost: MutableMap<Int, MutableSet<String>>,
    internal val dependenciesForID: MutableMap<Int, MutableMap<Int, Int>>,
    internal val keyLocationSrc: MutableMap<Int, Int>,
    internal val keyLocationDest: MutableMap<Int, Int>,
) {
    internal fun getParentsForID(childID: Int): Set<Int> {
        var res = mutableSetOf<Int>()
        if (childID != -1) {
            loop@ for ((parentID, vv) in dependenciesForID) {
                for (v in vv.keys) {
                    if (v == childID) {
                        res.add(parentID)
                    }
                }
            }
        }
        return res
    }
}

public class ConverterPOPBaseToBinaryDistributionHandler {
    // entry points into binary
    internal val idToOffset = mutableMapOf<Int/*ID*/, Int/*the offset of the operator*/>()

    // for writing it down
    internal var currentID = -1 //
    internal val partitionVariables = mutableMapOf<Int, String>()/*partitionID->Variable*/
    internal val partitionCount = mutableMapOf<Int, Int>()/*partitionID->partitionCount*/
    internal val partitionToChildID = mutableListOf<Pair<Pair<MutableMap<Int, Int>, Long>, Int>>()/*(thePartition,operatorID)->childID*/
    internal var currentPartition = mutableMapOf<Int, Int>()/*partitionID->partitionIndex*/
    internal val partitionToKey = mutableMapOf<Int, MutableMap<Long, IntArray>>()/*ID->(operatorID->keys)*/
    internal var keys = 0

    // for optimization
    internal val idToHost = mutableMapOf<Int, MutableSet<String>>()/*ID->(hostname)*/
    internal val dependenciesForID = mutableMapOf<Int, MutableMap<Int, Int>>()/*parentID->childID->key*/
    internal val keyLocationSrc = mutableMapOf<Int, Int>()/*key->off*/
    internal val keyLocationDest = mutableMapOf<Int, Int>()/*key->offset, where the offset points to the operator, to enable efficient replacement*/

    internal fun keyLocationSend(id: Int, off: Int) {
        if (keyLocationSrc[id] != null) {
            TODO("")
        }
        keyLocationSrc[id] = off
    }

    internal fun keyLocationReceive(id: Int, off: Int) {
        if (keyLocationDest[id] != null) {
            TODO("")
        }
        keyLocationDest[id] = off
    }

    internal fun getNextChildID(): Int {
        for (i in 0 until idToOffset.size + 1) {
            if (!idToOffset.contains(i)) {
                return i
            }
        }
        return -1 // unreachable
    }

    internal fun getParentsForID(childID: Int): Set<Int> {
        var res = mutableSetOf<Int>()
        if (childID != -1) {
            loop@ for ((parentID, vv) in dependenciesForID) {
                for (v in vv.keys) {
                    if (v == childID) {
                        res.add(parentID)
                    }
                }
            }
        }
        return res
    }
}
public typealias OPBaseToBinaryMap = (op: IOPBase, data: ByteArrayWrapper, mapping: MutableMap<String, Int>, distributed: Boolean, handler: ConverterPOPBaseToBinaryDistributionHandler, offPtr: Int) -> Int/*offset*/

public object ConverterPOPBaseToBinary {
    internal var enableOptimiation = true
    public var operatorMap: Array<OPBaseToBinaryMap?> = Array(0) { null }

    public fun assignOperatorPhysicalEncode(operatorIDs: IntArray, operator: OPBaseToBinaryMap) {
        for (operatorID in operatorIDs) {
            assignOperatorPhysicalEncode(operatorID, operator)
        }
    }

    public fun assignOperatorPhysicalEncode(operatorID: Int, operator: OPBaseToBinaryMap) {
        if (operatorMap.size <= operatorID) {
            var s = operatorMap.size
            if (s < 16) {
                s = 16
            }
            while (s <= operatorID) {
                s = s * 2
            }
            val tmp = Array<OPBaseToBinaryMap?>(s) { null }
            operatorMap.copyInto(tmp)
            operatorMap = tmp
        }
        operatorMap[operatorID] = operator
    }
    public fun optimize(data: ByteArrayWrapper, handler: BinaryMetadataHandler, query: Query): ByteArrayWrapper {
        // alle hosts zuweisen
        var queue: MutableList<Int>
        for (i in 0 until 2) {
// the first section needs to be done twice
            queue = handler.idToHost.keys.toMutableList()
            while (!queue.isEmpty()) {
                val childID = queue.removeAt(0)
                val hostnames = handler.idToHost[childID]!!
                var parentIDs = handler.getParentsForID(childID)
                for (parentID in parentIDs) {
                    var parentHost = handler.idToHost[parentID]
                    if (parentHost == null) {
                        parentHost = mutableSetOf<String>()
                        handler.idToHost[parentID] = parentHost
                    }
                    if (!parentHost.containsAll(hostnames)) {
                        parentHost.addAll(hostnames)
                        queue.add(parentID)
                    }
                }
            }
            val localhost = query.getInstance().tripleStoreManager!!.getLocalhost()
            queue = handler.idToOffset.keys.toMutableList()
            while (!queue.isEmpty()) {
                val childID = queue.removeAt(0)
                if (handler.idToHost[childID] == null) {
                    handler.idToHost[childID] = mutableSetOf(localhost)
                }
            }
        }
        val rootAddress = query.getInstance().LUPOS_PROCESS_URLS_STORE[0]
        queue = handler.idToOffset.keys.toMutableList()
        while (!queue.isEmpty()) {
            val childID = queue.removeAt(0)
            if (handler.idToHost[childID]!!.contains(rootAddress)) {
                handler.idToHost[childID] = mutableSetOf(rootAddress)
            }
        }
        if (enableOptimiation) {
            // mergen wenn beide gleichen (oder gar keinen) host haben
            queue = handler.idToOffset.keys.toMutableList()
            while (!queue.isEmpty()) {
                val childID = queue.removeAt(0)
                val hostname = handler.idToHost[childID]
                var parentIDs = handler.getParentsForID(childID)
                for (parentID in parentIDs) {
                    var parentHost = handler.idToHost[parentID]
                    if ((hostname != null && parentHost != null) && (hostname.size == parentHost.size && parentHost.containsAll(hostname))) {
                        val key = handler.dependenciesForID[parentID]!![childID]!!
                        val parentOffOff = handler.keyLocationDest[key]!!
                        val parentOff = ByteArrayWrapperExt.readInt4(data, parentOffOff, { "*.child" })
                        val childOff = handler.keyLocationSrc[key]!!
                        val parentType = ByteArrayWrapperExt.readInt4(data, parentOff, { "operatorID" })
                        val childType = ByteArrayWrapperExt.readInt4(data, childOff, { "operatorID" })
                        if (parentType == EOperatorIDExt.POPDistributedReceiveSingleID && childType == EOperatorIDExt.POPDistributedSendSingleID) {
                            val childrensOff = ByteArrayWrapperExt.readInt4(data, childOff + 8, { "POPDistributedSendSingle.child" })
                            ByteArrayWrapperExt.writeInt4(data, parentOffOff, childrensOff, { "*.child" })
                            fixAfterMerge(data, handler, childrensOff, parentOffOff, key, childID, parentID)
                        } else if (parentType == EOperatorIDExt.POPDistributedReceiveSingleCountID && childType == EOperatorIDExt.POPDistributedSendSingleCountID) {
                            val childrensOff = ByteArrayWrapperExt.readInt4(data, childOff + 8, { "POPDistributedSendSingleCount.child" })
                            ByteArrayWrapperExt.writeInt4(data, parentOffOff, childrensOff, { "*.child" })
                            fixAfterMerge(data, handler, childrensOff, parentOffOff, key, childID, parentID)
                        }
                    }
                }
            }
        }
        return data
    }
    private fun fixAfterMerge(data: ByteArrayWrapper, handler: BinaryMetadataHandler, childOff: Int, parentOffOff: Int, theKey: Int, childID: Int, parentID: Int) {
        handler.idToOffset.remove(childID)
        handler.idToHost.remove(childID)
        val depa = handler.dependenciesForID[parentID]!!
        val depb = handler.dependenciesForID[childID]
        if (depb != null) {
            depa.putAll(depb)
        }
        depa.remove(childID)
        handler.keyLocationSrc.remove(theKey)
        handler.keyLocationDest.remove(theKey)
        val childrensType = ByteArrayWrapperExt.readInt4(data, childOff, { "operatorID" })
        when (childrensType) {
            EOperatorIDExt.POPDistributedReceiveSingleID -> {
                val key = ByteArrayWrapperExt.readInt4(data, childOff + 4, { "POPDistributedReceiveSingle.key" })
                handler.keyLocationDest[key] = parentOffOff
            }
            EOperatorIDExt.POPDistributedReceiveSingleCountID -> {
                val key = ByteArrayWrapperExt.readInt4(data, childOff + 4, { "POPDistributedReceiveSingleCount.key" })
                handler.keyLocationDest[key] = parentOffOff
            }
            EOperatorIDExt.POPDistributedReceiveMultiID -> {
                val len = ByteArrayWrapperExt.readInt4(data, childOff + 4, { "POPDistributedReceiveMulti.size" })
                for (i in 0 until len) {
                    val key = ByteArrayWrapperExt.readInt4(data, childOff + 8 + 4 * i, { "POPDistributedReceiveMulti.key[$i]" })
                    handler.keyLocationDest[key] = parentOffOff
                }
            }
            EOperatorIDExt.POPDistributedReceiveMultiCountID -> {
                val len = ByteArrayWrapperExt.readInt4(data, childOff + 4, { "POPDistributedReceiveMultiCount.size" })
                for (i in 0 until len) {
                    val key = ByteArrayWrapperExt.readInt4(data, childOff + 8 + 4 * i, { "POPDistributedReceiveMultiCount.key[$i]" })
                    handler.keyLocationDest[key] = parentOffOff
                }
            }
            EOperatorIDExt.POPDistributedReceiveMultiOrderedID -> {
                val len = ByteArrayWrapperExt.readInt4(data, childOff + 4, { "POPDistributedReceiveMultiOrdered.size" })
                for (i in 0 until len) {
                    val key = ByteArrayWrapperExt.readInt4(data, childOff + 8 + 4 * i, { "POPDistributedReceiveMultiOrdered.key[$i]" })
                    handler.keyLocationDest[key] = parentOffOff
                }
            }
        }
    }
    public fun encode(op: IOPBase, distributed: Boolean): Pair<ByteArrayWrapper, BinaryMetadataHandler> {
        val handler = ConverterPOPBaseToBinaryDistributionHandler()
        val mapping = mutableMapOf<String, Int>()
        var data = ByteArrayWrapper()
        if (op is OPBaseCompound) {
            ByteArrayWrapperExt.setSize(data, 9 + 8 * op.children.size + op.columnProjectionOrder.map { it.size }.sum() * 4, false)
            ByteArrayWrapperExt.writeInt1(data, 4, 0x1, { "Root.isOPBaseCompound" })
            ByteArrayWrapperExt.writeInt4(data, 5, op.children.size, { "OPBaseCompound.children.size" })
            var o = 9
            for (i in 0 until op.children.size) {
                val k = if (op.columnProjectionOrder.size > i) {
                    op.columnProjectionOrder[i]
                } else {
                    listOf()
                }
                val child = convertToByteArrayHelper(op.children[i], data, mapping, distributed, handler, o)
                ByteArrayWrapperExt.writeInt4(data, o, child, { "OPBaseCompound.children[$i]" })
                o += 4
                ByteArrayWrapperExt.writeInt4(data, o, k.size, { "OPBaseCompound.columnProjectionOrder[$i].size" })
                o += 4
                for (j in 0 until k.size) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(k[j], data, mapping), { "OPBaseCompound.columnProjectionOrder[$i][$j]" })
                    o += 4
                }
            }
        } else {
            ByteArrayWrapperExt.setSize(data, 9, false)
            val off = convertToByteArrayHelper(op, data, mapping, distributed, handler, 5)
            ByteArrayWrapperExt.writeInt1(data, 4, 0x0, { "OPBase.isOPBaseCompound" })
            ByteArrayWrapperExt.writeInt4(data, 5, off, { "OPBase.children[0]" })
        }
        val handler2 = BinaryMetadataHandler(handler.idToOffset, handler.idToHost, handler.dependenciesForID, handler.keyLocationSrc, handler.keyLocationDest)
        data = optimize(data, handler2, op.getQuery() as Query)
        val off = ByteArrayWrapperExt.getSize(data)
        ByteArrayWrapperExt.writeInt4(data, 0, off, { "OPBase.handler" })
        ByteArrayWrapperExt.setSize(data, off + 4 + 8 * handler2.idToOffset.size, true)
        ByteArrayWrapperExt.writeInt4(data, off, handler2.idToOffset.size, { "OPBase.offsetMap.size" })
        var o = off + 4
        var i = 0
        for ((k, v) in handler2.idToOffset) {
            ByteArrayWrapperExt.writeInt4(data, o, k, { "OPBase.offsetMap[$i].id" })
            ByteArrayWrapperExt.writeInt4(data, o + 4, v, { "OPBase.offsetMap[$i].offset" })
            o += 8
            i++
        }
        return data to handler2
    }

    private fun convertToByteArrayHelper(op: IOPBase, data: ByteArrayWrapper, mapping: MutableMap<String, Int>, distributed: Boolean, handler: ConverterPOPBaseToBinaryDistributionHandler, offPtr: Int): Int {
        if ((op as OPBase).operatorID >= operatorMap.size) {
            TODO("convertToByteArrayHelper ${(op as OPBase).operatorID} -> ${EOperatorIDExt.names[(op as OPBase).operatorID]}")
        }
        val encoder = operatorMap[(op as OPBase).operatorID]
        if (encoder == null) {
            TODO("convertToByteArrayHelper ${(op as OPBase).operatorID} -> ${EOperatorIDExt.names[(op as OPBase).operatorID]}")
        }
        return encoder(op, data, mapping, distributed, handler, offPtr)
    }

    private fun mergePartitionEncodeHelperSplit(partitionCount: Int, handler: ConverterPOPBaseToBinaryDistributionHandler, currentID: Int, data: ByteArrayWrapper, partitionVariable: String?, mapping: MutableMap<String, Int>, distributed: Boolean, child: OPBase, labelAppendixReceive: String, labelAppendixSend: String, sendID: Int, receiveID: Int, partitionID: Int, operatorID: Long, offPtr: Int): Int {
        val currentPartitionCopy = mutableMapOf<Int, Int>()
        for ((k, v) in handler.currentPartition) {
            currentPartitionCopy[k] = v
        }
        val partition = handler.currentPartition[partitionID]!!
        handler.partitionCount[partitionID] = partitionCount
        handler.currentPartition.remove(partitionID)
        if (partitionVariable != null) {
            handler.partitionVariables[partitionID] = partitionVariable
        }
        var childID = -1
        loop@ for (kk in handler.partitionToChildID) {
            val thePartition = kk.first.first
            val theOperatorID = kk.first.second
            val theChildID = kk.second
            if (theOperatorID != operatorID) {
                continue@loop
            }
            for ((k, v) in handler.currentPartition) {
                if (thePartition[k] != v) {
                    continue@loop
                }
            }
            for ((k, v) in thePartition) {
                if (handler.currentPartition[k] != v) {
                    continue@loop
                }
            }
            childID = theChildID
            break@loop
        }
        val off: Int
        var keys0 = handler.partitionToKey[childID]
        if (keys0 == null) {
            keys0 = mutableMapOf<Long, IntArray>()
            handler.partitionToKey[childID] = keys0
        }
        var keys = keys0[operatorID]
        if (keys == null) {
            val kk = handler.keys
            handler.keys += partitionCount
            keys = IntArray(partitionCount) { kk + it }
            keys0[operatorID] = keys
        }
        if (childID == -1) {
            childID = handler.getNextChildID()
            handler.idToOffset[childID] = -1
            handler.currentID = childID
            var deps = handler.dependenciesForID[currentID]
            if (deps == null) {
                handler.dependenciesForID[currentID] = mutableMapOf(childID to keys[partition])
            } else {
                deps[childID] = keys[partition]
            }
            off = ByteArrayWrapperExt.getSize(data)
            handler.idToOffset[childID] = off + 8
            handler.keyLocationSend(keys[partition], off + 8)
            ByteArrayWrapperExt.setSize(data, off + 20 + 4 * partitionCount, true)
            val child = convertToByteArrayHelper(child, data, mapping, distributed, handler, off + 12)
            ByteArrayWrapperExt.writeInt4(data, off + 8, sendID, { "operatorID" })
            ByteArrayWrapperExt.writeInt4(data, off + 12, child, { "POPDistributedSendMulti$labelAppendixSend.child" })
            ByteArrayWrapperExt.writeInt4(data, off + 16, partitionCount, { "POPDistributedSendMulti$labelAppendixSend.count" })
            ByteArrayWrapperExt.writeInt4(data, off + 20 + 4 * partition, keys[partition], { "POPDistributedSendMulti$labelAppendixSend.key[$partition]" })
            handler.currentID = currentID
            val cpy = mutableMapOf<Int, Int>()
            for ((k, v) in handler.currentPartition) {
                cpy[k] = v
            }
            handler.partitionToChildID.add((cpy to operatorID) to childID)
        } else {
            off = ByteArrayWrapperExt.getSize(data)
            ByteArrayWrapperExt.setSize(data, off + 8, true)
            var deps = handler.dependenciesForID[currentID]
            if (deps == null) {
                handler.dependenciesForID[currentID] = mutableMapOf(childID to keys[partition])
            } else {
                deps[childID] = keys[partition]
            }
            val childOff = handler.idToOffset[childID]!!
            handler.keyLocationSend(keys[partition], childOff)
            ByteArrayWrapperExt.writeInt4(data, childOff + 12 + 4 * partition, keys[partition], { "POPDistributedSendMulti$labelAppendixSend.key[$partition]" })
        }
        handler.keyLocationReceive(keys[partition], offPtr)
        ByteArrayWrapperExt.writeInt4(data, off + 0, receiveID, { "operatorID" })
        ByteArrayWrapperExt.writeInt4(data, off + 4, keys[partition], { "POPDistributedReceiveSingle$labelAppendixReceive.key" })

        handler.currentPartition = currentPartitionCopy
        return off
    }

    private fun mergePartitionEncodeHelperMerge(partitionCount: Int, handler: ConverterPOPBaseToBinaryDistributionHandler, currentID: Int, data: ByteArrayWrapper, partitionVariable: String?, mapping: MutableMap<String, Int>, distributed: Boolean, child: OPBase, labelAppendixReceive: String, labelAppendixSend: String, sendID: Int, receiveID: Int, partitionID: Int, operatorID: Long, offPtr: Int): Int {
        val currentPartitionCopy = mutableMapOf<Int, Int>()
        for ((k, v) in handler.currentPartition) {
            currentPartitionCopy[k] = v
        }
        val childsOff = mutableListOf<Int>()
        val childIDs = mutableListOf<Int>()
        if (partitionVariable != null) {
            handler.partitionVariables[partitionID] = partitionVariable
        }
        handler.partitionCount[partitionID] = partitionCount
        var keys0 = handler.partitionToKey[currentID]
        if (keys0 == null) {
            keys0 = mutableMapOf<Long, IntArray>()
            handler.partitionToKey[currentID] = keys0
        }
        var keys = keys0[operatorID]
        if (keys == null) {
            keys = IntArray(partitionCount) { handler.keys + it }
            handler.keys += partitionCount
            keys0[operatorID] = keys
        }
        val off = ByteArrayWrapperExt.getSize(data)
        ByteArrayWrapperExt.setSize(data, off + 8 + 16 * partitionCount, true)
        ByteArrayWrapperExt.writeInt4(data, off + 0, receiveID, { "operatorID" })
        ByteArrayWrapperExt.writeInt4(data, off + 4, partitionCount, { "POPDistributedReceiveMulti$labelAppendixReceive.size" })
        for (partition in 0 until partitionCount) {
            handler.currentPartition[partitionID] = partition
            var childID = handler.getNextChildID()
            handler.idToOffset[childID] = -1
            handler.currentID = childID
            var deps = handler.dependenciesForID[currentID]
            if (deps == null) {
                handler.dependenciesForID[currentID] = mutableMapOf(childID to keys[partition])
            } else {
                deps[childID] = keys[partition]
            }
            var o = off + 8 + 4 * partitionCount + 12 * partition
            val child = convertToByteArrayHelper(child, data, mapping, distributed, handler, o + 8)
            childIDs.add(childID)
            handler.keyLocationReceive(keys[partition], offPtr)
            ByteArrayWrapperExt.writeInt4(data, off + 8 + 4 * partition, keys[partition], { "POPDistributedReceiveMulti$labelAppendixReceive.key[$partition]" })
            handler.keyLocationSend(keys[partition], o)
            handler.idToOffset[childIDs[partition]] = o
            ByteArrayWrapperExt.writeInt4(data, o + 0, sendID, { "operatorID" })
            ByteArrayWrapperExt.writeInt4(data, o + 4, keys[partition], { "POPDistributedSendSingle$labelAppendixSend.key" })
            ByteArrayWrapperExt.writeInt4(data, o + 8, child, { "POPDistributedSendSingle$labelAppendixSend.child" })
        }
        handler.currentID = currentID
        handler.currentPartition = currentPartitionCopy
        return off
    }

    private fun mergePartitionEncodeHelper1x1(handler: ConverterPOPBaseToBinaryDistributionHandler, currentID: Int, data: ByteArrayWrapper, mapping: MutableMap<String, Int>, distributed: Boolean, child: OPBase, labelAppendix: String, sendID: Int, receiveID: Int, offPtr: Int): Int {
        var childID = handler.getNextChildID()
        handler.idToOffset[childID] = -1
        handler.currentID = childID
        val key = handler.keys++
        var deps = handler.dependenciesForID[currentID]
        if (deps == null) {
            handler.dependenciesForID[currentID] = mutableMapOf(childID to key)
        } else {
            deps[childID] = key
        }
        val off = ByteArrayWrapperExt.getSize(data)
        handler.keyLocationReceive(key, offPtr)
        handler.keyLocationSend(key, off + 8)
        ByteArrayWrapperExt.setSize(data, off + 20, true)
        val child = convertToByteArrayHelper(child, data, mapping, distributed, handler, off + 16)
        ByteArrayWrapperExt.writeInt4(data, off + 0, receiveID, { "operatorID" })
        ByteArrayWrapperExt.writeInt4(data, off + 4, key, { "POPDistributedReceiveSingle$labelAppendix.key" })
        ByteArrayWrapperExt.writeInt4(data, off + 8, sendID, { "operatorID" })
        ByteArrayWrapperExt.writeInt4(data, off + 12, key, { "POPDistributedSendSingle$labelAppendix.key" })
        ByteArrayWrapperExt.writeInt4(data, off + 16, child, { "POPDistributedSendSingle$labelAppendix.child" })
        handler.idToOffset[childID] = off + 8
        handler.currentID = currentID
        return off
    }

    public fun initEncode() {
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPMergePartitionCountID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPMergePartitionCount
                val currentID = handler.currentID
                if (distributed) {
                    if (op.partitionCount > 1) {
                        mergePartitionEncodeHelperMerge(op.partitionCount, handler, currentID, data, op.partitionVariable, mapping, distributed, op.children[0] as OPBase, "Count", "Count", EOperatorIDExt.POPDistributedSendSingleCountID, EOperatorIDExt.POPDistributedReceiveMultiCountID, op.partitionID, op.uuid, offPtr)
                    } else {
                        mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "Count", EOperatorIDExt.POPDistributedSendSingleCountID, EOperatorIDExt.POPDistributedReceiveSingleCountID, offPtr)
                    }
                } else {
                    TODO("ConverterPOPBaseToBinary.POPMergePartitionCount")
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPMergePartitionOrderedByIntIdID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPMergePartitionOrderedByIntId
                val currentID = handler.currentID
                if (distributed) {
                    if (op.partitionCount2 > 1) {
                        mergePartitionEncodeHelperMerge(op.partitionCount2, handler, currentID, data, op.partitionVariable, mapping, distributed, op.children[0] as OPBase, "Ordered", "", EOperatorIDExt.POPDistributedSendSingleID, EOperatorIDExt.POPDistributedReceiveMultiOrderedID, op.partitionID, op.uuid, offPtr)
                    } else {
                        mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "", EOperatorIDExt.POPDistributedSendSingleID, EOperatorIDExt.POPDistributedReceiveSingleID, offPtr)
                    }
                } else {
                    TODO("ConverterPOPBaseToBinary.POPMergePartitionOrderedByIntId")
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPMergePartitionID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPMergePartition
                val currentID = handler.currentID
                if (distributed) {
                    if (op.partitionCount > 1) {
                        mergePartitionEncodeHelperMerge(op.partitionCount, handler, currentID, data, op.partitionVariable, mapping, distributed, op.children[0] as OPBase, "", "", EOperatorIDExt.POPDistributedSendSingleID, EOperatorIDExt.POPDistributedReceiveMultiID, op.partitionID, op.uuid, offPtr)
                    } else {
                        mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "", EOperatorIDExt.POPDistributedSendSingleID, EOperatorIDExt.POPDistributedReceiveSingleID, offPtr)
                    }
                } else {
                    TODO("ConverterPOPBaseToBinary.POPMergePartition")
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPSplitPartitionID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPSplitPartition
                val currentID = handler.currentID
                if (distributed) {
                    if (op.partitionCount > 1) {
                        mergePartitionEncodeHelperSplit(op.partitionCount, handler, currentID, data, op.partitionVariable, mapping, distributed, op.children[0] as OPBase, "", "", EOperatorIDExt.POPDistributedSendMultiID, EOperatorIDExt.POPDistributedReceiveSingleID, op.partitionID, op.uuid, offPtr)
                    } else {
                        mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "", EOperatorIDExt.POPDistributedSendSingleID, EOperatorIDExt.POPDistributedReceiveSingleID, offPtr)
                    }
                } else {
                    TODO("ConverterPOPBaseToBinary.POPSplitPartition")
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPGraphOperationID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPGraphOperation
                ConverterBinaryEncoder.encodePOPGraphOperation(
                    data,
                    mapping,
                    op.graph1type,
                    op.graph2type,
                    op.action,
                    op.graph1iri,
                    op.graph2iri,
                    op.silent,
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPProjectionID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as OPBase
                convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, offPtr)
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPDebugID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as OPBase
                convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, offPtr)
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPSplitPartitionFromStoreCountID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as OPBase
                val currentID = handler.currentID
                if (distributed) {
                    mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "", EOperatorIDExt.POPDistributedSendSingleCountID, EOperatorIDExt.POPDistributedReceiveSingleCountID, offPtr)
                } else {
                    convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, offPtr)
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPSplitPartitionFromStoreID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as OPBase
                val currentID = handler.currentID
                if (distributed) {
                    mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "", EOperatorIDExt.POPDistributedSendSingleID, EOperatorIDExt.POPDistributedReceiveSingleID, offPtr)
                } else {
                    convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, offPtr)
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPSplitMergePartitionFromStoreID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as OPBase
                val currentID = handler.currentID
                if (distributed) {
                    mergePartitionEncodeHelper1x1(handler, currentID, data, mapping, distributed, op.children[0] as OPBase, "", EOperatorIDExt.POPDistributedSendSingleCountID, EOperatorIDExt.POPDistributedReceiveSingleCountID, offPtr)
                } else {
                    convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, offPtr)
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPModifyDataID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPModifyData
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 8 + op.data.size * (4 + 3 * DictionaryValueHelper.getSize()), true)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPModifyDataID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, op.data.size, { "POPModifyData.data.size" })
                var o = off + 8
                var i = 0
                for (t in op.data) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(t.graph, data, mapping), { "POPModifyData.data[$i].graph" })
                    o += 4
                    for (j in 0 until 3) {
                        DictionaryValueHelper.toByteArray(data, o, (t.children[j] as AOPConstant).value, { "POPModifyData.data[$i][$j]" })
                        o += DictionaryValueHelper.getSize()
                    }
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPNothingID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPNothing
                val n = op.getProvidedVariableNames()
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 8 + 4 * n.size, true)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPNothingID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, n.size, { "POPNothing.size" })
                var i = 0
                for (s in n) {
                    ByteArrayWrapperExt.writeInt4(data, off + 8 + i * 4, ConverterString.encodeString(s, data, mapping), { "POPNothing.data[$i]" })
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            intArrayOf(
                EOperatorIDExt.POPValuesID,
                EOperatorIDExt.POPValuesCountID,
            ),
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPValues
                val off = ByteArrayWrapperExt.getSize(data)
                if (op.rows == -1) {
                    val size = op.data[op.variables.first()]!!.size
                    ByteArrayWrapperExt.setSize(data, off + 12 + op.data.size * (4 + size * DictionaryValueHelper.getSize()), true)
                    ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPValuesID, { "operatorID" })
                    ByteArrayWrapperExt.writeInt4(data, off + 4, op.data.size, { "POPValues.columns.size" })
                    ByteArrayWrapperExt.writeInt4(data, off + 8, size, { "POPValues.rows.size" })
                    var o = off + 12
                    var column = 0
                    for ((col, rows) in op.data) {
                        ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(col, data, mapping), { "POPValues.column[$column]" })
                        o += 4
                        var i = 0
                        for (row in rows) {
                            i++
                            DictionaryValueHelper.toByteArray(data, o, row, { "POPValues.data[$column][$i]" })
                            o += DictionaryValueHelper.getSize()
                        }
                        SanityCheck.check(
                            { /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_operator_factory/src/commonMain/kotlin/lupos/operator/factory/ConverterPOPBaseToBinary.kt:701"/*SOURCE_FILE_END*/ },
                            { i == size }
                        )
                        column++
                    }
                } else {
                    ByteArrayWrapperExt.setSize(data, off + 8, true)
                    ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPValuesCountID, { "operatorID" })
                    ByteArrayWrapperExt.writeInt4(data, off + 4, op.rows, { "POPValueCount.rows.size" })
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPEmptyRowID,
            { op, data, mapping, distributed, handler, offPtr ->
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 4, true)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPEmptyRowID, { "operatorID" })
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPUnionID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPUnion
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 16 + 4 * op.projectedVariables.size, true)
                val child0 = convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, off + 4)
                val child1 = convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, off + 8)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPUnionID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, child0, { "POPUnion.child0" })
                ByteArrayWrapperExt.writeInt4(data, off + 8, child1, { "POPUnion.child1" })
                ByteArrayWrapperExt.writeInt4(data, off + 12, op.projectedVariables.size, { "POPUnion.variables.size" })
                var o = off + 16
                var i = 0
                for (s in op.projectedVariables) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(s, data, mapping), { "POPUnion.variables[$i]" })
                    o += 4
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPMinusID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPMinus
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 16 + 4 * op.projectedVariables.size, true)
                val child0 = convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, off + 4)
                val child1 = convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, off + 8)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPMinusID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, child0, { "POPMinus.child0" })
                ByteArrayWrapperExt.writeInt4(data, off + 8, child1, { "POPMinus.child1" })
                ByteArrayWrapperExt.writeInt4(data, off + 12, op.projectedVariables.size, { "POPMinus.variables.size" })
                var o = off + 16
                var i = 0
                for (s in op.projectedVariables) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(s, data, mapping), { "POPMinus.variables[$i]" })
                    o += 4
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPJoinMergeSingleColumnID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPJoinMergeSingleColumn
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 12, true)
                val child0 = convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, off + 4)
                val child1 = convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, off + 8)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPJoinMergeSingleColumnID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, child0, { "POPJoinMergeSingleColumn.child0" })
                ByteArrayWrapperExt.writeInt4(data, off + 8, child1, { "POPJoinMergeSingleColumn.child1" })
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPJoinMergeOptionalID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPJoinMergeOptional
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 16 + 4 * op.projectedVariables.size, true)
                val child0 = convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, off + 4)
                val child1 = convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, off + 8)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPJoinMergeOptionalID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, child0, { "POPJoinMergeOptional.child0" })
                ByteArrayWrapperExt.writeInt4(data, off + 8, child1, { "POPJoinMergeOptional.child1" })
                ByteArrayWrapperExt.writeInt4(data, off + 12, op.projectedVariables.size, { "POPJoinMergeOptional.variables.size" })
                var o = off + 16
                var i = 0
                for (s in op.projectedVariables) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(s, data, mapping), { "POPJoinMergeOptional.variables[$i]" })
                    o += 4
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPJoinMergeID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPJoinMerge
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 16 + 4 * op.projectedVariables.size, true)
                val child0 = convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, off + 4)
                val child1 = convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, off + 8)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPJoinMergeID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, child0, { "POPJoinMerge.child0" })
                ByteArrayWrapperExt.writeInt4(data, off + 8, child1, { "POPJoinMerge.child1" })
                ByteArrayWrapperExt.writeInt4(data, off + 12, op.projectedVariables.size, { "POPJoinMerge.variables.size" })
                var o = off + 16
                var i = 0
                for (s in op.projectedVariables) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(s, data, mapping), { "POPJoinMerge.variables[$i]" })
                    o += 4
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPJoinHashMapID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPJoinHashMap
                val off = ByteArrayWrapperExt.getSize(data)
                ByteArrayWrapperExt.setSize(data, off + 17 + 4 * op.projectedVariables.size, true)
                val child0 = convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, off + 4)
                val child1 = convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, off + 8)
                ByteArrayWrapperExt.writeInt4(data, off + 0, EOperatorIDExt.POPJoinHashMapID, { "operatorID" })
                ByteArrayWrapperExt.writeInt4(data, off + 4, child0, { "POPJoinHashMap.child0" })
                ByteArrayWrapperExt.writeInt4(data, off + 8, child1, { "POPJoinHashMap.child1" })
                ByteArrayWrapperExt.writeInt1(data, off + 12, if (op.optional) 1 else 0, { "POPJoinHashMap.optional" })
                ByteArrayWrapperExt.writeInt4(data, off + 13, op.projectedVariables.size, { "POPJoinHashMap.variables.size" })
                var o = off + 17
                var i = 0
                for (s in op.projectedVariables) {
                    ByteArrayWrapperExt.writeInt4(data, o, ConverterString.encodeString(s, data, mapping), { "POPJoinHashMap.variables[$i]" })
                    o += 4
                    i++
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPJoinCartesianProductID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPJoinCartesianProduct
                ConverterBinaryEncoder.encodePOPJoinCartesianProduct(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    { parentOffOff -> convertToByteArrayHelper(op.children[1], data, mapping, distributed, handler, parentOffOff) },
                    op.optional,
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPLimitID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPLimit
                ConverterBinaryEncoder.encodePOPLimit(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    op.limit,
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPSortID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPSort
                if (op.getProvidedVariableNames().size == 0) {
                    convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, offPtr)
                } else {
                    ConverterBinaryEncoder.encodePOPSort(
                        data,
                        mapping,
                        { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                        op.mySortPriority.map { it.variableName },
                        op.getProvidedVariableNames(),
                        op.sortBy.map { it.name },
                        op.sortOrder,
                    )
                }
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPOffsetID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPOffset
                ConverterBinaryEncoder.encodePOPOffset(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    op.offset,
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPMakeBooleanResultID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPMakeBooleanResult
                ConverterBinaryEncoder.encodePOPMakeBooleanResult(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPReducedID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPReduced
                ConverterBinaryEncoder.encodePOPReduced(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    op.projectedVariables.size,
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPTripleStoreIterator,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPTripleStoreIterator
                val parent: Partition = if (handler.currentPartition.size == 0) {
                    Partition()
                } else if (handler.currentPartition.size == 1) {
                    var partitionID = 0
                    var partitionIndex = 0
                    for ((k, v) in handler.currentPartition) {
                        partitionID = k
                        partitionIndex = v
                    }
                    val partitionVariable = handler.partitionVariables[partitionID]!!
                    val partitionCount = handler.partitionCount[partitionID]!!
                    Partition(Partition(), partitionVariable, partitionIndex, partitionCount)
                } else {
                    TODO("??? ${handler.currentPartition}")
                }
                val target = op.getTarget(parent)
                var hostsTmp = handler.idToHost[handler.currentID]
                if (hostsTmp != null) {
                    hostsTmp.add(target.first)
                } else {
                    hostsTmp = mutableSetOf(target.first)
                    handler.idToHost[handler.currentID] = hostsTmp
                }
                ConverterBinaryEncoder.encodePOPTripleStoreIterator(
                    data,
                    mapping,
                    target,
                    op.getIndexPattern(),
                    op.children[0] is IAOPConstant,
                    (op.children[0] as? IAOPConstant)?.getValue(),
                    (op.children[0] as? IAOPVariable)?.getName(),
                    op.children[1] is IAOPConstant,
                    (op.children[1] as? IAOPConstant)?.getValue(),
                    (op.children[1] as? IAOPVariable)?.getName(),
                    op.children[2] is IAOPConstant,
                    (op.children[2] as? IAOPConstant)?.getValue(),
                    (op.children[2] as? IAOPVariable)?.getName(),
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPBindID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPBind
                var hostsTmp = handler.idToHost[handler.currentID]
                val rootAddress = op.getQuery().getInstance().LUPOS_PROCESS_URLS_STORE[0]
                if (hostsTmp != null) {
                    hostsTmp!!.add(rootAddress)
                } else {
                    hostsTmp = mutableSetOf(rootAddress)
                    handler.idToHost[handler.currentID] = hostsTmp!!
                }
                ConverterBinaryEncoder.encodePOPBind(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    op.getProvidedVariableNames(),
                    ConverterAOPBaseToBinary.encodeAOP(op.children[1] as AOPBase, data, mapping),
                    op.name.name,
                )
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPFilterID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPFilter
                var hostsTmp = handler.idToHost[handler.currentID]
                val rootAddress = op.getQuery().getInstance().LUPOS_PROCESS_URLS_STORE[0]
                if (hostsTmp != null) {
                    hostsTmp!!.add(rootAddress)
                } else {
                    hostsTmp = mutableSetOf(rootAddress)
                    handler.idToHost[handler.currentID] = hostsTmp!!
                }
                ConverterBinaryEncoder.encodePOPFilter(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    op.getProvidedVariableNames(),
                    ConverterAOPBaseToBinary.encodeAOP(op.children[1] as AOPBase, data, mapping),
                )
            },
        )
        assignOperatorPhysicalEncode(
            intArrayOf(
                EOperatorIDExt.POPGroupID,
                EOperatorIDExt.POPGroupCount0ID,
                EOperatorIDExt.POPGroupCount1ID,
                EOperatorIDExt.POPGroupSortedID,
                EOperatorIDExt.POPGroupWithoutKeyColumnID,
            ),
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPGroup
                val keyColumnNames = op.by.map { it.name }
                var off = -1
                var done = false
                if (op.by.isEmpty()) {
                    off = ConverterBinaryEncoder.encodePOPGroupWithoutKeyColumn(
                        data,
                        mapping,
                        { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                        keyColumnNames,
                        op.projectedVariables,
                        op.bindings.map { it -> it.first to ConverterAOPBaseToBinary.encodeAOP(it.second, data, mapping) },
                    )
                    done = true
                } else if (op.canUseSortedInput()) {
                    off = ConverterBinaryEncoder.encodePOPGroupSorted(
                        data,
                        mapping,
                        { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                        keyColumnNames,
                        op.projectedVariables,
                        op.bindings.map { it -> it.first to ConverterAOPBaseToBinary.encodeAOP(it.second, data, mapping) },
                    )
                    done = true
                } else if (op.isCountOnly()) {
                    if (op.by.size == 0) {
                        off = ConverterBinaryEncoder.encodePOPGroupCount0(
                            data,
                            mapping,
                            { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                            op.bindings.toList().first().first,
                        )
                        done = true
                    } else if (op.by.size == 1) {
                        off = ConverterBinaryEncoder.encodePOPGroupCount1(
                            data,
                            mapping,
                            { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                            op.bindings.toList().first().first,
                            op.by[0].name,
                        )
                        done = true
                    }
                }
                if (!done) {
                    off = ConverterBinaryEncoder.encodePOPGroup(
                        data,
                        mapping,
                        { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                        keyColumnNames,
                        op.bindings.map { it -> it.first to ConverterAOPBaseToBinary.encodeAOP(it.second, data, mapping) },
                    )
                }
                off
            },
        )
        assignOperatorPhysicalEncode(
            EOperatorIDExt.POPModifyID,
            { op, data, mapping, distributed, handler, offPtr ->
                op as POPModify
                ConverterBinaryEncoder.encodePOPModify(
                    data,
                    mapping,
                    { parentOffOff -> convertToByteArrayHelper(op.children[0], data, mapping, distributed, handler, parentOffOff) },
                    op.modify,
                )
            },
        )
    }
}