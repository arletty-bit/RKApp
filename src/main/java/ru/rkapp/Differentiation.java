package ru.rkapp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс для численного вычисления производных функций.
 * <p>
 * Предоставляет методы для вычисления:
 * <ul>
 *   <li>Значения производной в заданной точке</li>
 *   <li>Производных для набора точек</li>
 * </ul>
 * Использует метод центральных разностей с адаптивным шагом.
 * 
 * @author arletty
 */
public class Differentiation {
    private static final Logger LOG = LogManager.getLogger(Differentiation.class);

    
    /**
     * Коэффициент по умолчанию для вычисления шага дифференцирования.
     * <p>
     * Значение 1e-8 (0.00000001) обеспечивает баланс между точностью и устойчивостью вычислений.
     * Используется в формуле: {@code h = (|x| + 1) * DEFAULT_H_COEFF}
     */
    private static final double DEFAULT_H_COEFF = 1e-8;

    /**
     * Вычисляет производную функции в заданной точке.
     * <p>
     * Используется формула центральных разностей:
     * <pre>
     * f'(x) ≈ [f(x + h) - f(x - h)] / (2h)
     * </pre>
     * Шаг {@code h} адаптируется в зависимости от значения {@code x}:
     * <pre>
     * h = (|x| + 1) * DEFAULT_H_COEFF
     * </pre>
     * 
     * @param func Функция, для которой вычисляется производная (должна быть определена в окрестности x)
     * @param x    Точка, в которой вычисляется производная
     * @return     Значение производной в точке x
     * 
     * @throws NullPointerException если func == null
     * @throws ArithmeticException  если func возвращает NaN/Infinity или при делении на ноль
     * 
     * @see #DEFAULT_H_COEFF
     * 
     * Пример использования:
    * <pre>{@code 
    *   double result = derivative(x -> x*x, 2.0);
    * }</pre>

    * <pre>{@code 
    *   List<Double> points = List.of(0.0, 1.0, 2.0);
    *   List<Double> derivatives = derivativePoints(x -> x*x, points); 
    * }</pre>
     */
    public static double derivative(Function<Double, Double> func, double x) {
        double h = (Math.abs(x) + 1) * DEFAULT_H_COEFF;
        double f_plus = func.apply(x + h);
        double f_minus = func.apply(x - h);
        return (f_plus - f_minus) / (2 * h);
    }
    
    /**
     * Вычисляет производные функции для набора точек.
     * <p>
     * Для каждой точки из списка {@code xValues} вызывает метод {@link #derivative}.
     * 
     * @param func    Функция, для которой вычисляются производные
     * @param xValues Список точек для вычисления
     * @return        Список значений производных в порядке точек из {@code xValues}
     * 
     * @throws NullPointerException если func == null или xValues == null
     * 
     */
    public static List<Double> derivativePoints(
        Function<Double, Double> func, 
        List<Double> xValues
    ) {
        List<Double> derivatives = new ArrayList<>();
        for (double x : xValues) {
            derivatives.add(derivative(func, x));
        }
        return derivatives;
    }
}