package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 4-го порядка (Правило 3/8).
 * Вариант метода 4-го порядка с коэффициентами:
 * c = [1/3, 2/3, 1.0]
 * b = [1/8, 3/8, 3/8, 1/8]
 * a = [1/3, -1/3, 1.0, 1.0, -1.0, 1.0]
 */
public class RK38RuleMethod extends ButcherTableMethod {
    private static final double[] C = {1.0/3, 2.0/3, 1.0};
    private static final double[] B = {1.0/8, 3.0/8, 3.0/8, 1.0/8};
    private static final double[] A = {
        1.0/3,  // a10
        -1.0/3, // a20
        1.0,    // a21
        1.0,    // a30
        -1.0,   // a31
        1.0     // a32
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public RK38RuleMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}