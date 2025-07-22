package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 3-го порядка (Вариант b).
 * Классический метод 3-го порядка с коэффициентами:
 * c = [0.5, 1.0]
 * b = [1/6, 2/3, 1/6]
 * a = [0.5, -1.0, 2.0]
 */
public class RK3bMethod extends ButcherTableMethod {
    private static final double[] C = {0.5, 1.0};
    private static final double[] B = {1.0/6, 2.0/3, 1.0/6};
    private static final double[] A = {
        0.5,    // a10
        -1.0,   // a20
        2.0     // a21
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public RK3bMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}