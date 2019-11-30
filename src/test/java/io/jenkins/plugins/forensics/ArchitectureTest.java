package io.jenkins.plugins.forensics;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.hm.hafner.util.ArchitectureRules;

import static com.tngtech.archunit.base.DescribedPredicate.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Defines several architecture rules for the static analysis model and parsers.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("hideutilityclassconstructor")
@AnalyzeClasses(packages = "io.jenkins.plugins.forensics..")
class ArchitectureTest {
    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_CLASSES =
            noClasses().that().haveSimpleNameEndingWith("Test")
                    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and(doNot(have(simpleNameEndingWith("ITest"))))
                    .should().bePublic();

    @ArchTest
    static final ArchRule NO_TEST_API_CALLED = ArchitectureRules.NO_TEST_API_CALLED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED = noClasses()
            .should().dependOnClassesThat(resideInAnyPackage(
                    "org.apache.commons.lang..",
                    "org.joda.time..",
                    "org.eclipse..",
                    "javax.xml.bind..",
                    "javax.annotation..",
                    "com.google.common.."));

    @ArchTest
    static final ArchRule NO_FORBIDDEN_CLASSES_CALLED = ArchitectureRules.NO_FORBIDDEN_CLASSES_CALLED;
}
