package com.tngtech.archunit.lang.conditions;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.Collections;

import com.tngtech.archunit.core.DescribedPredicate;
import com.tngtech.archunit.core.HasParameters;
import com.tngtech.archunit.core.JavaClass;
import com.tngtech.archunit.core.JavaMethod;
import com.tngtech.archunit.core.TypeDetails;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.tngtech.archunit.core.JavaFieldAccess.AccessType.SET;
import static com.tngtech.archunit.core.TestUtils.javaClass;
import static com.tngtech.archunit.core.TestUtils.javaMethod;
import static com.tngtech.archunit.core.TestUtils.predicateWithDescription;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.accessType;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.hasParameters;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.named;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.ownerAndNameAre;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.ownerIs;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.resideIn;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.targetTypeResidesIn;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.theHierarchyOf;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.theHierarchyOfAClassThat;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ArchPredicatesTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private JavaClass mockClass;

    @Test
    public void matches_class_package() {
        when(mockClass.getPackage()).thenReturn("some.arbitrary.pkg");

        assertThat(resideIn("some..pkg").apply(mockClass)).as("package matches").isTrue();
    }

    @Test
    public void mismatches_class_package() {
        when(mockClass.getPackage()).thenReturn("wrong.arbitrary.pkg");

        assertThat(resideIn("some..pkg").apply(mockClass)).as("package matches").isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void matches_annotation() {
        when(mockClass.reflect()).thenReturn((Class) AnnotatedClass.class);

        assertThat(annotatedWith(SomeAnnotation.class).apply(mockClass))
                .as("annotated class matches")
                .isTrue();

        when(mockClass.reflect()).thenReturn((Class) NotAnnotatedClass.class);

        assertThat(annotatedWith(SomeAnnotation.class).apply(mockClass))
                .as("annotated class matches")
                .isFalse();
    }

    @Test
    public void matches_name() {
        when(mockClass.getName()).thenReturn("com.tngtech.SomeClass");

        assertThat(named(".*Class").apply(mockClass)).as("class name matches").isTrue();
        assertThat(named(".*Wrong").apply(mockClass)).as("class name matches").isFalse();
        assertThat(named("com.*").apply(mockClass)).as("class name matches").isTrue();
        assertThat(named("wrong.*").apply(mockClass)).as("class name matches").isFalse();
        assertThat(named(".*\\.S.*s").apply(mockClass)).as("class name matches").isTrue();
        assertThat(named(".*W.*").apply(mockClass)).as("class name matches").isFalse();
    }

    @Test
    public void inTheHierarchyOfAClass_matches_class_itself() {
        assertThat(theHierarchyOfAClassThat(named(".*Class")).apply(javaClass(AnnotatedClass.class)))
                .as("class itself matches the predicate").isTrue();
    }

    @Test
    public void inTheHierarchyOfAClass_matches_subclass() {
        assertThat(theHierarchyOfAClassThat(named(".*Annotated.*")).apply(javaClass(SubClass.class)))
                .as("subclass matches the predicate").isTrue();
    }

    @Test
    public void inTheHierarchyOfAClass_does_not_match_superclass() {
        assertThat(theHierarchyOfAClassThat(named(".*Annotated.*")).apply(javaClass(Object.class)))
                .as("superclass matches the predicate").isFalse();
    }

    @Test
    public void descriptions() {
        assertThat(resideIn("..any..").getDescription())
                .isEqualTo("reside in '..any..'");

        assertThat(annotatedWith(Rule.class).getDescription())
                .isEqualTo("annotated with @Rule");

        assertThat(named("any").getDescription())
                .isEqualTo("named 'any'");

        assertThat(theHierarchyOf(Object.class).getDescription())
                .isEqualTo("the hierarchy of Object.class");

        assertThat(theHierarchyOfAClassThat(predicateWithDescription("something")).getDescription())
                .isEqualTo("the hierarchy of a class that something");

        assertThat(ownerAndNameAre(System.class, "out").getDescription())
                .isEqualTo("owner is java.lang.System and name is 'out'");

        assertThat(ownerIs(System.class).getDescription())
                .isEqualTo("owner is java.lang.System");

        assertThat(accessType(SET).getDescription())
                .isEqualTo("access type " + SET);

        assertThat(targetTypeResidesIn("..any..").getDescription())
                .isEqualTo("target type resides in '..any..'");
    }

    @Test
    public void hasParameters_works() {
        JavaMethod method = javaMethod(SomeClass.class, "withArgs", Object.class, String.class);

        DescribedPredicate<HasParameters> predicate =
                hasParameters(TypeDetails.allOf(Collections.<Class<?>>singletonList(Object.class)));

        assertThat(predicate.apply(method)).as("Predicate matches").isFalse();
        assertThat(predicate.getDescription()).isEqualTo("has parameters [Object.class]");

        predicate =
                hasParameters(TypeDetails.allOf(Arrays.asList(Object.class, String.class)));

        assertThat(predicate.apply(method)).as("Predicate matches").isTrue();
        assertThat(predicate.getDescription()).isEqualTo("has parameters [Object.class, String.class]");
    }

    private static class SomeClass {
        void withArgs(Object arg, String stringArg) {
        }
    }

    @Retention(RUNTIME)
    @interface SomeAnnotation {
    }

    @SomeAnnotation
    public static class AnnotatedClass {
    }

    static class SubClass extends AnnotatedClass {
    }

    public static class NotAnnotatedClass {
    }
}