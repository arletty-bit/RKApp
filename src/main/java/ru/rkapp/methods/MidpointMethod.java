package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Средней Точки (CRK2a).
 * Явный метод Рунге-Кутты 2-го порядка.
 * c = [0.5]
 * b = [0.0, 1.0]
 * a = [0.5]
 */
public class MidpointMethod extends ButcherTableMethod {
    private static final double[] C = {0.5};
    private static final double[] B = {0.0, 1.0};
    private static final double[] A = {0.5};
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public MidpointMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}