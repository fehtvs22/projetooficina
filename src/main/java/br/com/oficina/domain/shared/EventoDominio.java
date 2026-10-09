package br.com.oficina.domain.shared;

import java.time.Instant;

/**
 * Fato relevante que ocorreu no domínio.
 */
public interface EventoDominio {

    Instant ocorridoEm();
}
