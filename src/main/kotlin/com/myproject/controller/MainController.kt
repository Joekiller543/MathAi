package com.myproject.controller

import com.myproject.model.DataModel
import com.myproject.service.BusinessService

class MainController(private val businessService: BusinessService) {
    fun handleRequest(requestId: String, data: DataModel) {
        println("Handling request $requestId with data: ${data.name}")
        businessService.processData(data)
    }
}