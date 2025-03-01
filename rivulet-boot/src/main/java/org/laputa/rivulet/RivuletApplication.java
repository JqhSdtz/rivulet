package org.laputa.rivulet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author JQH
 */
@SpringBootApplication
@EnableConfigurationProperties
public class RivuletApplication {

    public static void main(String[] args) {
        SpringApplication.run(RivuletApplication.class, args);
    }

}
