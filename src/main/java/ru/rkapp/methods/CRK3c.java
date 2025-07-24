package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 3-го порядка (Вариант c, метод Хойна) - Heun's third-order method.
 * c = [1/3, 2/3]
 * b = [0.25, 0.0, 0.75]
 * a = [1/3, 0.0, 2/3]
 */
public class CRK3c extends ButcherTableMethod {
    private static final double[] C = {
        1.0/3,  // c2
        2.0/3   // c3
    };
    private static final double[] B = {
        0.25,   // b1
        0.0,    // b2
        0.75    // b3
    };
    private static final double[] A = {
        1.0/3,  // a21
        
        0.0,    // a31
        2.0/3   // a32
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public CRK3c(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}