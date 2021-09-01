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
package lupos.code_gen_test_03
import lupos.endpoint.LuposdateEndpoint
import lupos.operator.arithmetik.noinput.AOPVariable
import lupos.operator.base.Query
import lupos.result_format.EQueryResultToStreamExt
import lupos.shared.EIndexPatternExt
import lupos.shared.EPartitionModeExt
import lupos.shared.EPredefinedPartitionSchemesExt
import lupos.shared.Luposdate3000Instance
import lupos.shared.MemoryTable
import lupos.shared.inline.File
import lupos.shared.inline.MyPrintWriter
import lupos.simulator_core.Simulation
import lupos.simulator_db.luposdate3000.DatabaseHandle
import lupos.simulator_db.luposdate3000.MySimulatorTestingCompareGraphPackage
import lupos.simulator_db.luposdate3000.MySimulatorTestingImportPackage
import lupos.simulator_iot.SimulationRun
import kotlin.test.Test
import kotlin.test.fail

public class DROPALL {
    internal val inputData = arrayOf(
        File("src/jvmTest/resources/DROPALL.input").readAsString(),
        File("src/jvmTest/resources/DROPALL.input1").readAsString(),
        File("src/jvmTest/resources/DROPALL.input2").readAsString(),
    )
    internal val inputGraph = arrayOf(
        "",
        "http://example.org/g1",
        "http://example.org/g2",
    )
    internal val inputType = arrayOf(
        ".ttl",
        ".ttl",
        ".ttl",
    )
    internal val query = "PREFIX     : <http://example.org/>  \n" +
        "DROP ALL \n" +
        ""

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByIDTwiceAllCollations - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByIDTwiceAllCollations
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query0 = Query(instance)
            val graph0 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator0 = graph0.getIterator(query0, arrayOf(AOPVariable(query0, "s"), AOPVariable(query0, "p"), AOPVariable(query0, "o")), EIndexPatternExt.SPO)
            val actual0 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator0, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected0 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err0 = MyPrintWriter()
            if (!expected0.equalsVerbose(actual0, true, true, buf_err0)) {
                fail(expected0.toString() + " .. " + actual0.toString() + " .. " + buf_err0.toString() + " .. " + operator0)
            }
            val query1 = Query(instance)
            val graph1 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator1 = graph1.getIterator(query1, arrayOf(AOPVariable(query1, "s"), AOPVariable(query1, "p"), AOPVariable(query1, "o")), EIndexPatternExt.SPO)
            val actual1 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator1, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected1 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err1 = MyPrintWriter()
            if (!expected1.equalsVerbose(actual1, true, true, buf_err1)) {
                fail(expected1.toString() + " .. " + actual1.toString() + " .. " + buf_err1.toString() + " .. " + operator1)
            }
            val query2 = Query(instance)
            val graph2 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator2 = graph2.getIterator(query2, arrayOf(AOPVariable(query2, "s"), AOPVariable(query2, "p"), AOPVariable(query2, "o")), EIndexPatternExt.SPO)
            val actual2 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator2, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected2 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err2 = MyPrintWriter()
            if (!expected2.equalsVerbose(actual2, true, true, buf_err2)) {
                fail(expected2.toString() + " .. " + actual2.toString() + " .. " + buf_err2.toString() + " .. " + operator2)
            }
            val operator3 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByIDTwiceAllCollations - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByIDTwiceAllCollations
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query4 = Query(instance)
            val graph4 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator4 = graph4.getIterator(query4, arrayOf(AOPVariable(query4, "s"), AOPVariable(query4, "p"), AOPVariable(query4, "o")), EIndexPatternExt.SPO)
            val actual4 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator4, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected4 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err4 = MyPrintWriter()
            if (!expected4.equalsVerbose(actual4, true, true, buf_err4)) {
                fail(expected4.toString() + " .. " + actual4.toString() + " .. " + buf_err4.toString() + " .. " + operator4)
            }
            val query5 = Query(instance)
            val graph5 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator5 = graph5.getIterator(query5, arrayOf(AOPVariable(query5, "s"), AOPVariable(query5, "p"), AOPVariable(query5, "o")), EIndexPatternExt.SPO)
            val actual5 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator5, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected5 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err5 = MyPrintWriter()
            if (!expected5.equalsVerbose(actual5, true, true, buf_err5)) {
                fail(expected5.toString() + " .. " + actual5.toString() + " .. " + buf_err5.toString() + " .. " + operator5)
            }
            val query6 = Query(instance)
            val graph6 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator6 = graph6.getIterator(query6, arrayOf(AOPVariable(query6, "s"), AOPVariable(query6, "p"), AOPVariable(query6, "o")), EIndexPatternExt.SPO)
            val actual6 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator6, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected6 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err6 = MyPrintWriter()
            if (!expected6.equalsVerbose(actual6, true, true, buf_err6)) {
                fail(expected6.toString() + " .. " + actual6.toString() + " .. " + buf_err6.toString() + " .. " + operator6)
            }
            val operator7 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_1_AllCollations - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_1_AllCollations
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query8 = Query(instance)
            val graph8 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator8 = graph8.getIterator(query8, arrayOf(AOPVariable(query8, "s"), AOPVariable(query8, "p"), AOPVariable(query8, "o")), EIndexPatternExt.SPO)
            val actual8 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator8, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected8 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err8 = MyPrintWriter()
            if (!expected8.equalsVerbose(actual8, true, true, buf_err8)) {
                fail(expected8.toString() + " .. " + actual8.toString() + " .. " + buf_err8.toString() + " .. " + operator8)
            }
            val query9 = Query(instance)
            val graph9 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator9 = graph9.getIterator(query9, arrayOf(AOPVariable(query9, "s"), AOPVariable(query9, "p"), AOPVariable(query9, "o")), EIndexPatternExt.SPO)
            val actual9 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator9, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected9 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err9 = MyPrintWriter()
            if (!expected9.equalsVerbose(actual9, true, true, buf_err9)) {
                fail(expected9.toString() + " .. " + actual9.toString() + " .. " + buf_err9.toString() + " .. " + operator9)
            }
            val query10 = Query(instance)
            val graph10 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator10 = graph10.getIterator(query10, arrayOf(AOPVariable(query10, "s"), AOPVariable(query10, "p"), AOPVariable(query10, "o")), EIndexPatternExt.SPO)
            val actual10 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator10, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected10 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err10 = MyPrintWriter()
            if (!expected10.equalsVerbose(actual10, true, true, buf_err10)) {
                fail(expected10.toString() + " .. " + actual10.toString() + " .. " + buf_err10.toString() + " .. " + operator10)
            }
            val operator11 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_1_AllCollations - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_1_AllCollations
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query12 = Query(instance)
            val graph12 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator12 = graph12.getIterator(query12, arrayOf(AOPVariable(query12, "s"), AOPVariable(query12, "p"), AOPVariable(query12, "o")), EIndexPatternExt.SPO)
            val actual12 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator12, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected12 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err12 = MyPrintWriter()
            if (!expected12.equalsVerbose(actual12, true, true, buf_err12)) {
                fail(expected12.toString() + " .. " + actual12.toString() + " .. " + buf_err12.toString() + " .. " + operator12)
            }
            val query13 = Query(instance)
            val graph13 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator13 = graph13.getIterator(query13, arrayOf(AOPVariable(query13, "s"), AOPVariable(query13, "p"), AOPVariable(query13, "o")), EIndexPatternExt.SPO)
            val actual13 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator13, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected13 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err13 = MyPrintWriter()
            if (!expected13.equalsVerbose(actual13, true, true, buf_err13)) {
                fail(expected13.toString() + " .. " + actual13.toString() + " .. " + buf_err13.toString() + " .. " + operator13)
            }
            val query14 = Query(instance)
            val graph14 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator14 = graph14.getIterator(query14, arrayOf(AOPVariable(query14, "s"), AOPVariable(query14, "p"), AOPVariable(query14, "o")), EIndexPatternExt.SPO)
            val actual14 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator14, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected14 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err14 = MyPrintWriter()
            if (!expected14.equalsVerbose(actual14, true, true, buf_err14)) {
                fail(expected14.toString() + " .. " + actual14.toString() + " .. " + buf_err14.toString() + " .. " + operator14)
            }
            val operator15 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_2_AllCollations - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_2_AllCollations
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query16 = Query(instance)
            val graph16 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator16 = graph16.getIterator(query16, arrayOf(AOPVariable(query16, "s"), AOPVariable(query16, "p"), AOPVariable(query16, "o")), EIndexPatternExt.SPO)
            val actual16 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator16, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected16 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err16 = MyPrintWriter()
            if (!expected16.equalsVerbose(actual16, true, true, buf_err16)) {
                fail(expected16.toString() + " .. " + actual16.toString() + " .. " + buf_err16.toString() + " .. " + operator16)
            }
            val query17 = Query(instance)
            val graph17 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator17 = graph17.getIterator(query17, arrayOf(AOPVariable(query17, "s"), AOPVariable(query17, "p"), AOPVariable(query17, "o")), EIndexPatternExt.SPO)
            val actual17 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator17, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected17 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err17 = MyPrintWriter()
            if (!expected17.equalsVerbose(actual17, true, true, buf_err17)) {
                fail(expected17.toString() + " .. " + actual17.toString() + " .. " + buf_err17.toString() + " .. " + operator17)
            }
            val query18 = Query(instance)
            val graph18 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator18 = graph18.getIterator(query18, arrayOf(AOPVariable(query18, "s"), AOPVariable(query18, "p"), AOPVariable(query18, "o")), EIndexPatternExt.SPO)
            val actual18 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator18, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected18 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err18 = MyPrintWriter()
            if (!expected18.equalsVerbose(actual18, true, true, buf_err18)) {
                fail(expected18.toString() + " .. " + actual18.toString() + " .. " + buf_err18.toString() + " .. " + operator18)
            }
            val operator19 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_2_AllCollations - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_2_AllCollations
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query20 = Query(instance)
            val graph20 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator20 = graph20.getIterator(query20, arrayOf(AOPVariable(query20, "s"), AOPVariable(query20, "p"), AOPVariable(query20, "o")), EIndexPatternExt.SPO)
            val actual20 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator20, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected20 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err20 = MyPrintWriter()
            if (!expected20.equalsVerbose(actual20, true, true, buf_err20)) {
                fail(expected20.toString() + " .. " + actual20.toString() + " .. " + buf_err20.toString() + " .. " + operator20)
            }
            val query21 = Query(instance)
            val graph21 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator21 = graph21.getIterator(query21, arrayOf(AOPVariable(query21, "s"), AOPVariable(query21, "p"), AOPVariable(query21, "o")), EIndexPatternExt.SPO)
            val actual21 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator21, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected21 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err21 = MyPrintWriter()
            if (!expected21.equalsVerbose(actual21, true, true, buf_err21)) {
                fail(expected21.toString() + " .. " + actual21.toString() + " .. " + buf_err21.toString() + " .. " + operator21)
            }
            val query22 = Query(instance)
            val graph22 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator22 = graph22.getIterator(query22, arrayOf(AOPVariable(query22, "s"), AOPVariable(query22, "p"), AOPVariable(query22, "o")), EIndexPatternExt.SPO)
            val actual22 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator22, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected22 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err22 = MyPrintWriter()
            if (!expected22.equalsVerbose(actual22, true, true, buf_err22)) {
                fail(expected22.toString() + " .. " + actual22.toString() + " .. " + buf_err22.toString() + " .. " + operator22)
            }
            val operator23 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_O_AllCollations - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_O_AllCollations
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query24 = Query(instance)
            val graph24 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator24 = graph24.getIterator(query24, arrayOf(AOPVariable(query24, "s"), AOPVariable(query24, "p"), AOPVariable(query24, "o")), EIndexPatternExt.SPO)
            val actual24 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator24, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected24 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err24 = MyPrintWriter()
            if (!expected24.equalsVerbose(actual24, true, true, buf_err24)) {
                fail(expected24.toString() + " .. " + actual24.toString() + " .. " + buf_err24.toString() + " .. " + operator24)
            }
            val query25 = Query(instance)
            val graph25 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator25 = graph25.getIterator(query25, arrayOf(AOPVariable(query25, "s"), AOPVariable(query25, "p"), AOPVariable(query25, "o")), EIndexPatternExt.SPO)
            val actual25 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator25, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected25 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err25 = MyPrintWriter()
            if (!expected25.equalsVerbose(actual25, true, true, buf_err25)) {
                fail(expected25.toString() + " .. " + actual25.toString() + " .. " + buf_err25.toString() + " .. " + operator25)
            }
            val query26 = Query(instance)
            val graph26 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator26 = graph26.getIterator(query26, arrayOf(AOPVariable(query26, "s"), AOPVariable(query26, "p"), AOPVariable(query26, "o")), EIndexPatternExt.SPO)
            val actual26 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator26, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected26 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err26 = MyPrintWriter()
            if (!expected26.equalsVerbose(actual26, true, true, buf_err26)) {
                fail(expected26.toString() + " .. " + actual26.toString() + " .. " + buf_err26.toString() + " .. " + operator26)
            }
            val operator27 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_O_AllCollations - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_O_AllCollations
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query28 = Query(instance)
            val graph28 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator28 = graph28.getIterator(query28, arrayOf(AOPVariable(query28, "s"), AOPVariable(query28, "p"), AOPVariable(query28, "o")), EIndexPatternExt.SPO)
            val actual28 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator28, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected28 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err28 = MyPrintWriter()
            if (!expected28.equalsVerbose(actual28, true, true, buf_err28)) {
                fail(expected28.toString() + " .. " + actual28.toString() + " .. " + buf_err28.toString() + " .. " + operator28)
            }
            val query29 = Query(instance)
            val graph29 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator29 = graph29.getIterator(query29, arrayOf(AOPVariable(query29, "s"), AOPVariable(query29, "p"), AOPVariable(query29, "o")), EIndexPatternExt.SPO)
            val actual29 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator29, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected29 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err29 = MyPrintWriter()
            if (!expected29.equalsVerbose(actual29, true, true, buf_err29)) {
                fail(expected29.toString() + " .. " + actual29.toString() + " .. " + buf_err29.toString() + " .. " + operator29)
            }
            val query30 = Query(instance)
            val graph30 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator30 = graph30.getIterator(query30, arrayOf(AOPVariable(query30, "s"), AOPVariable(query30, "p"), AOPVariable(query30, "o")), EIndexPatternExt.SPO)
            val actual30 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator30, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected30 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err30 = MyPrintWriter()
            if (!expected30.equalsVerbose(actual30, true, true, buf_err30)) {
                fail(expected30.toString() + " .. " + actual30.toString() + " .. " + buf_err30.toString() + " .. " + operator30)
            }
            val operator31 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_S_AllCollations - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_S_AllCollations
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query32 = Query(instance)
            val graph32 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator32 = graph32.getIterator(query32, arrayOf(AOPVariable(query32, "s"), AOPVariable(query32, "p"), AOPVariable(query32, "o")), EIndexPatternExt.SPO)
            val actual32 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator32, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected32 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err32 = MyPrintWriter()
            if (!expected32.equalsVerbose(actual32, true, true, buf_err32)) {
                fail(expected32.toString() + " .. " + actual32.toString() + " .. " + buf_err32.toString() + " .. " + operator32)
            }
            val query33 = Query(instance)
            val graph33 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator33 = graph33.getIterator(query33, arrayOf(AOPVariable(query33, "s"), AOPVariable(query33, "p"), AOPVariable(query33, "o")), EIndexPatternExt.SPO)
            val actual33 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator33, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected33 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err33 = MyPrintWriter()
            if (!expected33.equalsVerbose(actual33, true, true, buf_err33)) {
                fail(expected33.toString() + " .. " + actual33.toString() + " .. " + buf_err33.toString() + " .. " + operator33)
            }
            val query34 = Query(instance)
            val graph34 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator34 = graph34.getIterator(query34, arrayOf(AOPVariable(query34, "s"), AOPVariable(query34, "p"), AOPVariable(query34, "o")), EIndexPatternExt.SPO)
            val actual34 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator34, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected34 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err34 = MyPrintWriter()
            if (!expected34.equalsVerbose(actual34, true, true, buf_err34)) {
                fail(expected34.toString() + " .. " + actual34.toString() + " .. " + buf_err34.toString() + " .. " + operator34)
            }
            val operator35 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByID_S_AllCollations - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByID_S_AllCollations
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query36 = Query(instance)
            val graph36 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator36 = graph36.getIterator(query36, arrayOf(AOPVariable(query36, "s"), AOPVariable(query36, "p"), AOPVariable(query36, "o")), EIndexPatternExt.SPO)
            val actual36 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator36, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected36 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err36 = MyPrintWriter()
            if (!expected36.equalsVerbose(actual36, true, true, buf_err36)) {
                fail(expected36.toString() + " .. " + actual36.toString() + " .. " + buf_err36.toString() + " .. " + operator36)
            }
            val query37 = Query(instance)
            val graph37 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator37 = graph37.getIterator(query37, arrayOf(AOPVariable(query37, "s"), AOPVariable(query37, "p"), AOPVariable(query37, "o")), EIndexPatternExt.SPO)
            val actual37 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator37, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected37 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err37 = MyPrintWriter()
            if (!expected37.equalsVerbose(actual37, true, true, buf_err37)) {
                fail(expected37.toString() + " .. " + actual37.toString() + " .. " + buf_err37.toString() + " .. " + operator37)
            }
            val query38 = Query(instance)
            val graph38 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator38 = graph38.getIterator(query38, arrayOf(AOPVariable(query38, "s"), AOPVariable(query38, "p"), AOPVariable(query38, "o")), EIndexPatternExt.SPO)
            val actual38 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator38, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected38 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err38 = MyPrintWriter()
            if (!expected38.equalsVerbose(actual38, true, true, buf_err38)) {
                fail(expected38.toString() + " .. " + actual38.toString() + " .. " + buf_err38.toString() + " .. " + operator38)
            }
            val operator39 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByKeyAllCollations - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByKeyAllCollations
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query40 = Query(instance)
            val graph40 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator40 = graph40.getIterator(query40, arrayOf(AOPVariable(query40, "s"), AOPVariable(query40, "p"), AOPVariable(query40, "o")), EIndexPatternExt.SPO)
            val actual40 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator40, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected40 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err40 = MyPrintWriter()
            if (!expected40.equalsVerbose(actual40, true, true, buf_err40)) {
                fail(expected40.toString() + " .. " + actual40.toString() + " .. " + buf_err40.toString() + " .. " + operator40)
            }
            val query41 = Query(instance)
            val graph41 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator41 = graph41.getIterator(query41, arrayOf(AOPVariable(query41, "s"), AOPVariable(query41, "p"), AOPVariable(query41, "o")), EIndexPatternExt.SPO)
            val actual41 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator41, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected41 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err41 = MyPrintWriter()
            if (!expected41.equalsVerbose(actual41, true, true, buf_err41)) {
                fail(expected41.toString() + " .. " + actual41.toString() + " .. " + buf_err41.toString() + " .. " + operator41)
            }
            val query42 = Query(instance)
            val graph42 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator42 = graph42.getIterator(query42, arrayOf(AOPVariable(query42, "s"), AOPVariable(query42, "p"), AOPVariable(query42, "o")), EIndexPatternExt.SPO)
            val actual42 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator42, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected42 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err42 = MyPrintWriter()
            if (!expected42.equalsVerbose(actual42, true, true, buf_err42)) {
                fail(expected42.toString() + " .. " + actual42.toString() + " .. " + buf_err42.toString() + " .. " + operator42)
            }
            val operator43 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - PartitionByKeyAllCollations - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.PartitionByKeyAllCollations
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query44 = Query(instance)
            val graph44 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator44 = graph44.getIterator(query44, arrayOf(AOPVariable(query44, "s"), AOPVariable(query44, "p"), AOPVariable(query44, "o")), EIndexPatternExt.SPO)
            val actual44 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator44, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected44 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err44 = MyPrintWriter()
            if (!expected44.equalsVerbose(actual44, true, true, buf_err44)) {
                fail(expected44.toString() + " .. " + actual44.toString() + " .. " + buf_err44.toString() + " .. " + operator44)
            }
            val query45 = Query(instance)
            val graph45 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator45 = graph45.getIterator(query45, arrayOf(AOPVariable(query45, "s"), AOPVariable(query45, "p"), AOPVariable(query45, "o")), EIndexPatternExt.SPO)
            val actual45 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator45, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected45 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err45 = MyPrintWriter()
            if (!expected45.equalsVerbose(actual45, true, true, buf_err45)) {
                fail(expected45.toString() + " .. " + actual45.toString() + " .. " + buf_err45.toString() + " .. " + operator45)
            }
            val query46 = Query(instance)
            val graph46 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator46 = graph46.getIterator(query46, arrayOf(AOPVariable(query46, "s"), AOPVariable(query46, "p"), AOPVariable(query46, "o")), EIndexPatternExt.SPO)
            val actual46 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator46, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected46 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err46 = MyPrintWriter()
            if (!expected46.equalsVerbose(actual46, true, true, buf_err46)) {
                fail(expected46.toString() + " .. " + actual46.toString() + " .. " + buf_err46.toString() + " .. " + operator46)
            }
            val operator47 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - Simple - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query48 = Query(instance)
            val graph48 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator48 = graph48.getIterator(query48, arrayOf(AOPVariable(query48, "s"), AOPVariable(query48, "p"), AOPVariable(query48, "o")), EIndexPatternExt.SPO)
            val actual48 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator48, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected48 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err48 = MyPrintWriter()
            if (!expected48.equalsVerbose(actual48, true, true, buf_err48)) {
                fail(expected48.toString() + " .. " + actual48.toString() + " .. " + buf_err48.toString() + " .. " + operator48)
            }
            val query49 = Query(instance)
            val graph49 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator49 = graph49.getIterator(query49, arrayOf(AOPVariable(query49, "s"), AOPVariable(query49, "p"), AOPVariable(query49, "o")), EIndexPatternExt.SPO)
            val actual49 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator49, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected49 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err49 = MyPrintWriter()
            if (!expected49.equalsVerbose(actual49, true, true, buf_err49)) {
                fail(expected49.toString() + " .. " + actual49.toString() + " .. " + buf_err49.toString() + " .. " + operator49)
            }
            val query50 = Query(instance)
            val graph50 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator50 = graph50.getIterator(query50, arrayOf(AOPVariable(query50, "s"), AOPVariable(query50, "p"), AOPVariable(query50, "o")), EIndexPatternExt.SPO)
            val actual50 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator50, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected50 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err50 = MyPrintWriter()
            if (!expected50.equalsVerbose(actual50, true, true, buf_err50)) {
                fail(expected50.toString() + " .. " + actual50.toString() + " .. " + buf_err50.toString() + " .. " + operator50)
            }
            val operator51 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - None - Simple - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query52 = Query(instance)
            val graph52 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator52 = graph52.getIterator(query52, arrayOf(AOPVariable(query52, "s"), AOPVariable(query52, "p"), AOPVariable(query52, "o")), EIndexPatternExt.SPO)
            val actual52 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator52, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected52 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err52 = MyPrintWriter()
            if (!expected52.equalsVerbose(actual52, true, true, buf_err52)) {
                fail(expected52.toString() + " .. " + actual52.toString() + " .. " + buf_err52.toString() + " .. " + operator52)
            }
            val query53 = Query(instance)
            val graph53 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator53 = graph53.getIterator(query53, arrayOf(AOPVariable(query53, "s"), AOPVariable(query53, "p"), AOPVariable(query53, "o")), EIndexPatternExt.SPO)
            val actual53 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator53, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected53 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err53 = MyPrintWriter()
            if (!expected53.equalsVerbose(actual53, true, true, buf_err53)) {
                fail(expected53.toString() + " .. " + actual53.toString() + " .. " + buf_err53.toString() + " .. " + operator53)
            }
            val query54 = Query(instance)
            val graph54 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator54 = graph54.getIterator(query54, arrayOf(AOPVariable(query54, "s"), AOPVariable(query54, "p"), AOPVariable(query54, "o")), EIndexPatternExt.SPO)
            val actual54 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator54, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected54 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err54 = MyPrintWriter()
            if (!expected54.equalsVerbose(actual54, true, true, buf_err54)) {
                fail(expected54.toString() + " .. " + actual54.toString() + " .. " + buf_err54.toString() + " .. " + operator54)
            }
            val operator55 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - Thread - Simple - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.Thread
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query104 = Query(instance)
            val graph104 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator104 = graph104.getIterator(query104, arrayOf(AOPVariable(query104, "s"), AOPVariable(query104, "p"), AOPVariable(query104, "o")), EIndexPatternExt.SPO)
            val actual104 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator104, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected104 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err104 = MyPrintWriter()
            if (!expected104.equalsVerbose(actual104, true, true, buf_err104)) {
                fail(expected104.toString() + " .. " + actual104.toString() + " .. " + buf_err104.toString() + " .. " + operator104)
            }
            val query105 = Query(instance)
            val graph105 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator105 = graph105.getIterator(query105, arrayOf(AOPVariable(query105, "s"), AOPVariable(query105, "p"), AOPVariable(query105, "o")), EIndexPatternExt.SPO)
            val actual105 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator105, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected105 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err105 = MyPrintWriter()
            if (!expected105.equalsVerbose(actual105, true, true, buf_err105)) {
                fail(expected105.toString() + " .. " + actual105.toString() + " .. " + buf_err105.toString() + " .. " + operator105)
            }
            val query106 = Query(instance)
            val graph106 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator106 = graph106.getIterator(query106, arrayOf(AOPVariable(query106, "s"), AOPVariable(query106, "p"), AOPVariable(query106, "o")), EIndexPatternExt.SPO)
            val actual106 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator106, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected106 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err106 = MyPrintWriter()
            if (!expected106.equalsVerbose(actual106, true, true, buf_err106)) {
                fail(expected106.toString() + " .. " + actual106.toString() + " .. " + buf_err106.toString() + " .. " + operator106)
            }
            val operator107 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - Thread - Simple - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.Thread
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            val buf = MyPrintWriter(false)
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[0], inputGraph[0])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[1], inputGraph[1])
            } else {
                TODO()
            }
            if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
                LuposdateEndpoint.importTurtleString(instance, inputData[2], inputGraph[2])
            } else {
                TODO()
            }
            val query108 = Query(instance)
            val graph108 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator108 = graph108.getIterator(query108, arrayOf(AOPVariable(query108, "s"), AOPVariable(query108, "p"), AOPVariable(query108, "o")), EIndexPatternExt.SPO)
            val actual108 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator108, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected108 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err108 = MyPrintWriter()
            if (!expected108.equalsVerbose(actual108, true, true, buf_err108)) {
                fail(expected108.toString() + " .. " + actual108.toString() + " .. " + buf_err108.toString() + " .. " + operator108)
            }
            val query109 = Query(instance)
            val graph109 = instance.tripleStoreManager!!.getGraph(inputGraph[1])
            val operator109 = graph109.getIterator(query109, arrayOf(AOPVariable(query109, "s"), AOPVariable(query109, "p"), AOPVariable(query109, "o")), EIndexPatternExt.SPO)
            val actual109 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator109, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected109 = MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!
            val buf_err109 = MyPrintWriter()
            if (!expected109.equalsVerbose(actual109, true, true, buf_err109)) {
                fail(expected109.toString() + " .. " + actual109.toString() + " .. " + buf_err109.toString() + " .. " + operator109)
            }
            val query110 = Query(instance)
            val graph110 = instance.tripleStoreManager!!.getGraph(inputGraph[2])
            val operator110 = graph110.getIterator(query110, arrayOf(AOPVariable(query110, "s"), AOPVariable(query110, "p"), AOPVariable(query110, "o")), EIndexPatternExt.SPO)
            val actual110 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator110, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected110 = MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!
            val buf_err110 = MyPrintWriter()
            if (!expected110.equalsVerbose(actual110, true, true, buf_err110)) {
                fail(expected110.toString() + " .. " + actual110.toString() + " .. " + buf_err110.toString() + " .. " + operator110)
            }
            val operator111 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByIDTwiceAllCollations - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }
    public fun simulatorHelper(fileName: String, cfg: MutableMap<String, Any>) {
        val simRun = SimulationRun()
        val config = simRun.parseConfig(fileName, false)
        config.jsonObjects.database.putAll(cfg)
        simRun.sim = Simulation(config.getEntities())
        simRun.sim.maxClock = if (simRun.simMaxClock == simRun.notInitializedClock) simRun.sim.maxClock else simRun.simMaxClock
        simRun.sim.steadyClock = if (simRun.simSteadyClock == simRun.notInitializedClock) simRun.sim.steadyClock else simRun.simSteadyClock
        simRun.sim.startUp()
        val instance = (config.devices.filter { it.userApplication != null }.map { it.userApplication!!.getAllChildApplications() }.flatten().filter { it is DatabaseHandle }.first()as DatabaseHandle).instance
        val pkg0 = MySimulatorTestingImportPackage(inputData[0], inputGraph[0], inputType[0])
        val pkg1 = MySimulatorTestingImportPackage(inputData[1], inputGraph[1], inputType[1])
        pkg0.onFinish = pkg1
        val pkg2 = MySimulatorTestingImportPackage(inputData[2], inputGraph[2], inputType[2])
        pkg1.onFinish = pkg2
        var verifyExecuted3 = 0
        val pkg3 = MySimulatorTestingCompareGraphPackage("SELECT ?s ?p ?o WHERE { ?s ?p ?o . }", MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!, { verifyExecuted3++ })
        pkg2.onFinish = pkg3
        var verifyExecuted4 = 0
        val pkg4 = MySimulatorTestingCompareGraphPackage("SELECT ?s ?p ?o WHERE { GRAPH <${inputGraph[1]}> { ?s ?p ?o . }}", MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!, { verifyExecuted4++ })
        pkg3.onFinish = pkg4
        var verifyExecuted5 = 0
        val pkg5 = MySimulatorTestingCompareGraphPackage("SELECT ?s ?p ?o WHERE { GRAPH <${inputGraph[2]}> { ?s ?p ?o . }}", MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!, { verifyExecuted5++ })
        pkg4.onFinish = pkg5
        config.querySenders[0].queryPck = pkg0
        simRun.sim.run()
        simRun.sim.shutDown()
        if (verifyExecuted3 == 0) {
            fail("pck3 not verified")
        }
        if (verifyExecuted4 == 0) {
            fail("pck4 not verified")
        }
        if (verifyExecuted5 == 0) {
            fail("pck5 not verified")
        }
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByIDTwiceAllCollations - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_1_AllCollations - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_1_AllCollations - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_2_AllCollations - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_2_AllCollations - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_O_AllCollations - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_O_AllCollations - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_S_AllCollations - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_S_AllCollations - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByKeyAllCollations - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByKeyAllCollations - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - Simple - Centralized - true - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - Simple - Centralized - false - None`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByIDTwiceAllCollations - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByIDTwiceAllCollations - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_1_AllCollations - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_1_AllCollations - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_2_AllCollations - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_2_AllCollations - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_O_AllCollations - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_O_AllCollations - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_S_AllCollations - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByID_S_AllCollations - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByKeyAllCollations - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - PartitionByKeyAllCollations - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - Simple - Centralized - true - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }

    @Test(timeout = 2000)
    public fun `DROP ALL - in simulator - Simple - Centralized - false - Thread`() {
        simulatorHelper(
            "../luposdate3000_simulator_iot/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Thread",
            )
        )
    }
}