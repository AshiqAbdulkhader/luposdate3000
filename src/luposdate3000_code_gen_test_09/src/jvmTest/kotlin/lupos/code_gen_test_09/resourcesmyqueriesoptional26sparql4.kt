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
package lupos.code_gen_test_09
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
import kotlin.test.Test
import kotlin.test.fail

public class resourcesmyqueriesoptional26sparql4 {
    internal val inputData = arrayOf(
        File("src/jvmTest/resources/resourcesmyqueriesoptional26sparql4.input").readAsString(),
    )
    internal val inputGraph = arrayOf(
        "",
    )
    internal val inputType = arrayOf(
        ".ttl",
    )
    internal val targetData = File("src/jvmTest/resources/resourcesmyqueriesoptional26sparql4.output").readAsString()
    internal val targetType = ".srx"
    internal val query = "PREFIX : <http://example.org/> \n" +
        "SELECT * WHERE { \n" +
        " ?s :b1 ?o1 . \n" +
        " OPTIONAL { \n" +
        "  { \n" +
        "   ?s :b2 ?o2 . \n" +
        "  } UNION { \n" +
        "   ?s :b2 ?o2 . \n" +
        "  } \n" +
        " } \n" +
        "} \n" +
        ""

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByIDTwiceAllCollations - true`() {
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
            val query0 = Query(instance)
            val graph0 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator0 = graph0.getIterator(query0, arrayOf(AOPVariable(query0, "s"), AOPVariable(query0, "p"), AOPVariable(query0, "o")), EIndexPatternExt.SPO)
            val actual0 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator0, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected0 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err0 = MyPrintWriter()
            if (!expected0.equalsVerbose(actual0, true, true, buf_err0)) {
                fail(expected0.toString() + " .. " + actual0.toString() + " .. " + buf_err0.toString() + " .. " + operator0)
            }
            val operator1 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual1 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator1, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected1 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err1 = MyPrintWriter()
            if (!expected1.equalsVerbose(actual1, true, true, buf_err1)) {
                fail(expected1.toString() + " .. " + actual1.toString() + " .. " + buf_err1.toString() + " .. " + operator1)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByIDTwiceAllCollations - false`() {
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
            val query2 = Query(instance)
            val graph2 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator2 = graph2.getIterator(query2, arrayOf(AOPVariable(query2, "s"), AOPVariable(query2, "p"), AOPVariable(query2, "o")), EIndexPatternExt.SPO)
            val actual2 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator2, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected2 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err2 = MyPrintWriter()
            if (!expected2.equalsVerbose(actual2, true, true, buf_err2)) {
                fail(expected2.toString() + " .. " + actual2.toString() + " .. " + buf_err2.toString() + " .. " + operator2)
            }
            val operator3 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual3 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator3, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected3 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err3 = MyPrintWriter()
            if (!expected3.equalsVerbose(actual3, true, true, buf_err3)) {
                fail(expected3.toString() + " .. " + actual3.toString() + " .. " + buf_err3.toString() + " .. " + operator3)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_1_AllCollations - true`() {
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
            val query4 = Query(instance)
            val graph4 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator4 = graph4.getIterator(query4, arrayOf(AOPVariable(query4, "s"), AOPVariable(query4, "p"), AOPVariable(query4, "o")), EIndexPatternExt.SPO)
            val actual4 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator4, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected4 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err4 = MyPrintWriter()
            if (!expected4.equalsVerbose(actual4, true, true, buf_err4)) {
                fail(expected4.toString() + " .. " + actual4.toString() + " .. " + buf_err4.toString() + " .. " + operator4)
            }
            val operator5 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual5 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator5, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected5 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err5 = MyPrintWriter()
            if (!expected5.equalsVerbose(actual5, true, true, buf_err5)) {
                fail(expected5.toString() + " .. " + actual5.toString() + " .. " + buf_err5.toString() + " .. " + operator5)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_1_AllCollations - false`() {
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
            val query6 = Query(instance)
            val graph6 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator6 = graph6.getIterator(query6, arrayOf(AOPVariable(query6, "s"), AOPVariable(query6, "p"), AOPVariable(query6, "o")), EIndexPatternExt.SPO)
            val actual6 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator6, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected6 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err6 = MyPrintWriter()
            if (!expected6.equalsVerbose(actual6, true, true, buf_err6)) {
                fail(expected6.toString() + " .. " + actual6.toString() + " .. " + buf_err6.toString() + " .. " + operator6)
            }
            val operator7 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual7 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator7, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected7 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err7 = MyPrintWriter()
            if (!expected7.equalsVerbose(actual7, true, true, buf_err7)) {
                fail(expected7.toString() + " .. " + actual7.toString() + " .. " + buf_err7.toString() + " .. " + operator7)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_2_AllCollations - true`() {
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
            val query8 = Query(instance)
            val graph8 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator8 = graph8.getIterator(query8, arrayOf(AOPVariable(query8, "s"), AOPVariable(query8, "p"), AOPVariable(query8, "o")), EIndexPatternExt.SPO)
            val actual8 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator8, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected8 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err8 = MyPrintWriter()
            if (!expected8.equalsVerbose(actual8, true, true, buf_err8)) {
                fail(expected8.toString() + " .. " + actual8.toString() + " .. " + buf_err8.toString() + " .. " + operator8)
            }
            val operator9 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual9 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator9, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected9 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err9 = MyPrintWriter()
            if (!expected9.equalsVerbose(actual9, true, true, buf_err9)) {
                fail(expected9.toString() + " .. " + actual9.toString() + " .. " + buf_err9.toString() + " .. " + operator9)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_2_AllCollations - false`() {
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
            val query10 = Query(instance)
            val graph10 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator10 = graph10.getIterator(query10, arrayOf(AOPVariable(query10, "s"), AOPVariable(query10, "p"), AOPVariable(query10, "o")), EIndexPatternExt.SPO)
            val actual10 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator10, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected10 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err10 = MyPrintWriter()
            if (!expected10.equalsVerbose(actual10, true, true, buf_err10)) {
                fail(expected10.toString() + " .. " + actual10.toString() + " .. " + buf_err10.toString() + " .. " + operator10)
            }
            val operator11 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual11 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator11, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected11 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err11 = MyPrintWriter()
            if (!expected11.equalsVerbose(actual11, true, true, buf_err11)) {
                fail(expected11.toString() + " .. " + actual11.toString() + " .. " + buf_err11.toString() + " .. " + operator11)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_O_AllCollations - true`() {
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
            val query12 = Query(instance)
            val graph12 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator12 = graph12.getIterator(query12, arrayOf(AOPVariable(query12, "s"), AOPVariable(query12, "p"), AOPVariable(query12, "o")), EIndexPatternExt.SPO)
            val actual12 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator12, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected12 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err12 = MyPrintWriter()
            if (!expected12.equalsVerbose(actual12, true, true, buf_err12)) {
                fail(expected12.toString() + " .. " + actual12.toString() + " .. " + buf_err12.toString() + " .. " + operator12)
            }
            val operator13 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual13 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator13, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected13 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err13 = MyPrintWriter()
            if (!expected13.equalsVerbose(actual13, true, true, buf_err13)) {
                fail(expected13.toString() + " .. " + actual13.toString() + " .. " + buf_err13.toString() + " .. " + operator13)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_O_AllCollations - false`() {
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
            val query14 = Query(instance)
            val graph14 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator14 = graph14.getIterator(query14, arrayOf(AOPVariable(query14, "s"), AOPVariable(query14, "p"), AOPVariable(query14, "o")), EIndexPatternExt.SPO)
            val actual14 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator14, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected14 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err14 = MyPrintWriter()
            if (!expected14.equalsVerbose(actual14, true, true, buf_err14)) {
                fail(expected14.toString() + " .. " + actual14.toString() + " .. " + buf_err14.toString() + " .. " + operator14)
            }
            val operator15 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual15 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator15, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected15 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err15 = MyPrintWriter()
            if (!expected15.equalsVerbose(actual15, true, true, buf_err15)) {
                fail(expected15.toString() + " .. " + actual15.toString() + " .. " + buf_err15.toString() + " .. " + operator15)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_S_AllCollations - true`() {
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
            val query16 = Query(instance)
            val graph16 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator16 = graph16.getIterator(query16, arrayOf(AOPVariable(query16, "s"), AOPVariable(query16, "p"), AOPVariable(query16, "o")), EIndexPatternExt.SPO)
            val actual16 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator16, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected16 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err16 = MyPrintWriter()
            if (!expected16.equalsVerbose(actual16, true, true, buf_err16)) {
                fail(expected16.toString() + " .. " + actual16.toString() + " .. " + buf_err16.toString() + " .. " + operator16)
            }
            val operator17 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual17 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator17, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected17 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err17 = MyPrintWriter()
            if (!expected17.equalsVerbose(actual17, true, true, buf_err17)) {
                fail(expected17.toString() + " .. " + actual17.toString() + " .. " + buf_err17.toString() + " .. " + operator17)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByID_S_AllCollations - false`() {
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
            val query18 = Query(instance)
            val graph18 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator18 = graph18.getIterator(query18, arrayOf(AOPVariable(query18, "s"), AOPVariable(query18, "p"), AOPVariable(query18, "o")), EIndexPatternExt.SPO)
            val actual18 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator18, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected18 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err18 = MyPrintWriter()
            if (!expected18.equalsVerbose(actual18, true, true, buf_err18)) {
                fail(expected18.toString() + " .. " + actual18.toString() + " .. " + buf_err18.toString() + " .. " + operator18)
            }
            val operator19 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual19 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator19, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected19 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err19 = MyPrintWriter()
            if (!expected19.equalsVerbose(actual19, true, true, buf_err19)) {
                fail(expected19.toString() + " .. " + actual19.toString() + " .. " + buf_err19.toString() + " .. " + operator19)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByKeyAllCollations - true`() {
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
            val query20 = Query(instance)
            val graph20 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator20 = graph20.getIterator(query20, arrayOf(AOPVariable(query20, "s"), AOPVariable(query20, "p"), AOPVariable(query20, "o")), EIndexPatternExt.SPO)
            val actual20 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator20, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected20 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err20 = MyPrintWriter()
            if (!expected20.equalsVerbose(actual20, true, true, buf_err20)) {
                fail(expected20.toString() + " .. " + actual20.toString() + " .. " + buf_err20.toString() + " .. " + operator20)
            }
            val operator21 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual21 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator21, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected21 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err21 = MyPrintWriter()
            if (!expected21.equalsVerbose(actual21, true, true, buf_err21)) {
                fail(expected21.toString() + " .. " + actual21.toString() + " .. " + buf_err21.toString() + " .. " + operator21)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - PartitionByKeyAllCollations - false`() {
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
            val query22 = Query(instance)
            val graph22 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator22 = graph22.getIterator(query22, arrayOf(AOPVariable(query22, "s"), AOPVariable(query22, "p"), AOPVariable(query22, "o")), EIndexPatternExt.SPO)
            val actual22 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator22, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected22 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err22 = MyPrintWriter()
            if (!expected22.equalsVerbose(actual22, true, true, buf_err22)) {
                fail(expected22.toString() + " .. " + actual22.toString() + " .. " + buf_err22.toString() + " .. " + operator22)
            }
            val operator23 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual23 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator23, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected23 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err23 = MyPrintWriter()
            if (!expected23.equalsVerbose(actual23, true, true, buf_err23)) {
                fail(expected23.toString() + " .. " + actual23.toString() + " .. " + buf_err23.toString() + " .. " + operator23)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - Simple - true`() {
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
            val query24 = Query(instance)
            val graph24 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator24 = graph24.getIterator(query24, arrayOf(AOPVariable(query24, "s"), AOPVariable(query24, "p"), AOPVariable(query24, "o")), EIndexPatternExt.SPO)
            val actual24 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator24, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected24 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err24 = MyPrintWriter()
            if (!expected24.equalsVerbose(actual24, true, true, buf_err24)) {
                fail(expected24.toString() + " .. " + actual24.toString() + " .. " + buf_err24.toString() + " .. " + operator24)
            }
            val operator25 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual25 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator25, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected25 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err25 = MyPrintWriter()
            if (!expected25.equalsVerbose(actual25, true, true, buf_err25)) {
                fail(expected25.toString() + " .. " + actual25.toString() + " .. " + buf_err25.toString() + " .. " + operator25)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - None - Simple - false`() {
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
            val query26 = Query(instance)
            val graph26 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator26 = graph26.getIterator(query26, arrayOf(AOPVariable(query26, "s"), AOPVariable(query26, "p"), AOPVariable(query26, "o")), EIndexPatternExt.SPO)
            val actual26 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator26, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected26 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err26 = MyPrintWriter()
            if (!expected26.equalsVerbose(actual26, true, true, buf_err26)) {
                fail(expected26.toString() + " .. " + actual26.toString() + " .. " + buf_err26.toString() + " .. " + operator26)
            }
            val operator27 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual27 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator27, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected27 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err27 = MyPrintWriter()
            if (!expected27.equalsVerbose(actual27, true, true, buf_err27)) {
                fail(expected27.toString() + " .. " + actual27.toString() + " .. " + buf_err27.toString() + " .. " + operator27)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test(timeout = 2000)
    public fun `resourcesmyqueriesoptional26sparql4 - Thread - Simple - false`() {
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
            val query54 = Query(instance)
            val graph54 = instance.tripleStoreManager!!.getGraph(inputGraph[0])
            val operator54 = graph54.getIterator(query54, arrayOf(AOPVariable(query54, "s"), AOPVariable(query54, "p"), AOPVariable(query54, "o")), EIndexPatternExt.SPO)
            val actual54 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator54, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected54 = MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!
            val buf_err54 = MyPrintWriter()
            if (!expected54.equalsVerbose(actual54, true, true, buf_err54)) {
                fail(expected54.toString() + " .. " + actual54.toString() + " .. " + buf_err54.toString() + " .. " + operator54)
            }
            val operator55 = LuposdateEndpoint.evaluateSparqlToOperatorgraphA(instance, query)
            val actual55 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator55, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
            val expected55 = MemoryTable.parseFromAny(targetData, targetType, Query(instance))!!
            val buf_err55 = MyPrintWriter()
            if (!expected55.equalsVerbose(actual55, true, true, buf_err55)) {
                fail(expected55.toString() + " .. " + actual55.toString() + " .. " + buf_err55.toString() + " .. " + operator55)
            }
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }
}