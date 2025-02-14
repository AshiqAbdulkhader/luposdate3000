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
package lupos.optimizer.logical

import lupos.operator.arithmetik.noinput.AOPVariable
import lupos.operator.base.Query
import lupos.operator.base.noinput.OPEmptyRow
import lupos.operator.logical.multiinput.LOPJoin
import lupos.operator.logical.singleinput.LOPProjection
import lupos.operator.physical.noinput.POPNothing
import lupos.shared.ESortTypeExt
import lupos.shared.EmptyResultException
import lupos.shared.SanityCheck
import lupos.shared.SortHelper
import lupos.shared.operator.IOPBase

public class LogicalOptimizerJoinOrderML(query: Query) : OptimizerBase(query, EOptimizerIDExt.LogicalOptimizerJoinOrderID, "LogicalOptimizerJoinOrder") {
    private val joinOrder: Int = query.getInstance().machineLearningOptimizerOrder
    private fun findAllJoinsInChildren(node: LOPJoin): List<IOPBase> {
        val res = mutableListOf<IOPBase>()
        for (c in node.getChildren()) {
            if (c is LOPJoin && !c.optional) {
                res.addAll(findAllJoinsInChildren(c))
            } else if (c is LOPProjection) {
                var d = c.getChildren()[0]
                while (d is LOPProjection) {
                    d = d.getChildren()[0]
                }
                if (d is LOPJoin && !d.optional) {
                    res.addAll(findAllJoinsInChildren(d))
                } else {
                    res.add(d)
                }
            } else if (c is POPNothing) {
                // there can not be any result, if_ one of the children does not have any output.
                throw EmptyResultException()
            } else if (c is OPEmptyRow) {
                // skip those unnecessary joins, without any observeable effekt
            } else {
                res.add(c)
            }
        }
        return res
    }

    /*suspend*/ private fun buildJoinOrder(nodes: List<IOPBase>, root: LOPJoin, joinOrder: Int): IOPBase {
        when {
            nodes.size > 2 -> {
                var result = LogicalOptimizerBuildCustomJoinOrderML(nodes, root, joinOrder)
                if (result != null) {
                    return result
                }
                SanityCheck.checkUnreachable()
            }
            nodes.size == 2 -> {
                val res = LOPJoin(root.query, nodes[0], nodes[1], false)
                res.onlyExistenceRequired = root.onlyExistenceRequired
                return res
            }
            else -> {
                SanityCheck.check(
                    { /*SOURCE_FILE_START*/"/src/luposdate3000/src/luposdate3000_optimizer_logical/src/commonMain/kotlin/lupos/optimizer/logical/LogicalOptimizerJoinOrderML.kt:75"/*SOURCE_FILE_END*/ },
                    { nodes.size == 1 }
                )
                return nodes[0]
            }
        }
/*Coverage Unreachable*/
    }

    override /*suspend*/ fun optimize(node: IOPBase, parent: IOPBase?, onChange: () -> Unit): IOPBase {
        var res: IOPBase = node
        if (node is LOPJoin && !node.optional && (parent !is LOPJoin || parent.optional)) {
            val originalProvided = node.getProvidedVariableNames()
            try {
                val allChilds2 = findAllJoinsInChildren(node)
                if (allChilds2.size > 2) {
                    var result: IOPBase? = null
                    if (result == null) {
                        result = buildJoinOrder(allChilds2, node, joinOrder)
                    }
                    if (result != res) {
                        onChange()
                        if (!originalProvided.containsAll(result.getProvidedVariableNames())) {
                            result = LOPProjection(query, originalProvided.map { AOPVariable(query, it) }.toMutableList(), result)
                        }
                        res = result
                    }
                }
            } catch (e: EmptyResultException) {
                e.printStackTrace()
                res = POPNothing(query, originalProvided)
            }
        }
        return res
    }
}
