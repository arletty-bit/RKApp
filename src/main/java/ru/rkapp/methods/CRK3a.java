package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Реализация явного метода Рунге-Кутты 3-го порядка (Kutta's 3/8-rule, вариант a).
 * 
 * <p>Характеризуется таблицей Бутчера:</p>
 * <pre>
 *   0   |
 *  0.5  | 0.5
 *   1   | 0.0   1.0
 * ------+-------------
 *       | 0.25  0.5  0.25
 * </pre>
 * 
 * <p>Порядок точности: 3. Метод относится к классу явных схем Рунге-Кутты.
 * Широко известен как "Kutta's third-order method" или "Third-order Heun method".</p>
 * 
 * <p>Математическая форма:
 * k1 = f(t, y)
 * k2 = f(t + 0.5h, y + 0.5hk1)
 * k3 = f(t + h, y + hk2)
 * y_{n+1} = y_n + h(0.25k1 + 0.5k2 + 0.25k3)</p>
 * 
 */
public class CRK3a extends ButcherTableMethod {
    private static final double[] C = {
        0.5,    // c2
        1.0     // c3
    };
    private static final double[] B = {
        0.25,   // b1
        0.5,    // b2
        0.25    // b3
    };
    private static final double[] A = {
        0.5,    // a21 (коэффициент для k2)
        
        0.0,    // a31 (коэффициент для k3)
        1.0     // a32 (коэффициент для k3)
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public CRK3a(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}