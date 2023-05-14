package phonebook

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.HashMap
import java.util.Hashtable
import java.util.LinkedList
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.system.exitProcess

fun main() {
    val separator = File.separator
    val pathNamesForSearch = "D:${separator}Kotlin${separator}find.txt"
    val pathPhonebook = "D:${separator}Kotlin${separator}directory.txt"
    val fileNamesForSearch = File(pathNamesForSearch)
    val filePhonebook = File(pathPhonebook)
    val sizeOfSearch: Int = if (fileNamesForSearch.exists()) fileNamesForSearch.readLines().size else 0

    val finder = FinderAndSorter(filePhonebook, fileNamesForSearch)

    val (foundLinear, linearDuration) = duration {
        println("Start searching (linear search)...")
        finder.linearSearch()
    }
    println(
        "Found $foundLinear / $sizeOfSearch entries. Time taken: ${linearDuration / (1000 * 60) } min. " +
        "${linearDuration / 1000 % 60 } sec. ${ linearDuration % 1000 } ms."
    )
    println()

    println("Start searching (bubble sort + jump search)...")
    val bubbleSortStartTime = System.currentTimeMillis()
    val bubbleSorted: Boolean = finder.bubbleSort(bubbleSortStartTime, linearDuration)
    val bubbleSortDuration = System.currentTimeMillis() - bubbleSortStartTime

    if (bubbleSorted) {
        val (foundJump, jumpDuration) = duration { finder.jumpSearch(fileNamesForSearch) }
        println(
            "Found $foundJump / $sizeOfSearch entries. Time taken: ${ (bubbleSortDuration + jumpDuration) / (1000 * 60) } min. " +
            "${ (bubbleSortDuration + jumpDuration) / 1000 % 60 } sec. ${ (bubbleSortDuration + jumpDuration) % 1000 } ms."
        )
        println("Sorting time: ${ bubbleSortDuration / (1000 * 60) } min. ${ bubbleSortDuration / 1000 % 60 } sec. ${ bubbleSortDuration % 1000 } ms.")
        println("Searching time: ${ jumpDuration / (1000 * 60) } min. ${ jumpDuration / 1000 % 60 } sec. ${ jumpDuration % 1000} ms.")
    } else {
        val (newFoundLinear, newLinearDuration) = duration {
            println("Start searching (linear search)...")
            finder.linearSearch()
        }
        println(
            "Found $newFoundLinear / $sizeOfSearch entries. Time taken: ${ (bubbleSortDuration + newLinearDuration) / (1000 * 60) } min. " +
                    "${ (bubbleSortDuration + newLinearDuration) / 1000 % 60 } sec. ${ (bubbleSortDuration + newLinearDuration) % 1000 } ms."
        )
        println("Sorting time: ${ bubbleSortDuration / (1000 * 60) } min. ${ bubbleSortDuration / 1000 % 60 } sec. ${ bubbleSortDuration % 1000 } ms. - STOPPED, moved to line")
        println("Searching time: ${ newLinearDuration / (1000 * 60) } min. ${ newLinearDuration / 1000 % 60 } sec. ${ newLinearDuration % 1000 } ms.")
    }
    println()
    val phonebook = mutableListOf<String>()
        .apply { addAll(filePhonebook.readLines()) }
    val searchingNames = mutableListOf<String>()
        .apply { addAll(fileNamesForSearch.readLines()) }
    println("Start searching (quick sort + binary search)...")
    val quickSortStartTime = System.currentTimeMillis()
    finder.quickSort(phonebook, 0, phonebook.lastIndex)
    val quickSortDuration = System.currentTimeMillis() - quickSortStartTime
    val (foundBinary, binaryDuration) = duration { finder.binarySearch(phonebook, searchingNames) }
    println(
        "Found $foundBinary / $sizeOfSearch entries. Time taken: ${ (quickSortDuration + binaryDuration) / (1000 * 60) } min. " +
                "${ (quickSortDuration + binaryDuration) / 1000 % 60 } sec. ${ (quickSortDuration + binaryDuration) % 1000 } ms."
    )
    println("Sorting time: ${ quickSortDuration / (1000 * 60) } min. ${ quickSortDuration / 1000 % 60 } sec. ${ quickSortDuration % 1000 } ms.")
    println("Searching time: ${ binaryDuration / (1000 * 60) } min. ${ binaryDuration / 1000 % 60 } sec. ${ binaryDuration % 1000} ms.")
    println()
    println("Start searching (hash table)...")
    val hashTableStartTime = System.currentTimeMillis()
    val hashTable = hashTableMaker(filePhonebook)
    val hashTableDuration = System.currentTimeMillis() - hashTableStartTime
    val (foundHashTable, hashDuration) = duration { hashTableFinder(hashTable, fileNamesForSearch) }
    println(
        "Found $foundHashTable / $sizeOfSearch entries. Time taken: ${ (hashTableDuration + hashDuration) / (1000 * 60) } min. " +
                "${ (hashTableDuration + hashDuration) / 1000 % 60 } sec. ${ (hashTableDuration + hashDuration) % 1000 } ms."
    )
    println("Creating time: ${ hashTableDuration / (1000 * 60) } min. ${ hashTableDuration / 1000 % 60 } sec. ${ hashTableDuration % 1000 } ms.")
    println("Searching time: ${ hashDuration / (1000 * 60) } min. ${ hashDuration / 1000 % 60 } sec. ${ hashDuration % 1000} ms.")
}

