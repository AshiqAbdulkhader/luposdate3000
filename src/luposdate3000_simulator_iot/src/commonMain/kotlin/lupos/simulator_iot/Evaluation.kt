package lupos.simulator_iot

import lupos.simulator_iot.config.JsonObjects
import lupos.simulator_iot.measure.MeasurementPrinter
import lupos.simulator_iot.utils.FilePaths

public class Evaluation {
    public constructor() {}
    public fun simulate(configFileName: String) {
        val simRun = SimulationRun()
        val json = simRun.parseConfigFile(configFileName)
        val config = simRun.parseJsonObjects(json)
        simRun.startSimulation(config)
    }

    public fun measureStarPerformance(withDummyDatabase: Boolean) {
        var arr: IntArray = if (!withDummyDatabase) {
            buildNodeSizesArray(130, 10) // OutOfMemoryError >1330 DBs with 2048 heap space
        } else {
            buildNodeSizesArray(100, 1000)
        }

        for (numberOfChilds in arr)
            runSimulationStarPerformance(numberOfChilds, withDummyDatabase)
    }

    private fun buildNodeSizesArray(arrSize: Int, delta: Int): IntArray {
        val arr = IntArray(arrSize) { 0 }
        for (i in arr.withIndex())
            arr[i.index] = i.index * delta
        return arr
    }

    private fun buildNodeSizesArray(arrSize: Int): IntArray {
        val arr = IntArray(arrSize) { 0 }
        for (i in arr.withIndex())
            arr[i.index] = arr[i.index] * 2
        return arr
    }

    private fun runSimulationStarPerformance(numberOfChilds: Int, withDummyDatabase: Boolean) {
        // TODO
//        Logger.reset()
//        val configFileName = "${FilePaths.jvmResource}/starPerformance.json"
//        val jsonObjects = Configuration.readJsonFile(configFileName)
//        jsonObjects.randomStarNetwork[0].number = numberOfChilds
//        jsonObjects.dummyDatabase = withDummyDatabase
//        Configuration.parse(jsonObjects)
//        val entities = Configuration.getEntities()
//        val sim = Simulation(entities = entities, callback = Logger)
//        sim.startSimulation()
//        collection.add(Logger, entities.size)
    }

    public fun evalStarPerformanceWithoutDatabase() {
        val configFileName = "${FilePaths.jvmResource}/starPerformance.json"
        val nodeSizes = buildNodeSizesArray(10, 500)
        val printer = MeasurementPrinter()
        for ((index, numberOfNodes) in nodeSizes.withIndex()) {
            val prep = object : ISimRunPreparation {
                override fun prepareJsonObjects(jsonObjects: JsonObjects) {
                    jsonObjects.dummyDatabase = true
                    jsonObjects.randomStarNetwork[0].number = numberOfNodes
                }
            }
            MultipleSimulationRuns(configFileName, 20, prep, printer).startSimulationRuns()
            println("Run ${index + 1} finished. ${nodeSizes.size - index - 1 } runs left..")
        }
    }
}