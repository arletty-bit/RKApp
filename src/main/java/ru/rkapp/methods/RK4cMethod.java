package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 4-го порядка (Вариант c).
 * Особый метод 4-го порядка с иррациональными коэффициентами.
 * c = [0.5, 0.5, 1.0]
 * b = [1/6, (2-√2)/6, (2+√2)/6, 1/6]
 * a = [0.5, -(0.5-1/√2), (1-1/√2), 0.0, -1/√2, 1+1/√2]
 */
public class RK4cMethod extends ButcherTableMethod {
    private static final double[] C = {0.5, 0.5, 1.0};
    private static final double[] B = {
        1.0/6,
        (2.0 - Math.sqrt(2.0))/6,
        (2.0 + Math.sqrt(2.0))/6,
        1.0/6
    };
    private static final double[] A = {
        0.5,                                // a10
        -(0.5 - 1.0/Math.sqrt(2.0)),        // a20
        (1.0 - 1.0/Math.sqrt(2.0)),        // a21
        0.0,                                // a30
        -1.0/Math.sqrt(2.0),                // a31
        1.0 + 1.0/Math.sqrt(2.0)           // a32
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public RK4cMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}