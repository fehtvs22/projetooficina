package br.com.oficina.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Garante a regra de dependência da Clean Architecture: as dependências apontam sempre para dentro
 * (interfaces/infrastructure -> application -> domain) e o núcleo não conhece frameworks.
 */
@AnalyzeClasses(packages = "br.com.oficina", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule camadasConcentricas = onionArchitecture()
            .domainModels("br.com.oficina.domain..")
            .applicationServices("br.com.oficina.application..")
            .adapter("persistencia-seguranca-eventos", "br.com.oficina.infrastructure..")
            .adapter("rest", "br.com.oficina.interfaces..")
            .withOptionalLayers(true);

    @ArchTest
    static final ArchRule dominioIndependenteDeFrameworks = noClasses()
            .that().resideInAPackage("br.com.oficina.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "org.hibernate..", "com.fasterxml..", "tools.jackson..");

    @ArchTest
    static final ArchRule aplicacaoIndependenteDeFrameworks = noClasses()
            .that().resideInAPackage("br.com.oficina.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "org.hibernate..");

    @ArchTest
    static final ArchRule restNaoAcessaPersistencia = noClasses()
            .that().resideInAPackage("br.com.oficina.interfaces..")
            .should().dependOnClassesThat().resideInAPackage("br.com.oficina.infrastructure..");
}
