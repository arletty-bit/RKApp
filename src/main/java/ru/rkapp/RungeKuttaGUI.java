package ru.rkapp;

import ru.rkapp.*;
import ru.rkapp.methods.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Главное окно приложения для решения обыкновенных дифференциальных уравнений
 * с использованием методов Рунге-Кутты с визуализацией результатов.
 * 
 * <p>Окно содержит:
 * <ul>
 *   <li>Поля ввода параметров вычислений</li>
 *   <li>Выбор тестовой функции</li>
 *   <li>Выбор численного метода</li>
 *   <li>Область вывода результатов в табличном виде</li>
 *   <li>Панель визуализации графиков решения</li>
 * </ul>
 */
public class RungeKuttaGUI extends JFrame {
    
    /**
     * Перечисление тестовых функций для верификации методов Рунге-Кутты.
     * Каждая функция содержит:
     * <ul>
     *   <li>Математическое выражение</li>
     *   <li>Функцию вычисления значения</li>
     *   <li>Функцию вычисления производной</li>
     * </ul>
     */
    public enum TestFunction {
        SIN("sin(x)", 
            x -> Math.sin(x),         // Функция синуса
            x -> Math.cos(x)),        // Производная синуса - косинус
        
        COS("cos(x)", 
            x -> Math.cos(x),         // Функция косинуса
            x -> -Math.sin(x)),       // Производная косинуса - минус синус
        
        EXP("exp(x)", 
            x -> Math.exp(x),         // Экспоненциальная функция
            x -> Math.exp(x)),        // Производная экспоненты - сама экспонента
        
        QUAD("x^2", 
            x -> x * x,               // Квадратичная функция
            x -> 2 * x);              // Производная квадратичной функции - линейная
        
        private final String name;          // Название функции
        private final Function<Double, Double> function;   // Функция вычисления значения
        private final Function<Double, Double> derivative; // Функция вычисления производной
        
        /**
         * Конструктор тестовой функции.
         * 
         * @param name      название функции
         * @param function  лямбда-выражение для вычисления значения
         * @param derivative лямбда-выражение для вычисления производной
         */
        TestFunction(String name, 
                     Function<Double, Double> function,
                     Function<Double, Double> derivative) {
            this.name = name;
            this.function = function;
            this.derivative = derivative;
        }
        
        /**
         * Вычисляет значение функции в точке x.
         * 
         * @param x аргумент функции
         * @return значение функции
         */
        public double value(double x) {
            return function.apply(x);
        }
        
        /**
         * Вычисляет значение производной функции в точке x.
         * 
         * @param x аргумент функции
         * @return значение производной
         */
        public double derivative(double x) {
            return derivative.apply(x);
        }
        
        /**
         * Возвращает строковое представление функции.
         * 
         * @return название функции
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Обертка для методов Рунге-Кутты, обеспечивающая удобное создание экземпляров
     * и отображение в пользовательском интерфейсе.
     */
    private static class MethodWrapper {
        private final String name;  // Название метода
        private final java.util.function.Function<RightCalculator, RungeKuttaMethod> factory; // Фабрика для создания метода
        
