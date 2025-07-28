package ru.rkapp;

/**
 * Абстрактный базовый класс для всех методов Рунге-Кутты.
 * Определяет общую структуру и интерфейс для численного решения систем ОДУ.
 */
public abstract class RungeKuttaMethod {
    protected RightCalculator rightCalculator;  // Вычислитель правых частей системы ОДУ
    
    /**
     * Конструктор метода Рунге-Кутты.
     *
     * @param calculator вычислитель правых частей системы ОДУ
     */
    public RungeKuttaMethod(RightCalculator calculator) {
        this.rightCalculator = calculator;
    }
    
    /**
     * Выполняет один шаг интегрирования методом Рунге-Кутты.
     *
     * @param t   начальное время шага
     * @param Y   массив начальных значений переменных состояния
     * @param h   размер шага интегрирования
     * @param YN  массив для записи новых значений переменных после шага
     * @param parm пользовательские параметры для вычислений
     * @return true если шаг выполнен успешно, false при ошибке
     */    
    public abstract boolean step(double t, double[] Y, double h, double[] YN, Object parm);

    /**
     * Интерполирует решение в заданный момент времени (опциональная функция)
     * 
     * @param t     Время для интерполяции
     * @param y     Массив для записи результата
     * @return      true если интерполяция поддерживается и выполнена, иначе false
     */
    public boolean interpolate(double t, double[] y) {
        return false; // По умолчанию не поддерживается
    }

        public boolean supportsInterpolation() {
        return false;
    }
        
            public abstract void initialize();
}


