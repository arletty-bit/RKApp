package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Реализация классического метода Рунге-Кутты 4-го порядка (RK4).
 * 
 * <p>Метод 4-го порядка точности для решения обыкновенных дифференциальных уравнений.
 * <pre>
 * k1 = f(t, y)
 * k2 = f(t + h/2, y + h/2 * k1)
 * k3 = f(t + h/2, y + h/2 * k2)
 * k4 = f(t + h, y + h * k3)
 * y_{n+1} = y_n + h/6 * (k1 + 2k2 + 2k3 + k4)
 * </pre>
 * 
 * <p>Таблица Бутчера метода:
 * <table border="1">
 *   <tr><th>0</th><td></td><td></td><td></td></tr>
 *   <tr><th>1/2</th><td>1/2</td><td></td><td></td></tr>
 *   <tr><th>1/2</th><td>0</td><td>1/2</td><td></td></tr>
 *   <tr><th>1</th><td>0</td><td>0</td><td>1</td></tr>
 *   <tr><th></th><td>1/6</td><td>1/3</td><td>1/3</td><td>1/6</td></tr>
 * </table>
 */
public class CRK4a extends ButcherTableMethod {
    private static final double[] C = {
        0.5,    // c2
        0.5,    // c3
        1.0     // c4
    };
    
    private static final double[] B = {
        1.0/6,  // Весовой коэффициент b1 (для k1)
        1.0/3,  // Весовой коэффициент b2 (для k2)
        1.0/3,  // Весовой коэффициент b3 (для k3)
        1.0/6   // Весовой коэффициент b4 (для k4)
    };
    
    private static final double[] A = {
        0.5,    // a21 (коэффициент для k2)
        0.0,    // a31 (коэффициент для k3)
        0.5,    // a32 (коэффициент для k3)
        0.0,    // a41 (коэффициент для k4)
        0.0,    // a42 (коэффициент для k4)
        1.0     // a43 (коэффициент для k4)
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public CRK4a(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}