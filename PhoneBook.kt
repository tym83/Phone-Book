package phonebook

import java.io.File
import kotlin.math.floor
import kotlin.system.exitProcess

fun main() {
    val phoneBook = PhoneBook()
    println("Start searching (linear search)...")
    phoneBook.doLinearSearch()
    println("Start searching (bubble sort + jump search)...")
    phoneBook.doJumpSearchAllElements()
    println("Start searching (quick sort + binary search)...")
    phoneBook.searchBinaryAllElements()
    println("Start searching (hash table)...")
    phoneBook.searchHashMap()
}

class PhoneBook {
    private val phoneBook = File("/home/tym83/Downloads/directory.txt").readLines().toMutableList()
    private val contacts = File("/home/tym83/Downloads/find.txt").readLines()

    data class PhoneBookNameFirst(
        val phoneBookNameFirst: MutableList<String>,
        val orderedNumbers: MutableList<String>
    )

    fun doLinearSearch() {
        val startTime = System.currentTimeMillis()
        val foundedContacts = mutableSetOf<String>()
        contacts.forEach { contact -> phoneBook.forEach { if (it.contains(contact)) foundedContacts.add(contact) } }

        val linearSearchParam = mutableMapOf<String, Number>()
        linearSearchParam["contactsForSearch"] = contacts.size
        linearSearchParam["foundedContacts"] = foundedContacts.size
        linearSearchParam["searchingTime"] = System.currentTimeMillis() - startTime

        printLinearSearch(linearSearchParam)
    }

    private fun printLinearSearch(searchParam: MutableMap<String, Number>) {
        println("Found ${searchParam["contactsForSearch"]} / ${searchParam["foundedContacts"]} entries. " +
                "Time taken: ${convertTime(searchParam["searchingTime"]!!)}\n")
    }

    private fun doPhoneBookNameFirst(): PhoneBookNameFirst {
        val phoneBookNameFirst = mutableListOf<String>()
        phoneBookNameFirst.addAll(phoneBook)
        for (i in phoneBookNameFirst.indices) {
            val number = phoneBookNameFirst[i].substring(0, phoneBookNameFirst[i].indexOf(' ') + 1)
            phoneBookNameFirst[i] = (phoneBookNameFirst[i].drop(number.length) + " $number").trimEnd()
        }

        return PhoneBookNameFirst(phoneBookNameFirst, mutableListOf())
    }

    private fun doBubbleSort(phoneBookNameFirst: MutableList<String>, orderedNumbers: MutableList<String>) {
        var isNotSorted = true
        while (isNotSorted) {
            isNotSorted = false
            for (y in 0 until phoneBookNameFirst.lastIndex) {
                if (phoneBookNameFirst[y] > phoneBookNameFirst[y + 1]) {
                    phoneBookNameFirst[y] = phoneBookNameFirst[y + 1]
                        .also { phoneBookNameFirst[y + 1] = phoneBookNameFirst[y] }
                    isNotSorted = true
                }
            }
        }

        for (i in phoneBookNameFirst.indices) {
            orderedNumbers.add(phoneBookNameFirst[i].replace("[a-zA-Z]+ [a-zA-Z]* ?".toRegex(), ""))
            phoneBookNameFirst[i] = phoneBookNameFirst[i].replace(" \\d+".toRegex(), "")
        }
    }

    private fun doJumpSearchOneElement(
        contact: String,
        phoneBookNameFirst: MutableList<String>,
        orderedNumbers: MutableList<String>
    ): String? {
        val blockSize = floor(phoneBook.size.toFloat()).toInt()
        var curr = 0
        while (curr <= phoneBookNameFirst.lastIndex) {
            if (phoneBookNameFirst[curr] == contact) {
                return "${phoneBookNameFirst[curr]} ${orderedNumbers[curr]}"
            } else if (phoneBookNameFirst[curr] > contact) {
                var ind = curr - 1
                while (ind > curr - blockSize && ind >= 1) {
                    if (phoneBookNameFirst[ind] == contact) {
                        return "${phoneBookNameFirst[ind]} ${orderedNumbers[ind]}"
                    }
                    ind --
                }
            }
            curr += blockSize
        }
        var ind = phoneBookNameFirst.lastIndex

        while (ind > curr - blockSize) {
            if (phoneBookNameFirst[ind] == contact) {
                return "${phoneBookNameFirst[ind]} ${orderedNumbers[ind]}"
            }
            ind --
        }
        return null
    }

    fun doJumpSearchAllElements() {
        val foundContacts = mutableSetOf<String>()
        val orderedPhoneBook = doPhoneBookNameFirst()
        val startSortingTime = System.currentTimeMillis()
        doBubbleSort(orderedPhoneBook.phoneBookNameFirst, orderedPhoneBook.orderedNumbers)
        val startSearchingTime = System.currentTimeMillis()
        for (contact in contacts) {
            foundContacts.add(doJumpSearchOneElement(contact, orderedPhoneBook.phoneBookNameFirst,
                orderedPhoneBook.orderedNumbers) ?: continue)
        }

        val jumpSearchParam = mutableMapOf<String, Number>()
        jumpSearchParam["contactsForSearch"] = contacts.size
        jumpSearchParam["foundedContacts"] = foundContacts.size
        jumpSearchParam["searchingTime"] = System.currentTimeMillis() - startSearchingTime
        jumpSearchParam["sortingTime"] = startSearchingTime - startSortingTime
        jumpSearchParam["allTime"] = System.currentTimeMillis() - startSortingTime

        printSearch(jumpSearchParam)
    }

