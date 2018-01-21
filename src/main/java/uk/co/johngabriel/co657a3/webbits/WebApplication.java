package uk.co.johngabriel.co657a3.webbits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import uk.co.johngabriel.co657a3.things.DataManager;

/**
 * WHERE IT ALL BEGINS
 * @author John Gabriel
 */
@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(WebApplication.class, args);
	}

}