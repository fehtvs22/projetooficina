package br.com.oficina.domain.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de agregado: acumula eventos de domínio até que a camada de aplicação os publique.
 */
public abstract class AgregadoRaiz {

    private final List<EventoDominio> eventos = new ArrayList<>();

    protected void registrarEvento(EventoDominio evento) {
        eventos.add(evento);
    }

    public List<EventoDominio> extrairEventos() {
        List<EventoDominio> pendentes = List.copyOf(eventos);
        eventos.clear();
        return pendentes;
    }
}
