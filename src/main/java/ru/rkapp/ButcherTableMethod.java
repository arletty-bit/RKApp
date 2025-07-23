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
      final int n = Y.length;
        final double[][] k = new double[stages][n];
        final double[] temp = new double[n];
        
        // Стадия 0 (k0)
        if (!rightCalculator.compute(t, Y, k[0], parm)) {
            return false;
        }
        
        for (int i = 0; i < stages - 1; i++) {
            System.arraycopy(Y, 0, temp, 0, n);
            
            int aIndex = (i + 1) * i / 2;
            
            for (int j = 0; j <= i; j++) {
                final double aVal = a[aIndex + j];
                if (aVal != 0.0) {  // Оптимизация для нулевых коэффициентов
                    for (int m = 0; m < n; m++) {
                        temp[m] += h * aVal * k[j][m];
                    }
                }
            }
            
            double stageTime = t + c[i] * h;
            
            if (!rightCalculator.compute(stageTime, temp, k[i + 1], parm)) {
                return false;
            }
        }

        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < stages; j++) {
                sum += b[j] * k[j][i];
            }
            YN[i] = Y[i] + h * sum;
        }
        
        return true;
    }
}