package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 3-го порядка (Вариант c, метод Хойна).
 * Характеризуется коэффициентами:
 * c = [1/3, 2/3]
 * b = [0.25, 0.0, 0.75]
 * a = [1/3, 0.0, 2/3]
 */
public class RK3cMethod extends ButcherTableMethod {
    private static final double[] C = {1.0/3, 2.0/3};
    private static final double[] B = {0.25, 0.0, 0.75};
    private static final double[] A = {
        1.0/3,  // a10
        0.0,    // a20
        2.0/3   // a21
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public RK3cMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}