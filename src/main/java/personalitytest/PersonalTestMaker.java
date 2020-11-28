package personalitytest;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * This class is a start point to the personality testing project which gets answers for the personality test questions
 * and sends question and answer details to the users.
 * 
 * @author gizemabali
 *
 */
@SpringBootApplication
public class PersonalTestMaker extends SpringBootServletInitializer {

	/**
	 * Start point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PersonalTestMaker.class, args);
	}

	/**
	 * This method is used to close inner clients gracefully.
	 */
	@PreDestroy
	public void onExit() {
		ElasticClientOperations.getInstance().closeClient();
	}

}
