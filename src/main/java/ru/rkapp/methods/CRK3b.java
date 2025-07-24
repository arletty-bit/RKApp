package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Реализация метода Кутты 3-го порядка (Kutta's third-order method).
 * 
 * <p>Характеристики метода:
 * <ul>
 *   <li>Порядок точности: 3</li>
 *   <li>Количество стадий: 3</li>
 *   <li>Таблица Бутчера:
 *     <pre>
 *       0  |
 *      1/2 | 1/2
 *       1  | -1   2
 *       ---------------
 *           | 1/6 2/3 1/6
 *     </pre>
 *   </li>
 * </ul>
 * 
 * <pre>
 * k1 = f(t_n, y_n)
 * k2 = f(t_n + h/2, y_n + (h/2)*k1)
 * k3 = f(t_n + h, y_n + h*(-k1 + 2*k2))
 * y_{n+1} = y_n + h*(k1/6 + 2*k2/3 + k3/6)
 * </pre>
 * 
 * <p>Метод обеспечивает третий порядок точности и является явной схемой Рунге-Кутты.
 * 
 */
public class CRK3b extends ButcherTableMethod {
    private static final double[] C = {
        0.5,    // c2
        1.0     // c3
    };
    private static final double[] B = {
        1.0/6,  // b1
        2.0/3,  // b2
        1.0/6   // b3
    };
    private static final double[] A = {
        0.5,    // a21
        
        -1.0,   // a31
        2.0     // a32
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public CRK3b(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}