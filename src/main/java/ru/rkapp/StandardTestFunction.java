package ru.rkapp;

import java.util.function.Function;

/**
 * Стандартные тестовые функции
 */
public enum StandardTestFunction implements TestFunction {
    SIN("sin(x)", "Math.sin(x)",
            x -> Math.sin(x),
            x -> Math.cos(x)),
    COS("cos(x)", "Math.cos(x)",
            x -> Math.cos(x),
            x -> -Math.sin(x)),
    EXP("exp(x)", "Math.exp(x)",
            x -> Math.exp(x),
            x -> Math.exp(x)),
    QUAD("x^2", "x * x",
            x -> x * x,
            x -> 2 * x),
    SIN_COS("sin(x)*cos(10x)", "Math.sin(x) * Math.cos(10 * x)",
            x -> Math.sin(x) * Math.cos(10 * x),
            x -> Math.cos(x) * Math.cos(10 * x) - 10 * Math.sin(x) * Math.sin(10 * x)),
    LOG("log(x)", "Math.log(x)",
            x -> Math.log(x),
            x -> 1.0 / x);

    private final String name;
    private final String expression;
    private final Function<Double, Double> function;
    private final Function<Double, Double> derivative;

    StandardTestFunction(String name, String expression,
                       Function<Double, Double> function,
                       Function<Double, Double> derivative) {
        this.name = name;
        this.expression = expression;
        this.function = function;
        this.derivative = derivative;
    }

    @Override
    public double value(double x) {
        return function.apply(x);
    }

    @Override
    public double derivative(double x) {
        return derivative.apply(x);
    }

    @Override
    public double numericalDerivative(double x) {
        return Differentiation.derivative(function, x);
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return name;
    }
}