package com.example.bookreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.bookreview.service.BookService;

@SpringBootApplication
public class BookreviewApplication implements CommandLineRunner {

	@Autowired
	private BookService bookService;

	public static void main(String[] args) {
		SpringApplication.run(BookreviewApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Initialize sample books if database is empty
		bookService.initializeSampleBooks();
	}

}
