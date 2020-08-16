package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
    val indexes = mutableListOf<Int>()
    var index = 0
    while (true){
        index = this?.indexOf(substr,index, ignoreCase) ?: -1
        if (index != -1){
            indexes.add(index)
            index++
        }else break
    }
    return indexes

}
