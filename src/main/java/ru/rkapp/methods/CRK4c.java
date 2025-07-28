package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Реализация метода Рунге-Кутты 4-го порядка (Вариант c).
 * 
 * <p>Метод 4-го порядка с иррациональными коэффициентами, известный как
 * "Ralston's Fourth-Order Method". Характеризуется минимизацией ошибки усечения
 * за счет специфического подбора весовых коэффициентов.</p>
 * 
 * Коэффициенты метода:
 * <pre>
 *   c = [0.5, 0.5, 1.0]
 *   b = [1/6, (2-√2)/6, (2+√2)/6, 1/6]
 *   a = [
 *     0.5, 
 *     -(0.5 - 1/√2), 
 *     (1 - 1/√2), 
 *     0.0, 
 *     -1/√2, 
 *     (1 + 1/√2)
 *   ]
 * </pre>
 * 
 * Структура матрицы Бутчера:
 * <pre>
 *   0   |
 *   0.5 | 0.5
 *   0.5 | -(0.5-1/√2)  (1-1/√2)
 *   1.0 | 0.0          -1/√2      (1+1/√2)
 *   ----|-------------------------------
 *       | 1/6  (2-√2)/6  (2+√2)/6  1/6
 * </pre>
 * 
 */
public class CRK4c extends ButcherTableMethod {
    private static final double SQRT2 = Math.sqrt(2.0);
    private static final double INV_SQRT2 = 1.0 / SQRT2;
    
    private static final double[] C = {
        0.5,    // c1
        0.5,    // c2
        1.0     // c3
    };
    
    private static final double[] B = {
        1.0 / 6.0,                 // b1
        (2.0 - SQRT2) / 6.0,       // b2
        (2.0 + SQRT2) / 6.0,       // b3
        1.0 / 6.0                  // b4
    };
    
    private static final double[] A = {
        0.5,                        // a10
        -(0.5 - INV_SQRT2),         // a20
        (1.0 - INV_SQRT2),          // a21
        0.0,                        // a30
        -INV_SQRT2,                 // a31
        1.0 + INV_SQRT2             // a32
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public CRK4c(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}