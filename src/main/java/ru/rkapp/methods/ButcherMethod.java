package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 6-го порядка (Метод Бутчера).
 * Метод 6-го порядка с коэффициентами на основе золотого сечения.
 */
public class ButcherMethod extends ButcherTableMethod {
    private static final double sqrt5 = Math.sqrt(5.0);
    private static final double[] C = {
        0.5 - sqrt5/10,
        0.5 + sqrt5/10,
        0.5 - sqrt5/10,
        0.5 + sqrt5/10,
        0.5 - sqrt5/10,
        1.0
    };
    private static final double[] B = {
        1.0/12, 0.0, 0.0, 0.0, 5.0/12, 5.0/12, 1.0/12
    };
    private static final double[] A = {
        0.5 - sqrt5/10,
        -sqrt5/10, 0.5 + sqrt5/5,
        -0.75 + 7.0/20*sqrt5,
        -0.25 + 0.25*sqrt5,
        1.5 - 7.0/10*sqrt5,
        (5.0 - sqrt5)/60,
        0.0,
        1.0/6,
        (15.0 + 7.0*sqrt5)/60,
        (5.0 + sqrt5)/60,
        0.0,
        (9.0 - 5.0*sqrt5)/12,
        1.0/6,
        (-5.0 + 3.0*sqrt5)/10,
        1.0/6,
        0.0,
        (25.0 * sqrt5 - 55.0) / 12,
        -(25.0 + 7.0*sqrt5)/12,
        5.0 - 2.0*sqrt5,
        2.5 + sqrt5/2
    };
        
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public ButcherMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}