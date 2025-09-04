package br.com.systemrpg.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação SystemRPG Backend.
 * <p>
 * Esta classe contém o método main que inicia a aplicação Spring Boot.
 * </p>
 *
 * @author SystemRPG
 * @since 1.0
 */
@SpringBootApplication
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SystemRpgBackendApplication {

	/**
	 * Método principal que inicia a aplicação Spring Boot.
	 *
	 * @param args argumentos da linha de comando
	 */
	public static void main(String[] args) {
		SpringApplication.run(SystemRpgBackendApplication.class, args);
	}

}
