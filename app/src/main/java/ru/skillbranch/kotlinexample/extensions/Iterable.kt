package ru.skillbranch.kotlinexample.extentions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T>{
    var newList: MutableList<T> = mutableListOf()
    for (i in this.indices)
        if (!predicate(this[i])){
            newList.add(this[i])
        }else break
    return newList
}

