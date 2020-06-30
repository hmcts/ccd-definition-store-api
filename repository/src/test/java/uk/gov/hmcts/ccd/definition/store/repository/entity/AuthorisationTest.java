package uk.gov.hmcts.ccd.definition.store.repository.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthorisationTest {

    Authorisation classUnderTest;
    Authorisation other;

    @BeforeEach
    void setUp() {
        classUnderTest = new ComplexFieldACLEntity();
        classUnderTest.setCreate(false);
        classUnderTest.setRead(false);
        classUnderTest.setUpdate(false);
        classUnderTest.setDelete(false);

        other = new ComplexFieldACLEntity();
        other.setCreate(false);
        other.setRead(false);
        other.setUpdate(false);
        other.setDelete(false);

    }

    @Test
    @DisplayName("should return false when both C is false")
    void shouldReturnFalseCreateWhenBothAreFalse() {
        classUnderTest.setCreate(false);
        other.setCreate(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other C is false and class under test C is true")
    void shouldReturnFalseCreateWhenOtherIsFalseAndClassUnderTestIsTrue() {
        classUnderTest.setCreate(true);
        other.setCreate(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other C is true and class under test is C true")
    void shouldReturnFalseCreateWhenOtherIsTrueAndClassUnderTestIsTrue() {
        classUnderTest.setCreate(true);
        other.setCreate(true);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return true when other C is true and class under test C is false")
    void shouldReturnTrueCreateWhenOtherIsTrueAndClassUnderTestIsFalse() {
        classUnderTest.setCreate(false);
        other.setCreate(true);
        assertTrue(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when both R is false")
    void shouldReturnFalseReadWhenBothAreFalse() {
        classUnderTest.setRead(false);
        other.setRead(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other R is false and class under test R is true")
    void shouldReturnFalseReadWhenOtherIsFalseAndClassUnderTestIsTrue() {
        classUnderTest.setRead(true);
        other.setRead(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other R is true and class under test is R true")
    void shouldReturnFalseReadWhenOtherIsTrueAndClassUnderTestIsTrue() {
        classUnderTest.setRead(true);
        other.setRead(true);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return true when other R is true and class under test R is false")
    void shouldReturnTrueReadWhenOtherIsTrueAndClassUnderTestIsFalse() {
        classUnderTest.setRead(false);
        other.setRead(true);
        assertTrue(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when both U is false")
    void shouldReturnFalseUpdateWhenBothAreFalse() {
        classUnderTest.setUpdate(false);
        other.setUpdate(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other U is false and class under test U is true")
    void shouldReturnFalseUpdateWhenOtherIsFalseAndClassUnderTestIsTrue() {
        classUnderTest.setUpdate(true);
        other.setUpdate(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other U is true and class under test is U true")
    void shouldReturnFalseUpdateWhenOtherIsTrueAndClassUnderTestIsTrue() {
        classUnderTest.setUpdate(true);
        other.setUpdate(true);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return true when other U is true and class under test U is false")
    void shouldReturnTrueUpdateWhenOtherIsTrueAndClassUnderTestIsFalse() {
        classUnderTest.setUpdate(false);
        other.setUpdate(true);
        assertTrue(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when both D is false")
    void shouldReturnFalseDeleteWhenBothAreFalse() {
        classUnderTest.setDelete(false);
        other.setDelete(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other D is false and class under test D is true")
    void shouldReturnFalseDeleteWhenOtherIsFalseAndClassUnderTestIsTrue() {
        classUnderTest.setDelete(true);
        other.setDelete(false);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return false when other D is true and class under test is D true")
    void shouldReturnFalseDeleteWhenOtherIsTrueAndClassUnderTestIsTrue() {
        classUnderTest.setDelete(true);
        other.setDelete(true);
        assertFalse(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return true when other D is true and class under test D is false")
    void shouldReturnTrueDeleteWhenOtherIsTrueAndClassUnderTestIsFalse() {
        classUnderTest.setDelete(false);
        other.setDelete(true);
        assertTrue(classUnderTest.hasLowerAccessThan(other));
    }

    @Test
    @DisplayName("should return true when other has more access on any of CRUD")
    void shouldReturnTrueWhenOtherHasMoreAccessOnAnyCRUD() {
        classUnderTest.setCreate(false);
        classUnderTest.setRead(false);
        classUnderTest.setUpdate(false);
        classUnderTest.setDelete(false);

        other.setCreate(false);
        other.setRead(false);
        other.setUpdate(false);
        other.setDelete(true);

        assertTrue(classUnderTest.hasLowerAccessThan(other));
    }
}
