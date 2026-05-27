package com.tortiki.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée principal de l'application Tortiki API.
 *
 * <p>Lance le contexte Spring Boot avec la configuration automatique.</p>
 */
@SpringBootApplication
public class TortikiApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(TortikiApiApplication.class, args);
  }

}
