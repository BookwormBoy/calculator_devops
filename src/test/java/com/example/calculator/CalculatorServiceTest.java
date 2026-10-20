package com.example.calculator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private final CalculatorService calculator = new CalculatorService();

    @Test
    void testAdd() {
        assertEquals(5.0, calculator.add(2, 3));
    }

    @Test
    void testSubtract() {
        assertEquals(2.0, calculator.subtract(5, 3));
    }

    @Test
    void testMultiply() {
        assertEquals(12.0, calculator.multiply(4, 3));
    }

    @Test
    void testDivide() {
        assertEquals(5.0, calculator.divide(10, 2));
    }

    @Test
    void testDivideByZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.divide(10, 0)
        );
    }
}