package ru.rkapp.methods;
import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Кутты-Нюстрема 5-го порядка.
 * Специализированный метод для уравнений второго порядка.
 * c = [1/3, 0.4, 1.0, 2/3, 0.8]
 * b = [23/192, 0.0, 125/192, 0.0, -81/192, 125/192]
 * a = [1/3, 0.16, 0.24, 0.25, -3.0, 3.75, 6/81, 90/81, -50/81, 8/81, 6/75, 36/75, 10/75, 8/75, 0.0]
 */
public class KuttaNystromMethod extends ButcherTableMethod {
    private static final double[] C = {
        1.0/3, 0.4, 1.0, 2.0/3, 0.8
    };
    private static final double[] B = {
        23.0/192, 0.0, 125.0/192, 0.0, -81.0/192, 125.0/192
    };
    private static final double[] A = {
        1.0/3,         // a10
        0.16, 0.24,    // a20, a21
        0.25, -3.0, 3.75, // a30, a31, a32
        6.0/81, 90.0/81, -50.0/81, 8.0/81, // a40-a43
        6.0/75, 36.0/75, 10.0/75, 8.0/75, 0.0 // a50-a54
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public KuttaNystromMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}