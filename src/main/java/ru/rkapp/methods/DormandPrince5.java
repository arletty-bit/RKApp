package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Дорманда-Принса 5-го порядка (DoPri5).
 * Встроенная пара 5(4) порядка для адаптивных методов.
 * c = [1/5, 3/10, 4/5, 8/9, 1.0, 1.0]
 * b = [5179/57600, 0.0, 7571/16695, 393/640, -92097/339200, 187/2100, 1/40]
 * a = [1/5, 3/40, 9/40, 44/45, -56/15, 32/9, ...]
 */
public class DormandPrince5 extends ButcherTableMethod {
    private static final double[] C = {
        1.0/5, 3.0/10, 4.0/5, 8.0/9, 1.0, 1.0
    };
    private static final double[] B = {
        5179.0/57600, 0.0, 7571.0/16695, 
        393.0/640, -92097.0/339200, 187.0/2100, 1.0/40
    };
    private static final double[] A = {
        1.0/5,                     // a10
        3.0/40, 9.0/40,            // a20, a21
        44.0/45, -56.0/15, 32.0/9, // a30-a32
        19372.0/6561, -25360.0/2187, 64448.0/6561, -212.0/729, // a40-a43
        9017.0/3168, -355.0/33, 46732.0/5247, 49.0/176, -5103.0/18656, // a50-a54
        35.0/384, 0.0, 500.0/1113, 125.0/192, -2187.0/6784, 11.0/84 // a60-a65
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public DormandPrince5(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}