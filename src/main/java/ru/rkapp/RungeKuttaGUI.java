package ru.rkapp;

import ru.rkapp.*;
import ru.rkapp.methods.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Главное окно приложения для решения обыкновенных дифференциальных уравнений с
 * использованием методов Рунге-Кутты с визуализацией результатов.
 *
 * <p>
 * Окно содержит:
 * <ul>
 * <li>Поля ввода параметров вычислений</li>
 * <li>Выбор тестовой функции</li>
 * <li>Выбор численного метода</li>
 * <li>Область вывода результатов в табличном виде</li>
 * <li>Панель визуализации графиков решения</li>
 * </ul>
 */
public class RungeKuttaGUI extends JFrame {

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
    private final GraphPanel graphPanel;         // Панель для рисования графиков
    private final JTabbedPane tabbedPane;        // Панель с вкладками
    private final JCheckBox derivativeCheckBox;     // Флажок производной
    private final JCheckBox errorCheckBox;          //  Флажок ошибки

    /**
     * Конструктор главного окна приложения. Инициализирует компоненты
     * пользовательского интерфейса и настраивает окно.
     */
    public RungeKuttaGUI() {
        // Настройка основного окна
        setTitle("Методы Рунге-Кутты");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Создание панели ввода параметров
        JPanel inputPanel = new JPanel(new GridLayout(9, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Добавление выбора функции
        inputPanel.add(new JLabel("Функция:"));
        functionComboBox = new JComboBox<>(TestFunction.values());
        inputPanel.add(functionComboBox);

        // Добавление выбора метода
        inputPanel.add(new JLabel("Метод:"));
        methodComboBox = new JComboBox<>();
        inputPanel.add(methodComboBox);

        // Добавляем флажок для производной
        inputPanel.add(new JLabel("Производная:"));
        derivativeCheckBox = new JCheckBox("Показать");
        inputPanel.add(derivativeCheckBox);

        inputPanel.add(new JLabel("Ошибка:"));
        errorCheckBox = new JCheckBox("Показать");
        errorCheckBox.setSelected(true);

        inputPanel.add(errorCheckBox);

        // Заполнение списка методов Рунге-Кутты        
        methodComboBox.addItem(new MethodWrapper("Эверхарт (-:[2,32])",
                calc -> new Everhart(calc, 15, 1)));
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
        methodComboBox.addItem(new MethodWrapper("AdaptiveDormandPrince853Integrator",
                calc -> new AdaptiveDormandPrince853Integrator(
                        calc,
                        1e-8, // minStep
                        0.1, // maxStep
                        1e-8, // absTol
                        1e-8 // relTol
                )
        ));
//        AdaptiveDormandPrince853Integrator integrator = 
//            new AdaptiveDormandPrince853Integrator(minStep, maxStep, absTol, relTol);
//                methodComboBox.addItem(new MethodWrapper("AdaptiveDormandPrince853Integrator", AdaptiveDormandPrince853Integrator::new));



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
        functionComboBox.addActionListener(e -> {
            updateDerivative();
            setDefaultParametersForFunction();
        });
        x0Field.addActionListener(e -> updateDerivative());
        setDefaultParametersForFunction();
        // Первоначальное обновление производной
        updateDerivative();
    }

    private void setDefaultParametersForFunction() {
        TestFunction function = (TestFunction) functionComboBox.getSelectedItem();
        if (function == TestFunction.LOG) {
            minXField.setText("1");
            maxXField.setText("2");
            stepsField.setText("100");
            x0Field.setText("1");
            y0Field.setText("1");
        } else 
            
            if (function == TestFunction.SIN_COS) {
            minXField.setText("0.0");
            maxXField.setText("6.28318530717959"); // 2π
            stepsField.setText("180");
        }
    }

    /**
     * Обновляет значение производной при изменении функции или начальной точки.
     */
    private void updateDerivative() {
        TestFunction function = (TestFunction) functionComboBox.getSelectedItem();
        try {
            double x0 = Double.parseDouble(x0Field.getText());
            double y0 = function.value(x0);
            y0Field.setText(String.format("%.6f", y0));

            double derivative = function.derivative(x0);
            derivativeLabel.setText("Производная в x0: " + String.format("%.6f", derivative));
        } catch (NumberFormatException e) {
            derivativeLabel.setText("Производная в x0: ошибка ввода числа");
        } catch (Exception e) {
            derivativeLabel.setText("Производная в x0: ошибка вычисления");
            y0Field.setText(""); // Сброс некорректного значения
        }
    }

    // Поля для хранения функций
    private FunctionManager modelFunction;
    private FunctionManager derivativeFunction;

    // Вспомогательный метод для получения производных выражений
    private String getDerivativeExpression(TestFunction function) {
        switch (function) {
            case SIN:
                return "Math.cos(x)";
            case COS:
                return "-Math.sin(x)";
            case EXP:
                return "Math.exp(x)";
            case QUAD:
                return "2 * x";
            case SIN_COS:
                return "Math.cos(x)*Math.cos(10*x) - 10*Math.sin(x)*Math.sin(10*x)";
            case LOG:
                return "1.0 / x";
            default:
                throw new IllegalArgumentException("Неизвестная функция");
        }
    }

    /**
     * Обработчик события нажатия кнопки "Вычислить". Выполняет решение ОДУ
     * выбранным методом и отображает результаты.
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
            double y0 = function.value(x0);  // ◄◄◄ Начальное значение функции в точке x0
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

            modelFunction = new FunctionManager(function.getExpression());
            derivativeFunction = new FunctionManager(getDerivativeExpression(function));

            // Создание вычислителя правых частей ОДУ
            // dy/dx = f(t), где f(t) - производная выбранной функции
            RightCalculator calculator = (t, y, f, parm) -> {
                try {
                    // f[0] = function.derivative(t);           // ◄◄◄ Производная из строки
                    //f[0] = function.numericalDerivative(t);   // ◄◄◄ Вычисление производной
                    f[0] = derivativeFunction.compute(t);       // ◄◄◄ Динамический вызов
                } catch (Exception ex) {
                    Logger.getLogger(RungeKuttaGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            };

            // Создание экземпляра метода Рунге-Кутты
            RungeKuttaMethod method = methodWrapper.createMethod(calculator);

            // Подготовка начальных условий
            double[] y0Arr = {y0};

            // Решение ОДУ методом Рунге-Кутты ◄◄◄
            List<double[]> solution = RungeKuttaSolver.solve(
                    method, minX, y0Arr, h, steps, null
            );

            // Подготовка данных для визуализации
            List<Double> xValues = new ArrayList<>();
            List<Double> yValues = new ArrayList<>();      // ◄◄◄ Численное решение
            List<Double> exactValues = new ArrayList<>();  // ◄◄◄ Точное решение функции
            List<Double> derivativeValues = null;          // ◄◄◄ Значения производной

            // Заполнение списков координат
            double currentX = minX;
            for (int i = 0; i < solution.size(); i++) {
                xValues.add(currentX);
                yValues.add(solution.get(i)[0]); // Численное решение (результат Рунге-Кутты) ◄◄◄
                exactValues.add(function.value(currentX)); // Точное значение функции ◄◄◄
                // Увеличение x для следующей точки (кроме последней итерации)
                currentX += (i < solution.size() - 1) ? h : 0;
            }

            // Обновление графика
            graphPanel.setData(xValues, yValues, exactValues);

            if (derivativeCheckBox.isSelected()) {
                derivativeValues = new ArrayList<>();
                for (double x : xValues) {
                    // Численная производная через центральные разности
                    derivativeValues.add(function.numericalDerivative(x));
                }
            }

            // Обновление графика с учетом производной
            graphPanel.setData(xValues, yValues, exactValues);
            graphPanel.setDerivativeData(derivativeValues);
            graphPanel.setShowDerivative(derivativeCheckBox.isSelected());

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
                if (error > maxError) {
                    maxError = error;
                }

                // Добавление строки в таблицу
                sb.append(String.format("%-10.4f %-15.8f %-15.8f %-15.8e\n",
                        x, numY, exactY, error));
            }

            // Ошибка
            List<Double> errorValues = null;
            if (errorCheckBox.isSelected()) {
                errorValues = new ArrayList<>();
                for (int i = 0; i < solution.size(); i++) {
                    double x = minX + i * h;
                    double exactY = function.value(x);
                    double numY = solution.get(i)[0];
                    double error = Math.abs(numY - exactY);
                    errorValues.add(error);
                }
            }

            graphPanel.setErrorData(errorValues);
            graphPanel.setShowError(errorCheckBox.isSelected());
            
            graphPanel.setMaxError(maxError);

            // Вычисление средней ошибки
            double avgError = sumError / solution.size();
            // Добавление статистики в отчет
            sb.append("\nСтатистика ошибок:\n");
            
            sb.append(String.format("Средняя ошибка: %.16e\n", avgError));
            sb.append(String.format("Максимальная ошибка: %.16e\n", maxError));

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
