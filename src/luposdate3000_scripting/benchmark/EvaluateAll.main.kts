#!/usr/bin/env kotlin
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
@file:Import("DatabaseHandleVirtuoso.kt")
@file:Import("Config.kt")
@file:Import("DatabaseHandleBlazegraph.kt")
@file:Import("DatabaseHandleJena.kt")
@file:Import("DatabaseHandle.kt")
@file:Import("DatabaseHandleLuposdate3000Thread.kt")
@file:Import("DatabaseHandleLuposdate3000.kt")
@file:Import("DatabaseHandleLuposdate3000NoPartition.kt")
@file:Import("DatabaseHandleLuposdateMemory.kt")
@file:Import("DatabaseHandleLuposdateRDF3X.kt")
@file:Import("../../luposdate3000_shared/src/commonMain/kotlin/lupos/shared/EOperatingSystem.kt")
@file:Import("../../luposdate3000_shared/src/commonMain/kotlin/lupos/shared/EOperatingSystemExt.kt")
@file:Import("../../luposdate3000_shared/src/commonMain/kotlin/lupos/shared/ETripleComponentType.kt")
@file:Import("../../luposdate3000_shared/src/commonMain/kotlin/lupos/shared/ETripleComponentTypeExt.kt")
@file:Import("../../luposdate3000_shared_inline/src/commonMain/kotlin/lupos/shared/inline/Platform.kt")
@file:Import("../../luposdate3000_shared_inline/src/jvmMain/kotlin/lupos/shared/inline/Platform.kt")
@file:Import("../../luposdate3000_shared/src/jvmMain/kotlin/lupos/shared/DateHelperRelative.kt")
@file:Import("../../luposdate3000_shared/src/commonMain/kotlin/lupos/shared/DateHelperRelative.kt")
@file:CompilerOptions("-Xmulti-platform")

import lupos.benchmark.*
import lupos.shared.DateHelperRelative
import java.io.File

/*
 * pretty print xml :: 
 * apt install libxml2-utils
 * cat FILE_IN | sed 's/<?xml version="1.0"?>//g' | sed 's/<sparql [^>]*>/<sparql>/g' | xmllint --format - > FILE_OUT
 * for f in $(find /data/benchmark-results -type f -name "*xml") ; do cat $f | sed 's/<?xml version="1.0"?>//g' | sed 's/<sparql [^>]*>/<sparql>/g' | xmllint --format -  > $f.pretty.xml; done
 */

// configure databases
val allDatabases = mutableListOf(
//    DatabaseHandleBlazegraph("/data/benchmark/"),//out of memory during load
//    DatabaseHandleLuposdateMemory(port = 8080),//out of memory during load
//    DatabaseHandleLuposdateRDF3X(workDir = "/data/benchmark/", port = 8080),
//    DatabaseHandleVirtuoso(workDir = "/data/benchmark/"),
//    DatabaseHandleJena(port = 8080),
    DatabaseHandleLuposdate3000NoPartition(workDir = "/data/benchmark/", port = 8080).setBufferManager("Inmemory"),
    DatabaseHandleLuposdate3000NoPartition(workDir = "/data/benchmark/", port = 8080).setBufferManager("Persistent_Cached"),
)
for (i in listOf(2)) {
    allDatabases.add(
        DatabaseHandleLuposdate3000Thread(workDir = "/data/benchmark/", port = 8080, threadCount = i).setBufferManager("Inmemory")
    )
    allDatabases.add(
        DatabaseHandleLuposdate3000Thread(workDir = "/data/benchmark/", port = 8080, threadCount = i).setBufferManager("Persistent_Cached")
    )
}

// configure dataset locations
val allDatasets = mapOf(
    "yago1" to "/mnt/luposdate-testdata/yago1/yago-1.0.0-turtle.ttl",
    "yago2" to "/mnt/luposdate-testdata/yago2/yago-2.n3",
    "yago2s" to "/mnt/luposdate-testdata/yago2s/yago-2.5.3-turtle-simple.ttl",
    "barton" to "/mnt/luposdate-testdata/barton/barton.nt",
)
// configure file containing all queries
val literaturFile = "/src/benjamin/uni_luebeck/_00_papers/gelesenePaper/literatur.n3"
// blacklist some queries, comments show why these are blacklisted
val blacklistedQueries = mapOf(
    "LuposdateRDF3X" to mapOf(
        "yago1" to setOf(
            "_:26", // timeout
        ),
    ),
    "Jena" to mapOf(
        "yago1" to setOf(
            "_:11", // timeout
            "_:26", // timeout
        ),
    ),
    "Luposdate3000NoPartitionInmemory" to mapOf(
        "yago1" to setOf(
            "_:26", // timeout
        ),
    ),
    "Luposdate3000NoPartitionPersistent_Cached" to mapOf(
        "yago1" to setOf(
            "_:26", // timeout
        ),
    ),
)

// obtaining tasks from file
val allTasks = mutableMapOf<String, MutableMap<String, String>>()
File(literaturFile).forEachLine { line ->
    val idx = line.indexOf(" <query_sparql_")
    if (idx > 0) {
        if (!line.contains("_original>")) {
            val queryName = line.substring(0, idx)
            val idx2 = line.indexOf(">", idx)
            val datasetname = line.substring(idx + " <query_sparql_".length, idx2)
            val query = line.substring(idx2 + 3, line.length - 3)
            var tmp = allTasks[datasetname]
            if (tmp == null) {
                tmp = mutableMapOf<String, String>()
                allTasks[datasetname] = tmp
            }
            tmp[queryName] = query
        }
    }
}
// printing all tasks
for ((k, v) in allTasks) {
    println("dataset :: $k")
    for ((k2, v2) in v) {
        println("  query :: $k2 :: $v2")
    }
}

// evaluating all tasks
val outputFolder = "/data/benchmark-results/"
File(outputFolder).mkdirs()
File("$outputFolder/log.txt").printWriter().use { logger ->
    for ((datasetName, allQueries) in allTasks) {
        println("use $datasetName")
        val datasetFile = allDatasets[datasetName]!!
        for (databaseIdx in 0 until allDatabases.size) {
            val database = allDatabases[databaseIdx]
            println("use ${database.getName()}")
            try {
                var abortSignal = false
                val startTime = DateHelperRelative.markNow()
                database.launch(
                    datasetFile,
                    {
                        abortSignal = true
                    },
                    {
                        try {
                            if (!abortSignal) {
                                val importTime = DateHelperRelative.elapsedSeconds(startTime)
                                logger.println("import,$datasetName,${database.getName()},_,$importTime")
                                logger.flush()
                                for ((queryname, query) in allQueries) {
                                    println("use $queryname")
                                    val flag = blacklistedQueries[database.getName()]?.get(datasetName)?.contains(queryname)
                                    if (flag == null || !flag) {
                                        val startTime2 = DateHelperRelative.markNow()
                                        val response = database.runQuery(query)
                                        val querytime = DateHelperRelative.elapsedSeconds(startTime2)
                                        logger.println("evaluate,$datasetName,${database.getName()},$queryname,$querytime")
                                        logger.flush()
                                        File("$outputFolder$datasetName/$queryname").mkdirs()
                                        File("$outputFolder$datasetName/$queryname/${database.getName()}.xml").printWriter().use { out ->
                                            out.println(response)
                                        }
                                    }
                                }
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                println("errored import ${database.getName()} $datasetName")
            }
        }
    }
}
