package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Классический метод Рунге-Кутты 4-го порядка (CRK4a).
 * Наиболее распространенный метод 4-го порядка точности.
 * c = [0.5, 0.5, 1.0]
 * b = [1/6, 1/3, 1/3, 1/6]
 * a = [0.5, 0.0, 0.5, 0.0, 0.0, 1.0]
 */
public class ClassicalRungeKuttaMethod extends ButcherTableMethod {
    private static final double[] C = {0.5, 0.5, 1.0};
    private static final double[] B = {
        1.0/6, 1.0/3, 1.0/3, 1.0/6
    };
    private static final double[] A = {
        0.5,    // a10
        0.0,    // a20
        0.5,    // a21
        0.0,    // a30
        0.0,    // a31
        1.0     // a32
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public ClassicalRungeKuttaMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}