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
package lupos.code_gen_test_00

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
import lupos.simulator_db.luposdate3000.Application_Luposdate3000
import lupos.simulator_db.luposdate3000.Package_Luposdate3000_TestingCompareGraphPackage
import lupos.simulator_db.luposdate3000.Package_Luposdate3000_TestingExecute
import lupos.simulator_db.luposdate3000.Package_Luposdate3000_TestingImportPackage
import simora.SimulationRun
import simora.addQuerySender
import kotlin.test.Test
import kotlin.test.fail

public class ADD3 {
    internal val inputData = arrayOf(
        File("src/jvmTest/resources/ADD3.input").readAsString(),
        File("src/jvmTest/resources/ADD3.input1").readAsString(),
        File("src/jvmTest/resources/ADD3.input2").readAsString(),
    )
    internal val inputDataFile = arrayOf(
        "src/jvmTest/resources/ADD3.input",
        "src/jvmTest/resources/ADD3.input1",
        "src/jvmTest/resources/ADD3.input2",
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
    internal val outputData = arrayOf(
        File("src/jvmTest/resources/ADD3.output0").readAsString(),
        File("src/jvmTest/resources/ADD3.output1").readAsString(),
        File("src/jvmTest/resources/ADD3.output2").readAsString(),
    )
    internal val outputDataFile = arrayOf(
        "src/jvmTest/resources/ADD3.output0",
        "src/jvmTest/resources/ADD3.output1",
        "src/jvmTest/resources/ADD3.output2",
    )
    internal val outputGraph = arrayOf(
        "",
        "http://example.org/g1",
        "http://example.org/g2",
    )
    internal val outputType = arrayOf(
        ".ttl",
        ".ttl",
        ".ttl",
    )
    internal val query = "PREFIX : <http://example.org/> \n" +
        "ADD :g1 TO :g2"

    @Test
    public fun `ADD 3 - None - Simple - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            normalHelper(instance)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test
    public fun `ADD 3 - None - Simple - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.None
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            normalHelper(instance)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test
    public fun `ADD 3 - Thread - Simple - true`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.Thread
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = true
            instance = LuposdateEndpoint.initializeB(instance)
            normalHelper(instance)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test
    public fun `ADD 3 - Thread - Simple - false`() {
        var instance = Luposdate3000Instance()
        try {
            instance.LUPOS_BUFFER_SIZE = 128
            instance.LUPOS_PARTITION_MODE = EPartitionModeExt.Thread
            instance.predefinedPartitionScheme = EPredefinedPartitionSchemesExt.Simple
            instance.useDictionaryInlineEncoding = false
            instance = LuposdateEndpoint.initializeB(instance)
            normalHelper(instance)
        } finally {
            LuposdateEndpoint.close(instance)
        }
    }

    @Test
    public fun `ADD 3 - in simulator - Simple - Centralized - true - None - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - Simple - Centralized - true - None - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - Simple - Centralized - true - None - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - Simple - Centralized - false - None - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - Simple - Centralized - false - None - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - Simple - Centralized - false - None - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test2.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "Simple",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "None",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Centralized - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Centralized - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Centralized - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Centralized - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Centralized - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Centralized - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Routing - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Routing - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Routing - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Routing - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Routing - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByIDTwiceAllCollations - Routing - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByIDTwiceAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Centralized - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Centralized - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Centralized - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Centralized - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Centralized - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Centralized - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Routing - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Routing - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Routing - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Routing - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Routing - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_1_AllCollations - Routing - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_1_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Centralized - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Centralized - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Centralized - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Centralized - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Centralized - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Centralized - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Routing - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Routing - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Routing - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Routing - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Routing - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_2_AllCollations - Routing - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_2_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Centralized - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Centralized - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Centralized - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Centralized - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Centralized - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Centralized - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Routing - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Routing - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Routing - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Routing - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Routing - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_O_AllCollations - Routing - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_O_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Centralized - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Centralized - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Centralized - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Centralized - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Centralized - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Centralized - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Routing - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Routing - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Routing - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Routing - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Routing - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByID_S_AllCollations - Routing - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByID_S_AllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Centralized - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Centralized - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Centralized - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Centralized - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Centralized - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Centralized - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Centralized",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Routing - true - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Routing - true - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Routing - true - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to true,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Routing - false - Process - RPL`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Routing - false - Process - RPL_Fast`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "RPL_Fast",
        )
    }

    @Test
    public fun `ADD 3 - in simulator - PartitionByKeyAllCollations - Routing - false - Process - AllShortestPath`() {
        simulatorHelper(
            "../luposdate3000_simulator_db/src/jvmTest/resources/autoIntegrationTest/test1.json",
            mutableMapOf(
                "predefinedPartitionScheme" to "PartitionByKeyAllCollations",
                "mergeLocalOperatorgraphs" to true,
                "queryDistributionMode" to "Routing",
                "useDictionaryInlineEncoding" to false,
                "REPLACE_STORE_WITH_VALUES" to false,
                "LUPOS_PARTITION_MODE" to "Process",
            ),
            "AllShortestPath",
        )
    }

    public fun simulatorHelper(fileName: String, database_cfg: MutableMap<String, Any>, routingProtocol: String) {
        val simRun = SimulationRun()
        simRun.parseConfig(
            fileName,
            false,
            {
                it.getOrEmptyObject("deviceType").getOrEmptyObject("LUPOSDATE_DEVICE").getOrEmptyObject("applications").getOrEmptyObject("lupos.simulator_db.luposdate3000.ApplicationFactory_Luposdate3000").putAll(database_cfg)
                it.getOrEmptyObject("routing").putAll(mapOf("protocol" to routingProtocol))
            }
        )

        

        simRun.startUp()
        val instance = (simRun.devices.map { it.getAllChildApplications() }.flatten().filter { it is Application_Luposdate3000 }.first() as Application_Luposdate3000).instance
        val pkg0 = Package_Luposdate3000_TestingImportPackage(inputDataFile[0], inputGraph[0], inputType[0])
        val pkg1 = Package_Luposdate3000_TestingImportPackage(inputDataFile[1], inputGraph[1], inputType[1])
        pkg0.setOnFinish(pkg1)
        val pkg2 = Package_Luposdate3000_TestingImportPackage(inputDataFile[2], inputGraph[2], inputType[2])
        pkg1.setOnFinish(pkg2)
        var verifyExecuted3 = 0
        val pkg3 = Package_Luposdate3000_TestingCompareGraphPackage(null, MemoryTable.parseFromAny(inputData[0], inputType[0], Query(instance))!!, { verifyExecuted3++ }, inputGraph[0], instance)
        pkg2.setOnFinish(pkg3)
        var verifyExecuted4 = 0
        val pkg4 = Package_Luposdate3000_TestingCompareGraphPackage(null, MemoryTable.parseFromAny(inputData[1], inputType[1], Query(instance))!!, { verifyExecuted4++ }, inputGraph[1], instance)
        pkg3.setOnFinish(pkg4)
        var verifyExecuted5 = 0
        val pkg5 = Package_Luposdate3000_TestingCompareGraphPackage(null, MemoryTable.parseFromAny(inputData[2], inputType[2], Query(instance))!!, { verifyExecuted5++ }, inputGraph[2], instance)
        pkg4.setOnFinish(pkg5)
        val pkg6 = Package_Luposdate3000_TestingExecute(query)
        pkg5.setOnFinish(pkg6)
        var verifyExecuted7 = 0
        val pkg7 = Package_Luposdate3000_TestingCompareGraphPackage(null, MemoryTable.parseFromAny(outputData[0], outputType[0], Query(instance))!!, { verifyExecuted7++ }, outputGraph[0], instance)
        pkg6.setOnFinish(pkg7)
        var verifyExecuted8 = 0
        val pkg8 = Package_Luposdate3000_TestingCompareGraphPackage(null, MemoryTable.parseFromAny(outputData[1], outputType[1], Query(instance))!!, { verifyExecuted8++ }, outputGraph[1], instance)
        pkg7.setOnFinish(pkg8)
        var verifyExecuted9 = 0
        val pkg9 = Package_Luposdate3000_TestingCompareGraphPackage(null, MemoryTable.parseFromAny(outputData[2], outputType[2], Query(instance))!!, { verifyExecuted9++ }, outputGraph[2], instance)
        pkg8.setOnFinish(pkg9)
        simRun.addQuerySender(10, 1, 1, pkg0)
        simRun.run()
        simRun.shutDown()
        if (verifyExecuted3 == 0) {
            fail("pck3 not verified")
        }
        if (verifyExecuted4 == 0) {
            fail("pck4 not verified")
        }
        if (verifyExecuted5 == 0) {
            fail("pck5 not verified")
        }
        if (verifyExecuted7 == 0) {
            fail("pck7 not verified")
        }
        if (verifyExecuted8 == 0) {
            fail("pck8 not verified")
        }
        if (verifyExecuted9 == 0) {
            fail("pck9 not verified")
        }
    }

    internal fun normalHelper(instance: Luposdate3000Instance) {
        val buf = MyPrintWriter(false)
        if (listOf(".n3", ".ttl", ".nt").contains(inputType[0])) {
            LuposdateEndpoint.importTripleFileC(instance, inputDataFile[0], inputType[0], inputGraph[0])
        } else {
            TODO()
        }
        if (listOf(".n3", ".ttl", ".nt").contains(inputType[1])) {
            LuposdateEndpoint.importTripleFileC(instance, inputDataFile[1], inputType[1], inputGraph[1])
        } else {
            TODO()
        }
        if (listOf(".n3", ".ttl", ".nt").contains(inputType[2])) {
            LuposdateEndpoint.importTripleFileC(instance, inputDataFile[2], inputType[2], inputGraph[2])
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
        LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator3, buf, EQueryResultToStreamExt.EMPTY_STREAM)
        val query4 = Query(instance)
        val graph4 = instance.tripleStoreManager!!.getGraph(outputGraph[0])
        val operator4 = graph4.getIterator(query4, arrayOf(AOPVariable(query4, "s"), AOPVariable(query4, "p"), AOPVariable(query4, "o")), EIndexPatternExt.SPO)
        val actual4 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator4, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
        val expected4 = MemoryTable.parseFromAny(outputData[0], outputType[0], Query(instance))!!
        val buf_err4 = MyPrintWriter()
        if (!expected4.equalsVerbose(actual4, true, true, buf_err4)) {
            fail(expected4.toString() + " .. " + actual4.toString() + " .. " + buf_err4.toString() + " .. " + operator4)
        }
        val query5 = Query(instance)
        val graph5 = instance.tripleStoreManager!!.getGraph(outputGraph[1])
        val operator5 = graph5.getIterator(query5, arrayOf(AOPVariable(query5, "s"), AOPVariable(query5, "p"), AOPVariable(query5, "o")), EIndexPatternExt.SPO)
        val actual5 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator5, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
        val expected5 = MemoryTable.parseFromAny(outputData[1], outputType[1], Query(instance))!!
        val buf_err5 = MyPrintWriter()
        if (!expected5.equalsVerbose(actual5, true, true, buf_err5)) {
            fail(expected5.toString() + " .. " + actual5.toString() + " .. " + buf_err5.toString() + " .. " + operator5)
        }
        val query6 = Query(instance)
        val graph6 = instance.tripleStoreManager!!.getGraph(outputGraph[2])
        val operator6 = graph6.getIterator(query6, arrayOf(AOPVariable(query6, "s"), AOPVariable(query6, "p"), AOPVariable(query6, "o")), EIndexPatternExt.SPO)
        val actual6 = (LuposdateEndpoint.evaluateOperatorgraphToResultA(instance, operator6, buf, EQueryResultToStreamExt.MEMORY_TABLE) as List<MemoryTable>).first()
        val expected6 = MemoryTable.parseFromAny(outputData[2], outputType[2], Query(instance))!!
        val buf_err6 = MyPrintWriter()
        if (!expected6.equalsVerbose(actual6, true, true, buf_err6)) {
            fail(expected6.toString() + " .. " + actual6.toString() + " .. " + buf_err6.toString() + " .. " + operator6)
        }
    }
}
