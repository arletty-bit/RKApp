package ru.rkapp.methods;

import ru.rkapp.ButcherTableMethod;
import ru.rkapp.RightCalculator;

/**
 * Явный метод Рунге-Кутты 6-го порядка с 7 стадиями (Butcher's RK6(7)).
 * 
 * <p>Характеристики:
 * <ul>
 *   <li>Порядок точности: 6</li>
 *   <li>Количество стадий: 7</li>
 *   <li>Особенность: коэффициенты основаны на золотом сечении (√5)</li>
 *   <li>Оптимален для задач, требующих высокой точности</li>
 * </ul>
 * 
 * <p>Таблица Бутчера:
 * <pre>
 * 0     |
 * 1/2 - √5/10 | 1/2 - √5/10
 * 1/2 + √5/10 | -√5/10       1/2 + √5/5
 * 1/2 - √5/10 | -3/4 + 7√5/20  -1/4 + √5/4   3/2 - 7√5/10
 * 1/2 + √5/10 | (5-√5)/60      0             1/6           (15+7√5)/60
 * 1/2 - √5/10 | (5+√5)/60      0             (9-5√5)/12    1/6          (-5+3√5)/10
 * 1           | 1/6            0             ...           ...           ...          ...
 * ------------+-------------------------------------------------------------
 *             | 1/12           0             0             0             5/12         5/12        1/12
 * </pre>
 * 
 * <p>Оригинальная работа: 
 * Butcher, J. C. (1964). "On Runge-Kutta Processes of High Order". 
 * J. Austral. Math. Soc. 4: 179-194.
 */
public class CRK6x extends ButcherTableMethod {
    private static final double sqrt5 = Math.sqrt(5.0); 
    
    // Коэффициенты узлов (c_i)
    private static final double[] C = {
        0.5 - sqrt5/10,   // c2
        0.5 + sqrt5/10,   // c3
        0.5 - sqrt5/10,   // c4
        0.5 + sqrt5/10,   // c5
        0.5 - sqrt5/10,   // c6
        1.0               // c7
    };
    
    // Весовые коэффициенты (b_i)
    private static final double[] B = {
        1.0/12,          // b1
        0.0,             // b2
        0.0,             // b3
        0.0,             // b4
        5.0/12,          // b5
        5.0/12,          // b6
        1.0/12           // b7
    };
    
    // Коэффициенты матрицы A (a_ij)
    private static final double[] A = {
        // Первая строка (i=1)
        0.5 - sqrt5/10,   // a21
        
        // Вторая строка (i=2)
        -sqrt5/10,        // a31
        0.5 + sqrt5/5,    // a32
        
        // Третья строка (i=3)
        -0.75 + 7.0/20*sqrt5,  // a41
        -0.25 + 0.25*sqrt5,    // a42
        1.5 - 7.0/10*sqrt5,    // a43
        
        // Четвертая строка (i=4)
        (5.0 - sqrt5)/60,       // a51
        0.0,                    // a52
        1.0/6,                  // a53
        (15.0 + 7.0*sqrt5)/60,  // a54
        
        // Пятая строка (i=5)
        (5.0 + sqrt5)/60,       // a61
        0.0,                    // a62
        (9.0 - 5.0*sqrt5)/12,   // a63
        1.0/6,                  // a64
        (-5.0 + 3.0*sqrt5)/10,  // a65
        
        // Шестая строка (i=6)
        1.0/6,                  // a71
        0.0,                    // a72
        (25.0 * sqrt5 - 55.0) / 12,  // a73
        -(25.0 + 7.0*sqrt5)/12, // a74
        5.0 - 2.0*sqrt5,        // a75
        2.5 + sqrt5/2.0         // a76
    };
    
    /**
     * Конструктор метода.
     *
     * @param calculator вычислитель правых частей
     */
    public CRK6x(RightCalculator calculator) {
        super(calculator, C, B, A);
        
        // Валидация структуры таблицы Бутчера
        validateButcherTable();
    }
    
    /**
     * Проверяет корректность таблицы Бутчера.
     * 
     * @throws IllegalStateException если размерность таблицы не соответствует
     *         ожидаемой для метода 6-го порядка с 7 стадиями
     */
    private void validateButcherTable() {
        final int stagess = 7;
        final int expectedLength = stagess * (stagess - 1) / 2;
        
        if (A.length != expectedLength) {
            throw new IllegalStateException(
                "Некорректный размер матрицы A в таблице Бутчера. " +
                "Ожидалось: " + expectedLength + " элементов, " +
                "Фактически: " + A.length + " элементов. " +
                "Проверьте коэффициенты метода."
            );
        }
        
        if (C.length != stagess - 1 || B.length != stagess) {
            throw new IllegalStateException(
                "Некорректная размерность коэффициентов C или B. " +
                "C должен содержать " + (stagess - 1) + " элементов, " +
                "B должен содержать " + stagess + " элементов."
            );
        }
    }
}