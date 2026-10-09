package br.com.oficina.application.shared;

import br.com.oficina.domain.shared.EventoDominio;

import java.util.List;

/**
 * Porta de saída para publicação dos eventos de domínio gerados pelos agregados.
 */
public interface PublicadorEventos {

    void publicar(List<EventoDominio> eventos);
}
