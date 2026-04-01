package ch.ejpd.example.precrime

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import org.jmolecules.archunit.JMoleculesArchitectureRules
import org.jmolecules.archunit.JMoleculesDddRules

@AnalyzeClasses(
    packages = ["ch.ejpd.example.precrime"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
internal class ArchitectureTest {

    @ArchTest
    val ddd: ArchRule = JMoleculesDddRules.all()

    @ArchTest
    val onion: ArchRule = JMoleculesArchitectureRules.ensureOnionSimple()
}