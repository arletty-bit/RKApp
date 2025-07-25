package ru.rkapp;

import java.util.function.Function;

/**
 * Интерфейс тестовых функций для верификации методов Рунге-Кутты
 */
public interface TestFunction {
    double value(double x);
    double derivative(double x);
    double numericalDerivative(double x);
    String getExpression();
}