inline fun duration(block: () -> Int): Pair<Int, Long> {
    val startTime = System.currentTimeMillis()
    val found = block()
    return found to System.currentTimeMillis() - startTime
}

fun hashTableMaker(filePhonebook: File): HashMap<Int, String> {
    val phonebook = filePhonebook.readLines()
    val hashTable = HashMap<Int, String>()
    if (phonebook.isNotEmpty()) {
        for (data in phonebook) {
            val key = data.substringAfter(" ").hashCode()
            hashTable[key] = data.substringBefore(" ")
        }
    }
    return hashTable
}

fun hashTableFinder(hashTable: HashMap<Int, String>, fileNamesForSearch: File): Int {
    var found = 0
    val namesForSearch = fileNamesForSearch.readLines()

    for (name in namesForSearch) {
        val hash = name.hashCode()
        if (hashTable.contains(hash)) found++
    }

    return found
}

class FinderAndSorter(private val filePhonebook: File, private val fileNamesForSearch: File) {
    private val phonebook = mutableListOf<String>()

    fun bubbleSort(startTime: Long, linearDuration: Long): Boolean {
        if (filePhonebook.exists()) {
            phonebook.addAll(filePhonebook.readLines())
        } else {
            println("No phonebook to sort!")
            exitProcess(0)
        }
        var swapCounter = -1
        while (swapCounter != 0) {
            swapCounter = 0
            for (i in 0 until phonebook.lastIndex) {
                if (phonebook[i].substringAfter(" ") > phonebook[i + 1].substringAfter(" ")) {
                    val temp = phonebook[i]
                    phonebook[i] = phonebook[i + 1]
                    phonebook[i + 1] = temp
                    swapCounter++
                }
                if (System.currentTimeMillis() - startTime > 10 * linearDuration) {
                    return false
                }
            }
        }
        val separator = File.separator
        val saveFile = File("D:${separator}Kotlin${separator}sortedPhonebook.txt")
        BufferedWriter(FileWriter(saveFile)).use {
            for (i in phonebook.indices) {
                it.write(phonebook[i])
                if (i != phonebook.lastIndex) {
                    it.newLine()
                }
            }
        }
        return true
    }

    fun quickSort(phonebook: MutableList<String>, left: Int, right: Int) {
        if (left >= right) return
        val pivot = partitionByName(phonebook, left, right)
        quickSort(phonebook, left, pivot - 1)
        quickSort(phonebook, pivot + 1, right)
    }

    private fun partitionByName(phonebook: MutableList<String>, left: Int, right: Int): Int {
        val pivot = phonebook[right]
        var i = left
        for (j in left until right) {
            if (phonebook[j].substringAfter(" ") < pivot.substringAfter(" ")) {
                val temp = phonebook[i]
                phonebook[i] = phonebook[j]
                phonebook[j] = temp
                i++
            }
        }
        val temp = phonebook[i]
        phonebook[i] = phonebook[right]
        phonebook[right] = temp
        return i
    }

    fun binarySearch(phonebook: MutableList<String>, searchingNames: MutableList<String>): Int {
        var found = 0

        fun binarySubSearch(name: String): Boolean {
            var left = 0
            var right = phonebook.lastIndex
            while (left <= right) {
                val middle = (left + right) / 2
                if (phonebook[middle].substringAfter(" ") == name) return true
                else if (phonebook[middle].substringAfter(" ") > name) right = middle - 1
                else left = middle + 1
            }
            return false
        }

        for (data in searchingNames) {
            if (binarySubSearch(data)) found++ else continue
        }

        return found
    }

    fun jumpSearch(searchingNamesFile: File): Int {
        val separator = File.separator
        val loadFile = File("D:${separator}Kotlin${separator}sortedPhonebook.txt")
        val sortedPhonebook = loadFile.readLines()
        val searchingNames = searchingNamesFile.readLines()

        var count = 0
        val step = sqrt(sortedPhonebook.size - 1.toDouble()).toInt()
        var start = 0
        var end = 0
        val last = sortedPhonebook.size - 1
        var index: Int

        for (data in searchingNames) {
            index = -1

            while (end != last && sortedPhonebook[end].substringAfter(" ") < data) {
                start = end
                end = min(end + step, last)
            }

            for (i in start..end.coerceAtMost(sortedPhonebook.size - 1)) {
                if (sortedPhonebook[i].substringAfter(" ") == data) {
                    index = i
                    break
                }
            }

            if (index != -1) {
                count++
            }
            start = 0
            end = 0
        }
        return count
    }

    fun linearSearch(): Int {
        var found = 0
        val namesForSearch = mutableListOf<String>()
        if (fileNamesForSearch.exists() && filePhonebook.exists()) {
            namesForSearch.addAll(fileNamesForSearch.readLines())
            val phonebook = filePhonebook.readText()
            for (name in namesForSearch) {
                if (phonebook.contains(name)) found++
            }
        } else println("Wrong files")
        return found
    }
}
