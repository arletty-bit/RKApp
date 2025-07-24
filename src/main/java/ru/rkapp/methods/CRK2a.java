package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Реализация явного метода Рунге-Кутты 2-го порядка, известного как 
 * "Метод средней точки" (Midpoint method).
 * <p>
 * Таблица Бутчера метода:
 * <pre>
 *   0  |
 *  1/2 | 1/2
 * ------+---------
 *       | 0   1
 * </pre>
 * 
 * <ul>
 *   <li>c = [0.5]</li>
 *   <li>b = [0.0, 1.0]</li>
 *   <li>a = [0.5]</li>
 * </ul>
 * 
 * <ol>
 *   <li>k1 = f(t, y)</li>
 *   <li>k2 = f(t + h/2, y + (h/2)*k1)</li>
 *   <li>y(t + h) = y + h*k2</li>
 * </ol>
 * 
 */
public class CRK2a extends ButcherTableMethod {
    private static final double[] C = {
        0.5     // c2
    };
    private static final double[] B = {
        0.0,    // b1
        1.0     // b2
    };
    private static final double[] A = {
        0.5     // a21
    };
    
    /**
     * Конструктор метода средней точки.
     *
     * @param calculator вычислитель правых частей системы ОДУ.
     */
    public CRK2a(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}