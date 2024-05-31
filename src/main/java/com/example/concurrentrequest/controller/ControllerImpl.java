package com.example.concurrentrequest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/request-test")
public class ControllerImpl implements Controller {

	@Override
	@GetMapping
	public String test() throws InterruptedException {
		Thread.sleep(50000);
		return "FINISH";
	}
	
	
	
}
