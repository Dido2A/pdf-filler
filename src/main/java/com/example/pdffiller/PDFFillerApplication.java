package com.example.pdffiller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.itextpdf.commons.actions.EventManager;

import com.example.pdffiller.storage.PDFProperties;
import com.example.pdffiller.storage.StorageProperties;
import com.example.pdffiller.storage.StorageService;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, PDFProperties.class})
public class PDFFillerApplication {

	public static void main(String[] args) {
		//These two lines of code allow us to disable the AGPL warning messages
		// EventManager eventManager = EventManager.getInstance();
		//Note that you acknowledge and agree with the AGPL license requirements when you call this method
		EventManager.acknowledgeAgplUsageDisableWarningMessage();
		SpringApplication.run(PDFFillerApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> storageService.onStart();
	}

}

