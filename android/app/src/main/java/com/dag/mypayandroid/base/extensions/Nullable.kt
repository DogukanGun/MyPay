package com.dag.mypayandroid.base.extensions

inline fun<reified T> ifNull(value:T?,notNullClosure:(value:T)-> Unit,nullClosure:(()-> Unit) = { null }){
    if (value != null){
        notNullClosure(value)
    }else{
        nullClosure()
    }
}