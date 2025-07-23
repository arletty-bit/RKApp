package ru.rkapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Решатель системы ОДУ методами Рунге-Кутты.
 * Осуществляет интегрирование системы на заданное количество шагов.
 */
public class RungeKuttaSolver {
    /**
     * Решает систему ОДУ заданным методом на указанном интервале.
     *
     * @param method  метод интегрирования
     * @param t0      начальное время
     * @param y0      начальные условия
     * @param h       размер шага
     * @param steps   количество шагов
     * @param parm    пользовательские параметры
     * @return список состояний системы на каждом шаге
     */
    public static List<double[]> solve(RungeKuttaMethod method, 
                                     double t0, double[] y0,
                                     double h, int steps, Object parm) {
        
//        if (method instanceof AdaptiveDormandPrince853Integrator) {
//            return solveAdaptive((AdaptiveDormandPrince853Integrator) method,
//                    t0, y0, h, steps, parm);
//        }


        // Список для хранения результатов
        List<double[]> results = new ArrayList<>();
        // Добавление начальных условий
        results.add(y0.clone());
        
        // Текущее состояние системы
        double[] currentY = y0.clone();
        double t = t0;
        
        // Последовательное выполнение шагов интегрирования
        for (int i = 0; i < steps; i++) {
            double[] nextY = new double[y0.length];
            
            // Выполнение одного шага методом
            if (!method.step(t, currentY, h, nextY, parm)) {
                throw new RuntimeException("Ошибка вычисления на шаге " + i);
            }
            
            // Сохранение результата шага
            results.add(nextY);
            // Обновление текущего состояния
            currentY = nextY;
            t += h; // Увеличение времени
        }
        
        return results;
    }
    
//    
//    private static List<double[]> solveAdaptive(AdaptiveDormandPrince853Integrator method,
//                                          double t0, double[] y0,
//                                          double h, int steps, Object parm) {
//    List<double[]> results = new ArrayList<>();
//    results.add(y0.clone());
//    
//    double[] currentY = y0.clone();
//    double t = t0;
//    double currentH = h;
//    
//    for (int i = 0; i < steps; i++) {
//        double[] nextY = new double[y0.length];
//        
//        if (!method.step(t, currentY, currentH, nextY, parm)) {
//            throw new RuntimeException("Ошибка вычисления на шаге " + i);
//        }
//        
//        results.add(nextY);
//        currentY = nextY;
//        t += currentH;
//    }
//    
//    return results;
//}
//    
}