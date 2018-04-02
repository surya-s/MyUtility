package com.surya.app

import com.fasterxml.jackson.annotation.JsonInclude
import javax.persistence.Entity
import javax.persistence.Table
import org.springframework.data.annotation.PersistenceConstructor
import javax.persistence.Id
import javax.persistence.GeneratedValue

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "employee")
public data class Employee constructor (@Id @GeneratedValue
				val id: Long,
				val name:String,
				val age:Int){
	public constructor(): this(1, "", 1); 
} 