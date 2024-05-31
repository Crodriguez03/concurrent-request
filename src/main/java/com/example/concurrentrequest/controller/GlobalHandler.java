package com.example.concurrentrequest.controller;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GlobalHandler implements Filter {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
	
	public final AtomicInteger concurrentRequest = new AtomicInteger();

	@Value("${server.tomcat.threads.max}")
	private int maxConnetion;
	
	@Value("${app.healthcheck.path}")
	private String healthCkeckPath;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		try {
			int count = concurrentRequest.incrementAndGet();
			if (count < maxConnetion || isHealthCheck(request)) {
				chain.doFilter(request, response);
			} else {
				HttpServletResponse resp = (HttpServletResponse) response;
				resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			}
		} finally {
			concurrentRequest.decrementAndGet();
		}
	}
	
	private boolean isHealthCheck(ServletRequest request) {
		try {
			HttpServletRequest req = (HttpServletRequest) request;
			String requestUri = req.getRequestURI();
			Path path = Paths.get(requestUri);
			Path lastSegment = path.getName(path.getNameCount() - 1);
			boolean isHealthCheck = healthCkeckPath.equals(lastSegment.toString());
			if(isHealthCheck) {
				log.info("Dejamos pasar la llamada a {}", healthCkeckPath);
			}
			
			return isHealthCheck;
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostConstruct
	private void init() {
		ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		});
		
		exec.execute(() -> {
			while (true) {
				log.info("CONCURRENT_REQUEST {}, timestamp: {}", 
						concurrentRequest.get(), System.currentTimeMillis());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		
		exec.shutdown();
	}
}