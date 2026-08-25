package com.erp.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the module boundaries that used to be a (partial — see Section 4.2 of the
 * recommendation report below) side effect of separate Maven artifacts. Pom consolidation
 * removed the per-module dependency graph entirely, so THIS suite is now the only thing
 * enforcing "module A may only reach module B through B's own {@code crossmodule} package" —
 * there is no compile-time Maven check left to fall back on.
 *
 * <p>See governance/project-artifacts/INTERFACE-VS-REST-AND-POM-STRUCTURE-RECOMMENDATION.md
 * and governance/.github/skills/backend/create-service/SKILL.md's "Cross-Module Calls (XM)"
 * section.
 */
@AnalyzeClasses(packages = "com.erp")
public class CrossModuleBoundaryArchTest {

    /**
     * One entry per business module: its own package prefix, and its designated public
     * {@code crossmodule} sub-package. {@code erp-finance-gl} has no source yet (it was a
     * pom-only module before consolidation, per its own real Maven dependency on
     * erp-masterdata) — there is no package to select a rule against yet. Add an entry the
     * moment it gets its first class, restricting it to depending only on
     * {@code com.erp.masterdata.crossmodule}.
     */
    private static final List<Module> MODULES = List.of(
            new Module("com.erp.security", "com.erp.security.crossmodule"),
            new Module("com.erp.notification", "com.erp.notification.crossmodule"),
            new Module("com.erp.org", "com.erp.org.crossmodule"),
            new Module("com.erp.masterdata", "com.erp.masterdata.crossmodule"),
            new Module("com.erp.file", "com.erp.file.crossmodule")
    );

    /**
     * The one currently-accepted SpEL/reflection reference that crosses a module boundary
     * without going through a {@code crossmodule} interface — {@code SecurityPermissions} is a
     * small, intentional shared constants registry, referenced via
     * {@code hasAuthority(T(...))} SpEL because {@code @PreAuthorize} expressions are strings,
     * not real imports (see the recommendation report's Section 4.2 finding: this reference
     * predates and is independent of this ArchUnit suite). Any OTHER SpEL type reference that
     * crosses a module boundary is a new, unreviewed bypass and must fail the build.
     */
    private static final String TRACKED_SPEL_EXCEPTION = "com.erp.security.constants.SecurityPermissions";

    private record Module(String packagePrefix, String crossModulePackage) {
    }

    /**
     * The structural half of the boundary: no class outside module X's own package may depend
     * on a class inside module X's package unless that class is in module X's
     * {@code crossmodule} sub-package.
     */
    @ArchTest
    static void modules_only_expose_their_crossmodule_package_to_outsiders(JavaClasses classes) {
        for (Module module : MODULES) {
            ArchRule rule = noClasses().that().resideOutsideOfPackage(module.packagePrefix() + "..")
                    .should().dependOnClassesThat(
                            JavaClass.Predicates.resideInAPackage(module.packagePrefix() + "..")
                                    .and(DescribedPredicate.not(
                                            JavaClass.Predicates.resideInAPackage(module.crossModulePackage() + "..")))
                    )
                    .as("classes outside " + module.packagePrefix()
                            + " must only depend on its " + module.crossModulePackage() + " surface");
            rule.check(classes);
        }
    }

    /**
     * The half normal ArchUnit dependency rules structurally cannot see: a
     * {@code @PreAuthorize} SpEL string's {@code T(...)} type reference is a plain String
     * constant in bytecode, not a real class dependency — see the recommendation report's
     * Section 4.2. This walks every {@code @PreAuthorize}-annotated method's expression looking
     * for a {@code T(fully.qualified.Type)} reference that crosses a module boundary and isn't
     * {@link #TRACKED_SPEL_EXCEPTION}.
     */
    @ArchTest
    static void spel_type_references_do_not_bypass_the_module_boundary(JavaClasses classes) {
        Pattern typeReference = Pattern.compile("T\\(([a-zA-Z0-9_.]+)\\)");
        for (JavaClass clazz : classes) {
            for (JavaMethod method : clazz.getMethods()) {
                if (!method.isAnnotatedWith(PreAuthorize.class)) {
                    continue;
                }
                String expression = method.getAnnotationOfType(PreAuthorize.class).value();
                Matcher matcher = typeReference.matcher(expression);
                while (matcher.find()) {
                    String referencedType = matcher.group(1);
                    if (referencedType.equals(TRACKED_SPEL_EXCEPTION)) {
                        continue;
                    }
                    String callerModule = topLevelModuleOf(clazz.getPackageName());
                    String referencedModule = topLevelModuleOf(packageOf(referencedType));
                    if (!callerModule.equals(referencedModule)) {
                        throw new AssertionError(
                                "New cross-module @PreAuthorize SpEL type reference found: "
                                        + clazz.getFullName() + "#" + method.getName()
                                        + " references " + referencedType
                                        + " — this bypasses the structural ArchUnit rule above and was not "
                                        + "reviewed. If this is a deliberate, accepted exception (like "
                                        + TRACKED_SPEL_EXCEPTION + "), add it explicitly to "
                                        + "TRACKED_SPEL_EXCEPTION handling in this test — do not let it "
                                        + "pass silently.");
                    }
                }
            }
        }
    }

    private static String topLevelModuleOf(String packageName) {
        for (Module module : MODULES) {
            if (packageName.equals(module.packagePrefix()) || packageName.startsWith(module.packagePrefix() + ".")) {
                return module.packagePrefix();
            }
        }
        return "shared";
    }

    private static String packageOf(String fullyQualifiedClassName) {
        int lastDot = fullyQualifiedClassName.lastIndexOf('.');
        return lastDot < 0 ? "" : fullyQualifiedClassName.substring(0, lastDot);
    }
}
