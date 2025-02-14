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
package lupos.endpoint

import lupos.operator.base.Query
import lupos.operator.physical.singleinput.POPVisualisation
import lupos.optimizer.ast.OperatorGraphVisitor
import lupos.optimizer.logical.LogicalOptimizer
import lupos.optimizer.physical.PhysicalOptimizer
import lupos.optimizer.physical.PhysicalOptimizerVisualisation
import lupos.parser.LexerCharIterator
import lupos.parser.LookAheadTokenIterator
import lupos.parser.sparql1_1.ASTNode
import lupos.parser.sparql1_1.SPARQLParser
import lupos.parser.sparql1_1.TokenIteratorSPARQLParser
import lupos.shared.IVisualisation
import lupos.shared.Luposdate3000Instance
import lupos.shared.OPVisualGraph
import lupos.shared.inline.MyPrintWriter
import lupos.shared.operator.IOPBase
import kotlin.js.JsName

public class EndpointExtendedVisualize(input: String, internal val instance: Luposdate3000Instance) : IVisualisation {
    private var resultLog: Array<OPVisualGraph>
    private var resultPhys: Array<OPVisualGraph>
    private var result: String
    private var animationData: MutableList<String> = mutableListOf()

    init {
        val query: String = input
        val q: Query = Query(instance)
        val lcit: LexerCharIterator = LexerCharIterator(query)
        val tit: TokenIteratorSPARQLParser = TokenIteratorSPARQLParser(lcit)
        val ltit: LookAheadTokenIterator = LookAheadTokenIterator(tit, 3)
        val parser: SPARQLParser = SPARQLParser(ltit)
        val astNode: ASTNode = parser.expr()
        val lopNode: IOPBase = astNode.visit(OperatorGraphVisitor(q)) // Log Operatorgraph
        val logSteps: MutableList<IOPBase> = mutableListOf()
        val optLog: IOPBase = LogicalOptimizer(q).optimizeCall(lopNode, {}, { logSteps.add(it.cloneOP()) })
        resultLog = logSteps.map {
            val g = OPVisualGraph()
            LuposdateEndpoint.evaluateOperatorgraphToVisual(instance, it, g)
            g
        }.toTypedArray()
        val popOptimizer: PhysicalOptimizer = PhysicalOptimizer(q)
        val physSteps: MutableList<IOPBase> = mutableListOf()
        val tmp: IOPBase =
            popOptimizer.optimizeCall(optLog, {}, { physSteps.add(it.cloneOP()) }) // Physical Operatorgraph
        val optPhys: IOPBase = PhysicalOptimizerVisualisation(q).optimizeCall(tmp)
        physSteps.add(optPhys)
        resultPhys = physSteps.map {
            val g = OPVisualGraph()
            LuposdateEndpoint.evaluateOperatorgraphToVisual(instance, it, g)
            g
        }.toTypedArray()
        val buf = MyPrintWriter(true)
        recursive(optPhys)
        LuposdateEndpoint.evaluateOperatorgraphToResultB(instance, optPhys, buf)
        result = buf.toString()
    }

    private fun recursive(node: IOPBase) {
        for (i in node.getChildren()) {
            recursive(i)
        }
        if (node is POPVisualisation) {
            node.visualTest = this
        }
    }

    @JsName("getDataSteps")
    public fun getDataSteps(): Array<String> {
        return animationData.toTypedArray()
    }

    @JsName("getOptimizedStepsPhysical")
    public fun getOptimizedStepsPhysical(): Array<OPVisualGraph> {
        return resultPhys
    }

    @JsName("getOptimizedStepsLogical")
    public fun getOptimizedStepsLogical(): Array<OPVisualGraph> {
        return resultLog
    }

    @JsName("getResult")
    public fun getResult(): String {
        return result
    }

    override fun sendData(string: String) {
        animationData.add(string)
    }
}
