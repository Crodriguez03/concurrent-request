package com.example.concurrentrequest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.healthcheck.path}")
public class HealthCheckController {

	@GetMapping
	public String check() {
		return "OK";
	}
}
