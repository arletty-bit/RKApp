package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 3-го порядка (Вариант a).
 * Характеризуется коэффициентами:
 * c = [0.5, 1.0]
 * b = [0.25, 0.5, 0.25]
 * a = [0.5, 0.0, 1.0]
 */
public class RK3aMethod extends ButcherTableMethod {
    private static final double[] C = {0.5, 1.0};
    private static final double[] B = {0.25, 0.5, 0.25};
    private static final double[] A = {
        0.5,    // a10
        0.0,    // a20
        1.0     // a21
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public RK3aMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}