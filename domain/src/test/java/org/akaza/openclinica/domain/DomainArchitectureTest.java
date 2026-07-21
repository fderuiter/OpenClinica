package org.akaza.openclinica.domain;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class DomainArchitectureTest {
    @Test
    public void domainShouldNotDependOnOtherModules() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.akaza.openclinica");

        ArchRule rule = noClasses().that().resideInAPackage("org.akaza.openclinica..")
            .should().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.dao..")
            .orShould().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.logic..")
            .orShould().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.job..")
            .orShould().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.controller..")
            .orShould().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.web..")
            .orShould().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.ws..")
            .orShould().dependOnClassesThat().resideInAPackage("org.akaza.openclinica.modern..")
            .orShould().dependOnClassesThat().resideInAPackage("org.springframework..")
            .orShould().dependOnClassesThat().resideInAPackage("javax.persistence..")
            .orShould().dependOnClassesThat().resideInAPackage("org.hibernate..")
            .orShould().dependOnClassesThat().resideInAPackage("javax.servlet..")
            .orShould().dependOnClassesThat().resideInAPackage("java.sql..");

        rule.check(importedClasses);
    }
}
