package br.com.oficina.infrastructure.evento;

import br.com.oficina.application.shared.PublicadorEventos;
import br.com.oficina.domain.shared.EventoDominio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador que entrega os eventos de domínio ao barramento de eventos do Spring.
 */
@Component
public class SpringPublicadorEventos implements PublicadorEventos {

    private final ApplicationEventPublisher publisher;

    public SpringPublicadorEventos(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(List<EventoDominio> eventos) {
        eventos.forEach(publisher::publishEvent);
    }
}
