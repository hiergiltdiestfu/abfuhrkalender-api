package info.hiergiltdiestfu.waste.extractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TODO doku, externalization; less fast on the fail-fast; put SR API config in properties
 * 
 * @author @@hiergiltdiestfu
 *
 */
@SpringBootApplication
//@EnableDiscoveryClient
public class AbfuhrplanExtractorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbfuhrplanExtractorApplication.class, args);
	}
}
