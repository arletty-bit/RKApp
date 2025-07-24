package ru.rkapp;

import java.util.function.Function;

/**
 * Перечисление тестовых функций для верификации методов Рунге-Кутты. Каждая
 * функция содержит:
 * <ul>
 * <li>Математическое выражение</li>
 * <li>Функцию вычисления значения</li>
 * <li>Функцию вычисления производной</li>
 * </ul>
 */
/**
 * Перечисление тестовых функций для верификации методов Рунге-Кутты. Каждая
 * функция содержит:
 * <ul>
 * <li>Математическое выражение</li>
 * <li>Функцию вычисления значения</li>
 * <li>Функцию вычисления производной</li>
 * </ul>
 */
public enum TestFunction {
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
    ;

    private final String name;          // Название функции
    private final Function<Double, Double> function;   // Функция вычисления значения
    private final Function<Double, Double> derivative; // Функция вычисления производной
    private final String expression;

    /**
     * Конструктор тестовой функции.
     *
     * @param name название функции
     * @param function лямбда-выражение для вычисления значения
     * @param derivative лямбда-выражение для вычисления производной
     */
    TestFunction(String name, String expression,
            Function<Double, Double> function,
            Function<Double, Double> derivative) {
        this.name = name;
        this.expression = expression;

        this.function = function;
        this.derivative = derivative;
    }

    /**
     * Вычисляет значение функции в точке x.
     *
     * @param x аргумент функции
     * @return значение функции
     */
    public double value(double x) {
        return function.apply(x);
    }

    /**
     * Вычисляет значение производной функции в точке x.
     *
     * @param x аргумент функции
     * @return значение производной
     */
    public double derivative(double x) {
        return derivative.apply(x);
    }

    // Численная производная (через центральную разностную схему)
    public double numericalDerivative(double x) {
        return Differentiation.derivative(function, x);
    }

    public String getExpression() {
        return expression;
    }

    /**
     * Возвращает строковое представление функции.
     *
     * @return название функции
     */
    @Override
    public String toString() {
        return name;
    }

}
