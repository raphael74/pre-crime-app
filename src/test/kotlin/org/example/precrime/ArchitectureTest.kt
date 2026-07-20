package org.example.precrime

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.jmolecules.archunit.JMoleculesArchitectureRules
import org.jmolecules.archunit.JMoleculesDddRules

@AnalyzeClasses(
    packages = ["org.example.precrime"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
internal class ArchitectureTest {

    companion object {
        private const val DOMAIN_PACKAGE = "..domain.."
        private const val APPLICATION_PACKAGE = "..application.."
        private const val JAVA_PACKAGES = "java.."
        private const val KOTLIN_PACKAGES = "kotlin.."
        private const val JMOLECULES_PACKAGES = "org.jmolecules.."
        private const val JETBRAINS_PACKAGES = "org.jetbrains.."
        private const val SLF4J_PACKAGES = "org.slf4j.."
        private const val SPRING_STEREOTYPE_PACKAGES = "org.springframework.stereotype.."
        private const val SPRING_TRANSACTION_PACKAGES = "org.springframework.transaction.."

        private val COMMON_ALLOWED_PACKAGES = arrayOf(
            JAVA_PACKAGES,
            KOTLIN_PACKAGES,
            JMOLECULES_PACKAGES,
            JETBRAINS_PACKAGES
        )

        private val DOMAIN_ALLOWED_PACKAGES = arrayOf(
            DOMAIN_PACKAGE,
            *COMMON_ALLOWED_PACKAGES
        )

        private val APPLICATION_ALLOWED_PACKAGES = arrayOf(
            APPLICATION_PACKAGE,
            DOMAIN_PACKAGE,
            SLF4J_PACKAGES,
            SPRING_STEREOTYPE_PACKAGES,
            SPRING_TRANSACTION_PACKAGES,
            *COMMON_ALLOWED_PACKAGES
        )
    }

    @ArchTest
    val ddd: ArchRule = JMoleculesDddRules.all()

    @ArchTest
    val onion: ArchRule = JMoleculesArchitectureRules.ensureOnionSimple()

    @ArchTest
    val domainShouldNotDependOnFrameworks: ArchRule = noClasses()
        .that().resideInAPackage(DOMAIN_PACKAGE)
        .should().dependOnClassesThat().resideOutsideOfPackages(*DOMAIN_ALLOWED_PACKAGES)

    @ArchTest
    val applicationShouldOnlyDependOnDomainAndLimitedSpring: ArchRule = noClasses()
        .that().resideInAPackage(APPLICATION_PACKAGE)
        .should().dependOnClassesThat().resideOutsideOfPackages(*APPLICATION_ALLOWED_PACKAGES)
}