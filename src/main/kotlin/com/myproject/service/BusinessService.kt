package com.myproject.service

import com.myproject.model.DataModel

class BusinessService {
    fun processData(data: DataModel) {
        println("Processing data: ${data.name} (ID: ${data.id})")
        // Further business logic here
    }
}