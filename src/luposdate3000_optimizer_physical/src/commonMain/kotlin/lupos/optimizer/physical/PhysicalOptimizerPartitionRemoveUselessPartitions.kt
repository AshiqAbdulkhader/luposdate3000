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
package lupos.optimizer.physical

import lupos.operator.base.Query
import lupos.operator.physical.partition.POPChangePartitionOrderedByIntId
import lupos.operator.physical.partition.POPMergePartition
import lupos.operator.physical.partition.POPMergePartitionCount
import lupos.operator.physical.partition.POPMergePartitionOrderedByIntId
import lupos.operator.physical.partition.POPSplitMergePartitionFromStore
import lupos.operator.physical.partition.POPSplitPartition
import lupos.operator.physical.partition.POPSplitPartitionFromStore
import lupos.operator.physical.partition.POPSplitPartitionFromStoreCount
import lupos.optimizer.logical.EOptimizerIDExt
import lupos.optimizer.logical.OptimizerBase
import lupos.shared.EPartitionModeExt
import lupos.shared.operator.IOPBase
import lupos.triple_store_manager.POPTripleStoreIterator

public class PhysicalOptimizerPartitionRemoveUselessPartitions(query: Query) : OptimizerBase(query, EOptimizerIDExt.PhysicalOptimizerPartitionRemoveUselessPartitionsID, "PhysicalOptimizerPartitionRemoveUselessPartitions") {
    // this optimizer removes useless partitioning operators
    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res = node
        if (query.getInstance().LUPOS_PARTITION_MODE == EPartitionModeExt.Thread || query.getInstance().LUPOS_PARTITION_MODE == EPartitionModeExt.Process) {
            when (node) {
                is POPSplitPartitionFromStore -> {
                    if (node.partitionCount == 1) {
                        res = node.children[0]
                        var storeNodeTmp = node.children[0]
                        while (storeNodeTmp !is POPTripleStoreIterator) {
// this is POPDebug or something similar with is not affecting the calculation - otherwise this node wont be POPSplitPartitionFromStore
                            storeNodeTmp = storeNodeTmp.getChildren()[0]
                        }
                        val storeNode = storeNodeTmp
                        query.removePartitionOperator(node.getUUID(), node.partitionID)
                        if (storeNode.requireSplitFromStore()) {
                            val requiredPartition = storeNode.requiresPartitioning()
                            if (requiredPartition.size != 0) {
                                var ids = mutableListOf<Int>()
                                res = storeNode
                                for ((key, count) in requiredPartition) {
                                    val partitionID = query.getNextPartitionOperatorID()
                                    ids.add(partitionID)
                                    res = POPSplitPartitionFromStore(query, res.getProvidedVariableNames(), key, count, partitionID, res)
                                    query.addPartitionOperator(res.getUUID(), partitionID)
                                }
                                for ((key, count) in requiredPartition) {
                                    val partitionID = ids.removeAt(0)
                                    res = POPMergePartition(query, res.getProvidedVariableNames(), key, count, partitionID, res)
                                    query.addPartitionOperator(res.getUUID(), partitionID)
                                }
                            } else {
                                res = POPSplitMergePartitionFromStore(query, res.getProvidedVariableNames(), node.partitionID, res)
                            }
                        } else {
                            storeNode.hasSplitFromStore = false
                        }
                        onChange()
                    }
                }
                is POPSplitPartitionFromStoreCount -> {
                    if (node.partitionCount == 1) {
                        res = node.children[0]
                        var storeNodeTmp = node.children[0]
                        while (storeNodeTmp !is POPTripleStoreIterator) {
// this is POPDebug or something similar with is not affecting the calculation - otherwise this node wont be POPSplitPartitionFromStoreCount
                            storeNodeTmp = storeNodeTmp.getChildren()[0]
                        }
                        val storeNode = storeNodeTmp
                        storeNode.hasSplitFromStore = false
                        query.removePartitionOperator(node.getUUID(), node.partitionID)
                        onChange()
                    }
                }
                is POPSplitPartition -> {
                    if (node.partitionCount == 1) {
                        res = node.children[0]
                        query.removePartitionOperator(node.getUUID(), node.partitionID)
                        onChange()
                    }
                }
                is POPMergePartition -> {
                    if (node.partitionCount == 1) {
                        res = node.children[0]
                        query.removePartitionOperator(node.getUUID(), node.partitionID)
                        onChange()
                    }
                }
                is POPMergePartitionCount -> {
                    if (node.partitionCount == 1) {
                        res = node.children[0]
                        query.removePartitionOperator(node.getUUID(), node.partitionID)
                        onChange()
                    }
                }
                is POPMergePartitionOrderedByIntId -> {
                    if (node.partitionCount2 == 1) {
                        res = node.children[0]
                        query.removePartitionOperator(node.getUUID(), node.partitionID)
                        onChange()
                    }
                }
                is POPChangePartitionOrderedByIntId -> {
                    if (node.partitionCountFrom == 1 && node.partitionCountTo == 1) {
                        res = node.children[0]
                        query.removePartitionOperator(node.getUUID(), node.partitionIDFrom)
                        query.removePartitionOperator(node.getUUID(), node.partitionIDTo)
                        onChange()
                    } else if (node.partitionCountFrom == 1) {
                        res = POPSplitPartition(query, node.children[0].getProvidedVariableNames(), node.partitionVariable, node.partitionCountTo, node.partitionIDTo, node.children[0])
                        query.removePartitionOperator(node.getUUID(), node.partitionIDFrom)
                        query.removePartitionOperator(node.getUUID(), node.partitionIDTo)
                        query.addPartitionOperator(res.getUUID(), res.partitionID)
                        onChange()
                    } else if (node.partitionCountTo == 1) {
                        res = POPMergePartitionOrderedByIntId(query, node.children[0].getProvidedVariableNames(), node.partitionVariable, node.partitionCountFrom, node.partitionIDFrom, node.children[0])
                        res.setMySortPriority(node.mySortPriority, node.children[0].getProvidedVariableNames())
                        query.removePartitionOperator(node.getUUID(), node.partitionIDFrom)
                        query.removePartitionOperator(node.getUUID(), node.partitionIDTo)
                        query.addPartitionOperator(res.getUUID(), res.partitionID)
                        onChange()
                    }
                }
            }
        }
        return res
    }
}
