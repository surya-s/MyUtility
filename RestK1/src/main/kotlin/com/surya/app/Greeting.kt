package com.surya.app;

import com.fasterxml.jackson.annotation.JsonInclude
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.GeneratedValue
import javax.persistence.Id
import org.springframework.data.annotation.PersistenceConstructor

data class Greeting(val id: Long, val content: String)

/*@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "employee")
data class Employee @PersistenceConstructor constructor(@Id @GeneratedValue val id: Long, val name:String, val age:Int)*/

