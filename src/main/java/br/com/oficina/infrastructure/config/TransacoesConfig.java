package br.com.oficina.infrastructure.config;

import org.springframework.aop.Advisor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.List;

/**
 * Transações declarativas aplicadas a todos os casos de uso sem anotar a camada de aplicação.
 * <p>
 * Cada método público de uma classe *UseCase é uma unidade de trabalho atômica: ou todas as alterações
 * (ex.: aprovar orçamento + baixar estoque de várias peças) são gravadas, ou nenhuma. Consultas rodam
 * em transações somente leitura.
 */
@Configuration
public class TransacoesConfig {

    private static final String PACOTE_APLICACAO = "br.com.oficina.application";

    /**
     * O interceptor resolve o TransactionManager sob demanda (via BeanFactory), evitando inicializar
     * DataSource/JPA antes dos BeanPostProcessors.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static TransactionInterceptor interceptorTransacoesCasosDeUso() {
        RuleBasedTransactionAttribute leitura = new RuleBasedTransactionAttribute();
        leitura.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        leitura.setReadOnly(true);

        RuleBasedTransactionAttribute escrita = new RuleBasedTransactionAttribute(
                TransactionDefinition.PROPAGATION_REQUIRED, List.of(new RollbackRuleAttribute(Exception.class)));

        NameMatchTransactionAttributeSource atributos = new NameMatchTransactionAttributeSource();
        for (String prefixo : List.of("buscar*", "listar*", "consultar*", "calcular*")) {
            atributos.addTransactionalMethod(prefixo, leitura);
        }
        atributos.addTransactionalMethod("*", escrita);

        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(atributos);
        return interceptor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor transacoesCasosDeUso(
            @Qualifier("interceptorTransacoesCasosDeUso") TransactionInterceptor interceptor) {
        ClassFilter casosDeUso = classe -> classe.getPackageName().startsWith(PACOTE_APLICACAO)
                && classe.getSimpleName().endsWith("UseCase");
        return new DefaultPointcutAdvisor(new ComposablePointcut(casosDeUso), interceptor);
    }
}