        /**
         * Конструктор обертки метода.
         * 
         * @param name   название метода
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

    // Элементы пользовательского интерфейса
    private final JComboBox<TestFunction> functionComboBox; // Выбор функции
    private final JComboBox<MethodWrapper> methodComboBox;  // Выбор метода
    private final JTextField x0Field;      // Поле ввода начального X
    private final JTextField y0Field;      // Поле ввода начального Y
    private final JTextField minXField;    // Поле ввода минимального X
    private final JTextField maxXField;    // Поле ввода максимального X
    private final JTextField stepsField;   // Поле ввода количества шагов
    private final JTextArea resultArea;    // Область вывода результатов
    private final JLabel derivativeLabel;  // Метка для отображения производной
    private GraphPanel graphPanel;         // Панель для рисования графиков
    private JTabbedPane tabbedPane;        // Панель с вкладками

    /**
     * Конструктор главного окна приложения.
     * Инициализирует компоненты пользовательского интерфейса и настраивает окно.
     */
    public RungeKuttaGUI() {
        // Настройка основного окна
        setTitle("Методы Рунге-Кутты");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Создание панели ввода параметров
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Добавление выбора функции
        inputPanel.add(new JLabel("Функция:"));
        functionComboBox = new JComboBox<>(TestFunction.values());
        inputPanel.add(functionComboBox);
        
        // Добавление выбора метода
        inputPanel.add(new JLabel("Метод:"));
        methodComboBox = new JComboBox<>();
        inputPanel.add(methodComboBox);

        // Заполнение списка методов Рунге-Кутты
        methodComboBox.addItem(new MethodWrapper("Метод Эйлера (1:1)", EULER::new));
        methodComboBox.addItem(new MethodWrapper("Метод Трапеций (2:2)", T2::new));

        methodComboBox.addItem(new MethodWrapper("Метод Средней Точки (2:2)", CRK2a::new));
        methodComboBox.addItem(new MethodWrapper("CRK3a (3:3)", CRK3a::new));
        methodComboBox.addItem(new MethodWrapper("CRK3b (3:3)", CRK3b::new));
        methodComboBox.addItem(new MethodWrapper("CRK3c Метод Хойна (3:3)", CRK3c::new));
        methodComboBox.addItem(new MethodWrapper("Классический метод Рунге-Кутта (4:4)", CRK4a::new));

        methodComboBox.addItem(new MethodWrapper("Правило 3/8 Кутта (4:4)", CRK4b::new));
        methodComboBox.addItem(new MethodWrapper("CRK4c (4:4)", CRK4c::new));
        methodComboBox.addItem(new MethodWrapper("CRK5a Метод Кутта-Нюстрема (6:5)", CRK5a::new));
        
        
        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (7:4)", calc -> new DOPRI5(calc, 4)));

        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (7:5)", calc -> new DOPRI5(calc, 5)));

        methodComboBox.addItem(new MethodWrapper("CRK6a (7:6)", CRK6a::new));
        methodComboBox.addItem(new MethodWrapper("Метод Бутчера (7:6)", CRK6x::new));


        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (13:7)", calc -> new DOPRI8(calc, 7)));

        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (13:8)", calc -> new DOPRI8(calc, 8)));

        
        methodComboBox.addItem(new MethodWrapper("DormandPrince853Integrator", DormandPrince853Integrator::new));
//        methodComboBox.addItem(new MethodWrapper("AdaptiveDormandPrince853Integrator",
//                calc -> new AdaptiveDormandPrince853Integrator(
//                        calc,
//                        1e-8, // minStep
//                        0.1, // maxStep
//                        1e-8, // absTol
//                        1e-8 // relTol
//                )
//        ));
//        AdaptiveDormandPrince853Integrator integrator = 
//            new AdaptiveDormandPrince853Integrator(minStep, maxStep, absTol, relTol);
//                methodComboBox.addItem(new MethodWrapper("AdaptiveDormandPrince853Integrator", AdaptiveDormandPrince853Integrator::new));

        methodComboBox.addItem(new MethodWrapper("Эверхарт (-:[2,32])", 
            calc -> new Everhart(calc, 15, 1)));
        
        // Добавление полей ввода начальных условий
        inputPanel.add(new JLabel("Начальное x:"));
        x0Field = new JTextField("0.0");
        inputPanel.add(x0Field);
        
        inputPanel.add(new JLabel("Начальное y:"));
        y0Field = new JTextField("0.0");
        inputPanel.add(y0Field);
        
        // Добавление полей ввода диапазона
        inputPanel.add(new JLabel("Min X:"));
        minXField = new JTextField("0.0");
        inputPanel.add(minXField);
        
        inputPanel.add(new JLabel("Max X:"));
        maxXField = new JTextField("6.28318530717959"); // 2π
        inputPanel.add(maxXField);
        
        // Добавление поля ввода количества шагов
        inputPanel.add(new JLabel("Шаги:"));
        stepsField = new JTextField("180");
        inputPanel.add(stepsField);

        // Размещение панели ввода в верхней части окна
        add(inputPanel, BorderLayout.NORTH);

        // Создание панели результатов
        JPanel resultPanel = new JPanel(new BorderLayout());
        
        // Метка для отображения производной
        derivativeLabel = new JLabel("Производная в x0: ");
        resultPanel.add(derivativeLabel, BorderLayout.NORTH);

        // Создание панели с вкладками
        tabbedPane = new JTabbedPane();

        // Вкладка с текстовыми результатами
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Моноширинный шрифт для выравнивания
        resultArea.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(resultArea);

        // Вкладка с графиком
        graphPanel = new GraphPanel();
        JScrollPane graphScrollPane = new JScrollPane(graphPanel);

        // Добавление вкладок
        tabbedPane.addTab("График", graphScrollPane);
        tabbedPane.addTab("Результаты", textScrollPane);

        // Размещение панели вкладок в центре окна
        add(tabbedPane, BorderLayout.CENTER);

        // Создание кнопки вычисления
        JButton calculateButton = new JButton("Вычислить");
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        calculateButton.addActionListener(this::calculateAction);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(calculateButton);
        add(buttonPanel, BorderLayout.SOUTH); // Размещение в нижней части
        
        // Настройка обработчиков событий
        functionComboBox.addActionListener(e -> updateDerivative());
        x0Field.addActionListener(e -> updateDerivative());
        
        // Первоначальное обновление производной
        updateDerivative();
    }
    
    /**
     * Обновляет значение производной при изменении функции или начальной точки.
     */
    private void updateDerivative() {
        // Получение выбранной функции
        TestFunction function = (TestFunction) functionComboBox.getSelectedItem();
        try {
            // Парсинг начального значения x
            double x0 = Double.parseDouble(x0Field.getText());
            // Вычисление производной
            double derivative = function.derivative(x0);
            // Обновление текста метки
            derivativeLabel.setText("Производная в x0: " + String.format("%.6f", derivative));
        } catch (NumberFormatException e) {
            // Обработка ошибки неверного формата числа
            derivativeLabel.setText("Производная в x0: ошибка ввода");
        }
    }
    
