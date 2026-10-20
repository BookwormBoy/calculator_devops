package com.example.calculator;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculator;

    public CalculatorController(CalculatorService calculator) {
        this.calculator = calculator;
    }

    @GetMapping("/add")
    public Map<String, Object> add(
            @RequestParam double a,
            @RequestParam double b) {
        return response("add", a, b, calculator.add(a, b));
    }

    @GetMapping("/subtract")
    public Map<String, Object> subtract(
            @RequestParam double a,
            @RequestParam double b) {
        return response("subtract", a, b, calculator.subtract(a, b));
    }

    @GetMapping("/multiply")
    public Map<String, Object> multiply(
            @RequestParam double a,
            @RequestParam double b) {
        return response("multiply", a, b, calculator.multiply(a, b));
    }

    @GetMapping("/divide")
    public Map<String, Object> divide(
            @RequestParam double a,
            @RequestParam double b) {
        return response("divide", a, b, calculator.divide(a, b));
    }

    private Map<String, Object> response(
            String operation, double a, double b, double result) {
        return Map.of(
                "operation", operation,
                "a", a,
                "b", b,
                "result", result
        );
    }
}