    private fun sortQuick(phoneBookNameFirst: MutableList<String>, orderedNumbers: MutableList<String>) {
        val phoneBook = recursionQuickSearch(phoneBookNameFirst)
        phoneBookNameFirst.clear()
        phoneBookNameFirst.addAll(phoneBook)

        for (i in phoneBookNameFirst.indices) {
            orderedNumbers.add(phoneBookNameFirst[i].replace("[a-zA-Z]+ [a-zA-Z]* ?".toRegex(), ""))
            phoneBookNameFirst[i] = phoneBookNameFirst[i].replace(" \\d+".toRegex(), "")
        }
    }

    private fun recursionQuickSearch(phoneBook: MutableList<String>): MutableList<String> {
        return if (phoneBook.size < 2) {
            phoneBook
        } else {
            val pivot = phoneBook.last()
            val lesser = mutableListOf<String>()
            val greater = mutableListOf<String>()

            for (i in 0 until phoneBook.lastIndex) {
                if (phoneBook[i] > pivot) {
                    greater.add(phoneBook[i])
                } else {
                    lesser.add(phoneBook[i])
                }
            }
            return (recursionQuickSearch(lesser) + pivot + recursionQuickSearch(greater)).toMutableList()
        }
    }

    private fun searchBinaryOneElement(
        contact: String,
        phoneBookNameFirst: MutableList<String>,
        orderedNumbers: MutableList<String>
    ): String? {
        var left = 0
        var right = phoneBookNameFirst.lastIndex
        while (left <= right) {
            val middle = (left + right) / 2
            if (contact == phoneBookNameFirst[middle]) {
                return "${phoneBookNameFirst[middle]} ${orderedNumbers[middle]}"
            } else if (contact < phoneBookNameFirst[middle]) {
                right = middle - 1
            } else {
                left = middle + 1
            }
        }
        return null
    }

    fun searchBinaryAllElements() {
        val foundContacts = mutableSetOf<String>()
        val orderedPhoneBook = doPhoneBookNameFirst()
        val startSortingTime = System.currentTimeMillis()
        sortQuick(orderedPhoneBook.phoneBookNameFirst, orderedPhoneBook.orderedNumbers)
        val startSearchingTime = System.currentTimeMillis()
        for (contact in contacts) {
            foundContacts.add(searchBinaryOneElement(contact, orderedPhoneBook.phoneBookNameFirst,
                orderedPhoneBook.orderedNumbers) ?: continue)
        }

        val binarySearchParam = mutableMapOf<String, Number>()
        binarySearchParam["contactsForSearch"] = contacts.size
        binarySearchParam["foundedContacts"] = foundContacts.size
        binarySearchParam["searchingTime"] = System.currentTimeMillis() - startSearchingTime
        binarySearchParam["sortingTime"] = startSearchingTime - startSortingTime
        binarySearchParam["allTime"] = System.currentTimeMillis() - startSortingTime

        printSearch(binarySearchParam)
    }

    private fun createHashMap(): HashMap<String, String> {
        val phoneBookNameFirst = hashMapOf<String, String>()
        for (i in phoneBook.indices) {
            val number = phoneBook[i].substring(0, phoneBook[i].indexOf(' ') + 1).trimEnd()
            val name = phoneBook[i].drop(number.length).trimEnd()
            phoneBookNameFirst[name] = number
        }

        return phoneBookNameFirst
    }

    fun searchHashMap() {
        val startCreateTime = System.currentTimeMillis()
        val hashMap = createHashMap()
        val startSearchingTime = System.currentTimeMillis()
        val foundContacts = mutableListOf<String>()
        for (name in contacts) {
            foundContacts.add("$name ${hashMap[name]}")
        }

        val hashSearchParam = mutableMapOf<String, Number>()
        hashSearchParam["contactsForSearch"] = contacts.size
        hashSearchParam["foundedContacts"] = foundContacts.size
        hashSearchParam["searchingTime"] = System.currentTimeMillis() - startSearchingTime
        hashSearchParam["creatingTime"] = startSearchingTime - startCreateTime
        hashSearchParam["allTime"] = System.currentTimeMillis() - startCreateTime

        printHashSearch(hashSearchParam)
    }

    private fun printHashSearch(searchParam: MutableMap<String, Number>) {
        println("Found ${searchParam["foundedContacts"]} / ${searchParam["contactsForSearch"]} entries. " +
                "Time taken: ${convertTime(searchParam["allTime"]!!)}\n" +
                "Creating time: ${convertTime(searchParam["creatingTime"]!!)}\n" +
                "Searching time: ${convertTime(searchParam["searchingTime"]!!)}\n")
    }

    private fun printSearch(searchParam: MutableMap<String, Number>) {
        println("Found ${searchParam["foundedContacts"]} / ${searchParam["contactsForSearch"]} entries. " +
                "Time taken: ${convertTime(searchParam["allTime"]!!)}\n" +
                "Sorting time: ${convertTime(searchParam["sortingTime"]!!)}\n" +
                "Searching time: ${convertTime(searchParam["searchingTime"]!!)}\n")
    }

    private fun convertTime(time: Number): String {
        val minutes = (time.toLong() / 60000)
        val seconds = (time.toLong() % 60000 / 1000)
        val milliseconds = (time.toLong() / 60000 % 1000)

        return "$minutes min. $seconds sec. $milliseconds ms."
    }
}
