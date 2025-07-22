/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.rkapp.methods;
import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Метод Рунге-Кутты 6-го порядка (Вариант a).
 * Метод высокого порядка точности с коэффициентами:
 * c = [0.5, 2/3, 1/3, 5/6, 1/6, 1.0]
 * b = [13/200, 0.0, 11/40, 11/40, 4/25, 4/25, 13/200]
 * a = [0.5, 2/9, 4/9, 7/36, 2/9, -1/12, ...]
 */
public class RK6aMethod extends ButcherTableMethod {
    private static final double[] C = {
        0.5, 2.0/3, 1.0/3, 5.0/6, 1.0/6, 1.0
    };
    private static final double[] B = {
        13.0/200, 0.0, 11.0/40, 11.0/40, 4.0/25, 4.0/25, 13.0/200
    };
    private static final double[] A = {
        0.5,                    // a10
        2.0/9, 4.0/9,           // a20, a21
        7.0/36, 2.0/9, -1.0/12, // a30-a32
        -35.0/144, -55.0/36, 35.0/48, 15.0/8, // a40-a43
        -1.0/360, -11.0/36, -1.0/8, 0.5, 0.1, // a50-a54
        -41.0/260, 22.0/13, 43.0/156, -118.0/39, 32.0/195, 80.0/39 // a60-a65
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public RK6aMethod(RightCalculator calculator) {
        super(calculator, C, B, A);
    }
}