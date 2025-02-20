package com.bezkoder.spring.restapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

	@GetMapping("/helloworld")
	public String getMessage() {
		return "helloworld";
	}
//	http://localhost:8080/helloworld
}
