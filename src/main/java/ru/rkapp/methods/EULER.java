package ru.rkapp.methods;
import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Явный метод Эйлера (Explicit Euler Method) - метод первого порядка.
 * 
 * <p>Формула интегрирования:
 * <pre>y<sub>n+1</sub> = y<sub>n</sub> + h · f(t<sub>n</sub>, y<sub>n</sub>)</pre>
 * 
 * <p>Параметры таблицы Бутчера:
 * <ul>
 *   <li>c = []</li>
 *   <li>b = [1.0]</li>
 *   <li>a = []</li>
 * </ul>
 * 
 * <p>Характеристики:
 * <ul>
 *   <li>Порядок: 1</li>
 *   <li>Стадийность: 1 (одно вычисление правой части на шаг)</li>
 *   <li>Устойчивость: условная (требует малого шага)</li>
 * </ul>
 * 
 */
public class EULER extends ButcherTableMethod {
    private static final double[] C = {};
    private static final double[] B = {1.0};
    private static final double[] A = {};
    
    /**
     * Конструктор метода Эйлера.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public EULER(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}