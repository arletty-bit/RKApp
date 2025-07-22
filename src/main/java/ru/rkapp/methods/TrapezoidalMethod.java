package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Трапеций (2-го порядка).
 * Неявный метод второго порядка точности.
 * c = [1.0]
 * b = [0.5, 0.5]
 * a = [1.0]
 */
public class TrapezoidalMethod extends ButcherTableMethod {
    private static final double[] C = {1.0};
    private static final double[] B = {0.5, 0.5};
    private static final double[] A = {1.0};
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public TrapezoidalMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}
