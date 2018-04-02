package com.surya.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource as Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.PropertySource

@PropertySource("classpath:application.properties")
@SpringBootApplication
open class Application
fun main(args: Array<String>) {
     SpringApplication.run(Application::class.java, *args)
} 