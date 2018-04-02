package com.surya.app

import com.surya.app.Greeting;

import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.atomic.AtomicLong
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.rest.core.annotation.RepositoryRestResource as Resource
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import javax.websocket.server.PathParam
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.data.repository.CrudRepository


@Resource(collectionResourceRel = "employee", path = "employee")
interface EmployeeRepository : CrudRepository<Employee, Long>

//@Autowired
//    val repository: EmployeeRepository ?= null


@RestController  
class GreetingController {

    val counter = AtomicLong()

	@Autowired
	lateinit var repository: EmployeeRepository
	
    @GetMapping("/greeting") 
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String,
				 @RequestParam(value = "name1", defaultValue = "World") name1: String) =
            Greeting(counter.incrementAndGet(), "Hello, $name, $name1")
	
	@GetMapping("/add")
	fun add(@RequestParam(value="one") one:Int,
		   @RequestParam(value="two") two:Int) : Int {
			   var x = one + two;
			 return x;
		   }
	
	@PutMapping("/addemp")
	fun addEmployee(@RequestBody employee : Employee) : String{
		val e = repository.findOne(employee.id);
		print(e);
		repository.save(employee);
		return "Employee added to Repo  " + employee;
	}

//	
//	@GetMapping("getemp/{id}")
//	fun getEmployee(@PathVariable id : Long) : Employee {
//		return repository?.findOne(id);
//	}
} 