package ru.rkapp.methods;
import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Эйлера (1-го порядка).
 * Простейший метод для решения ОДУ.
 * c = []
 * b = [1.0]
 * a = []
 */
public class EulerMethod extends ButcherTableMethod {
    private static final double[] C = {};
    private static final double[] B = {1.0};
    private static final double[] A = {};
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public EulerMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}