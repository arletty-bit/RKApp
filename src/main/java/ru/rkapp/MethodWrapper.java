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
     * Конструктор обертки метода.
     *
     * @param name название метода
     * @param factory фабричная функция для создания экземпляра метода
     */
    public MethodWrapper(String name,
                         java.util.function.Function<RightCalculator, RungeKuttaMethod> factory) {
        this.name = name;
        this.factory = factory;
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
}