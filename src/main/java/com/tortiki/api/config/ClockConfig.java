package com.tortiki.api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du bean {@link Clock} pour l'injection dans les services.
 *
 * <p>Centralise la source de temps — remplaçable par un {@code Clock} fixe
 * dans les tests unitaires et d'intégration pour garantir
 * le déterminisme des assertions temporelles.</p>
 */
@Configuration
public class ClockConfig {

  /**
   * Fournit un {@link Clock} UTC pour tous les services applicatifs.
   *
   * @return horloge système UTC
   */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}