package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Явный метод Трапеции (Хойна) (2-го порядка) - Heun's method (Explicit trapezoidal rule).
 * Явный метод второго порядка точности.
 * Таблица Бутчера:
 *   c = [0, 1]
 *   a = [0, 0]
 *         [1, 0]
 *   b = [0.5, 0.5]
 *
 * Фактически коэффициенты заданы для стадий:
 *   C = [1.0]   (c2)
 *   A = [1.0]   (a21)
 *   B = [0.5, 0.5] (b1, b2)
 *
 * Алгоритм:
 *   k1 = f(t_n, y_n)
 *   k2 = f(t_n + h, y_n + h*k1)
 *   y_{n+1} = y_n + (h/2)*(k1 + k2)
 */
public class T2 extends ButcherTableMethod {
    private static final double[] C = {
        1.0     // c2
    };
    private static final double[] B = {
        0.5,    // b1
        0.5     // b2
    };
    private static final double[] A = {
        1.0     // a21
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public T2(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}