package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 4-го порядка (Правило 3/8 Кутта) - Kutta's 3/8 rule.
 * <p>
 * Один из двух классических методов 4-го порядка. Отличается от классического метода 
 * (CRK4a) коэффициентами таблицы Бутчера. Имеет меньшую ошибку округления, но требует
 * больше вычислений (4 вычисления правых частей на шаг).
 * <p>
 * Таблица Бутчера:
 * <pre>
 *   0   |
 *  1/3  | 1/3
 *  2/3  | -1/3  1
 *   1   |  1    -1    1
 * ------+-----------------
 *       | 1/8  3/8  3/8  1/8
 * </pre>
 * Формулы:
 * <pre>
 * k1 = f(t, X)
 * k2 = f(t + h/3, X + h/3*k1)
 * k3 = f(t + 2h/3, X + h*(-1/3*k1 + k2))
 * k4 = f(t + h, X + h*(k1 - k2 + k3))
 * X_{n+1} = X_n + h*(k1 + 3k2 + 3k3 + k4)/8
 * </pre>
 * 
 */
public class CRK4b extends ButcherTableMethod {
    private static final double[] C = {
        1.0/3,  // c2
        2.0/3,  // c3
        1.0     // c4
    };
    private static final double[] B = {
        1.0/8,  // b1
        3.0/8,  // b2
        3.0/8,  // b3
        1.0/8   // b4
    };
    private static final double[] A = {
        1.0/3,  // a21
        
        -1.0/3, // a31
        1.0,    // a32
        
        1.0,    // a41
        -1.0,   // a42
        1.0     // a43
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public CRK4b(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}