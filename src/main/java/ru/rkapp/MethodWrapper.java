package ru.rkapp;

/**
 * Обертка для методов Рунге-Кутты, обеспечивающая удобное создание
 * экземпляров и отображение в пользовательском интерфейсе.
 */
public class MethodWrapper {
    /**
     * Название метода для отображения в интерфейсе.
     */
    private final String name;
    
    /**
     * Фабричная функция для создания экземпляра метода.
     */
    private final java.util.function.Function<RightCalculator, RungeKuttaMethod> factory;
    
    /**
     * Поддерживает ли метод интерполяцию
     */
    private final boolean supportsInterpolation;

    /**
     * Конструктор обертки метода.
     *
     * @param name название метода
     * @param factory фабричная функция для создания экземпляра метода
     * @param supportsInterpolation поддерживает ли метод интерполяцию
     */
    public MethodWrapper(String name,
                         java.util.function.Function<RightCalculator, RungeKuttaMethod> factory,
                         boolean supportsInterpolation) {
        this.name = name;
        this.factory = factory;
        this.supportsInterpolation = supportsInterpolation;
    }

    /**
     * Создает экземпляр метода Рунге-Кутты.
     *
     * @param calculator вычислитель правых частей ОДУ
     * @return экземпляр метода
     */
    public RungeKuttaMethod createMethod(RightCalculator calculator) {
        return factory.apply(calculator);
    }

    /**
     * Возвращает название метода.
     *
     * @return строковое представление метода
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Поддерживает ли метод интерполяцию
     * @return true если поддерживает
     */
    public boolean supportsInterpolation() {
        return supportsInterpolation;
    }
}