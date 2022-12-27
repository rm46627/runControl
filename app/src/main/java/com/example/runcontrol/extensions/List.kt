package com.example.runcontrol.extensions

    fun <T> List<T>.toPairs(): Array<Pair<Number, T>> {
        val list: MutableList<Pair<Number, T>> = arrayListOf()
        this.forEach {
            list.add(Pair(this.indexOf(it) + 1, it))
        }
        return list.toTypedArray()
    }