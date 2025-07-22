package ru.rkapp;

/**
 * Абстрактный класс для методов Рунге-Кутты, использующих таблицу Бутчера.
 * Реализует общую логику шага интегрирования на основе коэффициентов таблицы.
 */

public abstract class ButcherTableMethod extends RungeKuttaMethod {
    /**
     * Коэффициенты c из таблицы Бутчера
     */
    protected final double[] c;
    /**
     * Коэффициенты b из таблицы Бутчера
     */
    protected final double[] b;
    /*
      Коэффициенты a из таблицы Бутчера
    */
    protected final double[] a;
    /*
     Количество стадий метода
    */
    protected final int stages;
    
    /**
     * Конструктор метода с таблицей Бутчера.
     *
     * @param calculator вычислитель правых частей
     * @param c          массив коэффициентов c
     * @param b          массив коэффициентов b
     * @param a          массив коэффициентов a
     */
    public ButcherTableMethod(RightCalculator calculator, 
                             double[] c, double[] b, double[] a) {
        super(calculator);
        this.c = c;
        this.b = b;
        this.a = a;
        this.stages = b.length;
    }

    /**
     * Выполняет один шаг интегрирования по методу Рунге-Кутты.
     * 
     * @param t   начальное время шага
     * @param Y   начальные значения переменных
     * @param h   размер шага
     * @param YN  массив для записи результатов
     * @param parm пользовательские параметры
     * @return true при успешном выполнении, false при ошибке
     */
    @Override
    public boolean step(double t, double[] Y, double h, double[] YN, Object parm) {
        final int n = Y.length; // Количество уравнений в системе
        final double[][] k = new double[stages][n]; // Матрица коэффициентов k для каждой стадии
        final double[] temp = new double[n]; // Временный массив для промежуточных вычислений
        
         // Вычисление k0 (первая стадия)
        if (!rightCalculator.compute(t, Y, k[0], parm)) {
            return false; // Ошибка при вычислении правых частей
        }
        
        // Вычисление k1..k_{s-1}
        for (int i = 1; i < stages; i++) {
            // Копирование начальных значений Y во временный массив
            System.arraycopy(Y, 0, temp, 0, n);
            // Расчет индекса для доступа к коэффициентам a
            int aIndex = i * (i - 1) / 2;
            // Суммирование влияния предыдущих стадий
            for (int j = 0; j < i; j++) {
                final double aVal = a[aIndex + j];
                if (aVal != 0) {
                    for (int m = 0; m < n; m++) {
                        // Обновление временного массива
                        temp[m] += h * aVal * k[j][m];
                    }
                }
            }
////            // Вычисление времени для текущей стадии
////            double stageTime = t;
////            if (i > 1) {
////                stageTime += c[i-2] * h;
////            } else if (i == 1 && c.length > 0) {
////                stageTime += c[0] * h;
////            }
////            // Вычисление правых частей для текущей стадии
////            if (!rightCalculator.compute(stageTime, temp, k[i], parm)) {
////                return false; // Ошибка при вычислении
////            }
        double stageTime = t;
        if (i - 1 < c.length) {
            stageTime += c[i - 1] * h;  // Используем c[i-1] вместо c[i-2]
        }
        
        if (!rightCalculator.compute(stageTime, temp, k[i], parm)) {
            return false;
        }

        }
        // Вычисление нового значения Y
        for (int i = 0; i < n; i++) {
            double sum = 0;
            // Суммирование вклада всех стадий
            for (int j = 0; j < stages; j++) {
                sum += b[j] * k[j][i];
            }
            // Обновление значения переменной
            YN[i] = Y[i] + h * sum;
        }
        
        return true; // Успешное завершение шага
    }
}