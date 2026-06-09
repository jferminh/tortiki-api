package com.tortiki.api.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client MinIO pour le stockage des photos d'annonces.
 *
 * <p>Instancie le bean {@link MinioClient} à partir des propriétés YAML
 * selon le profil actif (dev ou prod).</p>
 *
 * <p>Les credentials de production sont injectés via variables d'environnement
 * conformément aux bonnes pratiques OWASP (pas de secrets en dur).</p>
 */
@Slf4j
@Configuration
public class MinioConfig {

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  /**
   * Crée et expose le client MinIO comme bean Spring.
   *
   * @return instance configurée de {@link MinioClient}
   */
  @Bean
  public MinioClient minioClient() {
    log.info("Initialisation du client MinIO sur endpoint : {}", endpoint);
    return MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build();
  }
}