    /**
     * Обработчик события нажатия кнопки "Вычислить".
     * Выполняет решение ОДУ выбранным методом и отображает результаты.
     * 
     * @param e событие действия
     */
    private void calculateAction(ActionEvent e) {
        try {
            // Получение выбранной функции
            TestFunction function = (TestFunction) functionComboBox.getSelectedItem();
            // Получение обертки выбранного метода
            MethodWrapper methodWrapper = (MethodWrapper) methodComboBox.getSelectedItem();
            
            // Парсинг параметров из полей ввода
            double x0 = Double.parseDouble(x0Field.getText());
            //double y0 = Double.parseDouble(y0Field.getText());
            double y0 = function.value(x0);
            double minX = Double.parseDouble(minXField.getText());
            double maxX = Double.parseDouble(maxXField.getText());
            int steps = Integer.parseInt(stepsField.getText());
            
            // Проверка корректности количества шагов
            if (steps <= 0) {
                throw new IllegalArgumentException("Количество шагов должно быть положительным");
            }
            
            // Проверка корректности диапазона
            if (maxX <= minX) {
                throw new IllegalArgumentException("Max X должен быть больше Min X");
            }
            
            // Расчет шага интегрирования
            double h = (maxX - minX) / steps;
            
            // Создание вычислителя правых частей ОДУ
            // dy/dx = f(t), где f(t) - производная выбранной функции
            RightCalculator calculator = (t, y, f, parm) -> {
                f[0] = function.derivative(t);
                return true;
            };
            
            // Создание экземпляра метода Рунге-Кутты
            RungeKuttaMethod method = methodWrapper.createMethod(calculator);
            
            // Подготовка начальных условий
            double[] y0Arr = {y0};
            
            // Решение ОДУ
            List<double[]> solution = RungeKuttaSolver.solve(
                method, minX, y0Arr, h, steps, null
            );
            
            // Подготовка данных для визуализации
            List<Double> xValues = new ArrayList<>();
            List<Double> yValues = new ArrayList<>();
            List<Double> exactValues = new ArrayList<>();
            
            // Заполнение списков координат
            double currentX = minX;
            for (int i = 0; i < solution.size(); i++) {
                xValues.add(currentX);
                yValues.add(solution.get(i)[0]); // Численное решение
                exactValues.add(function.value(currentX)); // Точное решение
                // Увеличение x для следующей точки (кроме последней итерации)
                currentX += (i < solution.size() - 1) ? h : 0;
            }
            
            // Обновление графика
            graphPanel.setData(xValues, yValues, exactValues);
            
            // Формирование текстового отчета
            StringBuilder sb = new StringBuilder();
            sb.append("Метод: ").append(methodWrapper.toString()).append("\n");
            sb.append("Функция: ").append(function.toString()).append("\n");
            sb.append("Параметры: minX = ").append(minX)
              .append(", maxX = ").append(maxX)
              .append(", y0 = ").append(y0)
              .append(", шагов = ").append(steps)
              .append(", h = ").append(String.format("%.6f", h)).append("\n\n");
            
            // Заголовок таблицы результатов
            sb.append("Результаты:\n");
            sb.append(String.format("%-10s %-15s %-15s %-15s\n", 
                "x", "Численное", "Точное", "Ошибка"));
            
            // Вычисление статистики ошибок
            double maxError = 0;
            double sumError = 0;
            
            // Заполнение таблицы результатов
            for (int i = 0; i < solution.size(); i++) {
                double x = minX + i * h;
                double numY = solution.get(i)[0];
                double exactY = function.value(x);
                double error = Math.abs(numY - exactY);
                
                // Обновление статистики
                sumError += error;
                if (error > maxError) maxError = error;
                
                // Добавление строки в таблицу
                sb.append(String.format("%-10.4f %-15.8f %-15.8f %-15.8e\n",
                    x, numY, exactY, error));
            }
            
            // Вычисление средней ошибки
            double avgError = sumError / solution.size();
            // Добавление статистики в отчет
            sb.append("\nСтатистика ошибок:\n");
            sb.append(String.format("Средняя ошибка: %e\n", avgError));
            sb.append(String.format("Максимальная ошибка: %e\n", maxError));
            
            // Обновление области вывода результатов
            resultArea.setText(sb.toString());
            // Переключение на вкладку с результатами
            tabbedPane.setSelectedIndex(0);
            
        } catch (NumberFormatException ex) {
            // Обработка ошибок формата чисел
            JOptionPane.showMessageDialog(this, "Ошибка ввода данных: " + ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            // Обработка некорректных аргументов
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Обработка общих ошибок вычислений
            JOptionPane.showMessageDialog(this, "Ошибка вычислений: " + ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Точка входа в приложение.
     * 
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        // Запуск в потоке обработки событий Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Установка системного оформления
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Создание и отображение главного окна
            RungeKuttaGUI gui = new RungeKuttaGUI();
            // Центрирование окна на экране
            gui.setLocationRelativeTo(null);
            // Отображение окна
            gui.setVisible(true);
        });
    }
}

