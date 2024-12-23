package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.demo.job.ReportGenerationScheduler;

@SpringBootApplication
@EnableScheduling
public class SbCronjobAppApplication implements CommandLineRunner {
	
	  @Autowired
	    private ReportGenerationScheduler scheduler;

	public static void main(String[] args) {
		SpringApplication.run(SbCronjobAppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		scheduler.generateReportAndSendEmail();
	}
}
