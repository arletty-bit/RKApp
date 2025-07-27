package ru.rkapp;

import ru.rkapp.methods.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;

/**
 * Главное окно приложения для численного решения обыкновенных дифференциальных
 * уравнений (ОДУ) с использованием методов Рунге-Кутты. Приложение
 * предоставляет:
 * <ul>
 * <li>Интерфейс для выбора тестовых функций и методов решения</li>
 * <li>Параметризацию условий интегрирования (начальные условия, диапазон,
 * шаги)</li>
 * <li>Визуализацию результатов в виде графиков и табличных данных</li>
 * <li>Анализ точности численных решений (ошибки, производные)</li>
 * <li>Поддержку адаптивных и неадаптивных методов Рунге-Кутты</li>
 * </ul>
 *
 * <p>
 * Класс реализует графический интерфейс с использованием Swing и включает:
 * <ul>
 * <li>Панель управления с параметрами расчета</li>
 * <li>Интерактивный график с отображением решения и ошибок</li>
 * <li>Таблицу с детальными результатами вычислений</li>
 * <li>Механизм динамического обновления данных</li>
 * </ul>
 */
public class RungeKuttaGUI extends JFrame {

    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(RungeKuttaGUI.class);


    /**
     * Выпадающий список для выбора тестовой функции (SIN, COS, EXP и др.).
     */
    private JComboBox<TestFunction> functionComboBox;

    /**
     * Выпадающий список для выбора численного метода (Эйлера, Рунге-Кутты 4-го
     * порядка и др.).
     */
    private JComboBox<MethodWrapper> methodComboBox;

    /**
     * Поле ввода начального значения независимой переменной.
     */
    private JTextField x0Field;

    /**
     * Поле ввода начального значения функции.
     */
    private JTextField y0Field;

    /**
     * Поле ввода нижней границы интервала интегрирования.
     */
    private JTextField minXField;

    /**
     * Поле ввода верхней границы интервала интегрирования.
     */
    private JTextField maxXField;

    /**
     * Поле ввода количества шагов интегрирования.
     */
    private JTextField stepsField;

    /**
     * Текстовая область для вывода таблицы результатов.
     */
    private JTextArea resultArea;

    /**
     * Метка для отображения значения производной в начальной точке.
     */
    private JLabel derivativeLabel;

    /**
     * Панель для визуализации графиков решения, точных значений и ошибок.
     */
    private GraphPanel graphPanel;

    /**
     * Панель с вкладками (график/таблица результатов).
     */
    private JTabbedPane tabbedPane;

    /**
     * Флажок управления отображением производной на графике.
     */
    private JCheckBox derivativeCheckBox;

    /**
     * Флажок управления отображением ошибки на графике.
     */
    private JCheckBox errorCheckBox;

    /**
     * Менеджер для вычисления значений тестовой функции.
     */
    private FunctionManager modelFunction;

    /**
     * Менеджер для вычисления производных тестовой функции.
     */
    private FunctionManager derivativeFunction;
    
    /**
     * Поле ввода количества точек интерполяции между шагами.
     */
    private JTextField interpolationPointsField;

    /**
     * Конструктор главного окна приложения.
     *
     * - Настраивает параметры окна - Создает интерфейс управления -
     * Устанавливает обработчики событий - Задает начальные параметры
     */
    public RungeKuttaGUI() {
        configureMainWindow();
        JPanel inputPanel = createInputPanel();
        JPanel resultPanel = createResultPanel();
        add(inputPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        setupEventHandlers();
        setDefaultParametersForFunction();
        updateDerivative();
        updateInterpolationFieldState(); // Initialize field state
    }

    /**
     * Настраивает основные параметры главного окна.
     */
    private void configureMainWindow() {
        setTitle("Методы Рунге-Кутты");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    /**
     * Создает панель ввода параметров расчета.
     *
     * @return Панель с компонентами: - Выбор функции/метода - Поля ввода
     * значений - Флажки отображения
     */
    private JPanel createInputPanel() {
        // Changed to 0 rows to auto-size based on content
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Функция:"));
        functionComboBox = new JComboBox<>(TestFunction.values());
        panel.add(functionComboBox);

        panel.add(new JLabel("Метод:"));
        methodComboBox = new JComboBox<>();
        initializeMethods();
        panel.add(methodComboBox);

        panel.add(new JLabel("Производная:"));
        derivativeCheckBox = new JCheckBox("Показать");
        panel.add(derivativeCheckBox);

        panel.add(new JLabel("Ошибка:"));
        errorCheckBox = new JCheckBox("Показать");
        errorCheckBox.setSelected(true);
        panel.add(errorCheckBox);

        addInputFields(panel);

        return panel;
    }

    /**
     * Инициализирует список доступных методов.
     */
    private void initializeMethods() {
        methodComboBox.addItem(new MethodWrapper("Эверхарт (-:[2,32])", calc -> new Everhart(calc, 15, 1), true));
        methodComboBox.addItem(new MethodWrapper("Метод Эйлера (1:1)", EULER::new, false));
        methodComboBox.addItem(new MethodWrapper("Метод Трапеций (2:2)", T2::new, false));
        methodComboBox.addItem(new MethodWrapper("Метод Средней Точки (2:2)", CRK2a::new, false));
        methodComboBox.addItem(new MethodWrapper("CRK3a (3:3)", CRK3a::new, false));
        methodComboBox.addItem(new MethodWrapper("CRK3b (3:3)", CRK3b::new, false));
        methodComboBox.addItem(new MethodWrapper("CRK3c Метод Хойна (3:3)", CRK3c::new, false));
        methodComboBox.addItem(new MethodWrapper("Классический метод Рунге-Кутта (4:4)", CRK4a::new, false));
        methodComboBox.addItem(new MethodWrapper("Правило 3/8 Кутта (4:4)", CRK4b::new, false));
        methodComboBox.addItem(new MethodWrapper("CRK4c (4:4)", CRK4c::new, false));
        methodComboBox.addItem(new MethodWrapper("CRK5a Метод Кутта-Нюстрема (6:5)", CRK5a::new, false));
        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (7:4)", calc -> new DOPRI5(calc, 4), false));
        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (7:5)", calc -> new DOPRI5(calc, 5), false));
        methodComboBox.addItem(new MethodWrapper("CRK6a (7:6)", CRK6a::new, false));
        methodComboBox.addItem(new MethodWrapper("Метод Бутчера (7:6)", CRK6x::new, false));
        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (13:7)", calc -> new DOPRI8(calc, 7), false));
        methodComboBox.addItem(new MethodWrapper("Дорман-Принс (13:8)", calc -> new DOPRI8(calc, 8), false));
        methodComboBox.addItem(new MethodWrapper("DormandPrince853Integrator", DormandPrince853Integrator::new, false));
//        methodComboBox.addItem(new MethodWrapper("AdaptiveDormandPrince853Integrator",
//                calc -> new AdaptiveDormandPrince853Integrator(calc, 1e-8, 0.1, 1e-8, 1e-8), false);
    }

    /**
     * Добавляет поля ввода на указанную панель.
     *
     * @param panel Панель для добавления компонентов ввода
     */
    private void addInputFields(JPanel panel) {
        panel.add(new JLabel("Начальное x:"));
        x0Field = new JTextField("0.0");
        panel.add(x0Field);

        panel.add(new JLabel("Начальное y:"));
        y0Field = new JTextField("0.0");
        panel.add(y0Field);

        panel.add(new JLabel("Min X:"));
        minXField = new JTextField("0.0");
        panel.add(minXField);

        panel.add(new JLabel("Max X:"));
        maxXField = new JTextField("6.28318530717959");
        panel.add(maxXField);

        panel.add(new JLabel("Шаги:"));
        stepsField = new JTextField("180");
        panel.add(stepsField);

        panel.add(new JLabel("Точек интерполяции:"));
        interpolationPointsField = new JTextField("0"); // Значение по умолчанию
        panel.add(interpolationPointsField);
    }

    /**
     * Настраивает состояние поля интерполяции в зависимости от выбранного метода.
     * <p>
     * Логика работы:
     * <ul>
     * <li>Определяет поддержку интерполяции через MethodWrapper.supportsInterpolation()</li>
     * <li>Автоматически блокирует поле для методов без интерполяции</li>
     * <li>Вызывается при инициализации и смене метода</li>
     * </ul>
     */
    private void updateInterpolationFieldState() {
        MethodWrapper wrapper = (MethodWrapper) methodComboBox.getSelectedItem();
        interpolationPointsField.setEnabled(wrapper.supportsInterpolation());
    }

    /**
     * Создает панель результатов с вкладками для графика и текстовых данных.
     *
     * @return Панель результатов
     */
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
//        derivativeLabel = new JLabel("Производная в x0: ");
//        panel.add(derivativeLabel, BorderLayout.NORTH);

        tabbedPane = createTabbedPane();
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Создает панель с вкладками для отображения графика и текстовых
     * результатов.
     *
     * @return Панель с вкладками
     */
    private JTabbedPane createTabbedPane() {
        JTabbedPane pane = new JTabbedPane();
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setEditable(false);
        pane.addTab("График", new JScrollPane(createGraphPanel()));
        pane.addTab("Результаты", new JScrollPane(resultArea));
        return pane;
    }

    /**
     * Создает и настраивает панель для отображения графиков.
     *
     * @return Панель графика
     */
    private GraphPanel createGraphPanel() {
        graphPanel = new GraphPanel();
        return graphPanel;
    }

    /**
     * Создает панель с кнопкой выполнения вычислений.
     *
     * @return Панель с кнопкой
     */
    private JPanel createButtonPanel() {
        JButton calculateButton = new JButton("Вычислить");
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        calculateButton.addActionListener(this::calculateAction);

        JPanel panel = new JPanel();
        panel.add(calculateButton);
        return panel;
    }

    /**
     * Настраивает обработчики событий для компонентов интерфейса.
     */
    private void setupEventHandlers() {
        functionComboBox.addActionListener(e -> {
            updateDerivative();
            setDefaultParametersForFunction();
        });
        x0Field.addActionListener(e -> updateDerivative());
        methodComboBox.addActionListener(e -> updateInterpolationFieldState());
    }

    /**
     * Устанавливает значения параметров по умолчанию в зависимости от выбранной
     * функции.
     */
    private void setDefaultParametersForFunction() {
        TestFunction function = (TestFunction) functionComboBox.getSelectedItem();
        if (function == TestFunction.LOG) {
            minXField.setText("1");
            maxXField.setText("2");
            stepsField.setText("100");
            x0Field.setText("1");
            y0Field.setText("1");
        } else if (function == TestFunction.SIN_COS) {
            minXField.setText("0.0");
            maxXField.setText("6.28318530717959");
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
//            derivativeLabel.setText("Производная в x0: " + String.format("%.6f", derivative));
        } catch (NumberFormatException e) {
            if (derivativeLabel != null) {
                derivativeLabel.setText("Производная в x0: ошибка ввода числа");
            }
            LOG.fatal("Производная в x0: ошибка ввода числа: {} ;", e);
        } catch (Exception e) {
            if (derivativeLabel != null) {
                derivativeLabel.setText("Производная в x0: ошибка вычисления");
            }
            y0Field.setText("");
            LOG.fatal("Производная в x0: ошибка вычисления: {} ;", e);

        }
    }

    /**
     * Выполняет основной расчет: 1. Парсит параметры 2. Проверяет корректность
     * данных 3. Запускает решатель ОДУ 4. Визуализирует результаты 5.
     * Генерирует отчет
     *
     * @param e Событие кнопки "Вычислить"
     */
    private void calculateAction(ActionEvent e) {
        try {
            CalculationParameters params = parseInputParameters();
            validateParameters(params);

            double h = (params.getMaxX() - params.getMinX()) / params.getSteps();
            setupFunctionManagers(params.getFunction());

            List<double[]> solution = performCalculation(params, h);
            VisualizationData data = prepareVisualizationData(params, solution, h);
            updateGraph(data);
            generateReport(params, solution, data, h);

        } catch (NumberFormatException ex) {
            LOG.fatal("Ошибка вычислений: {} ;", ex);
            showErrorDialog("Ошибка ввода данных: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOG.fatal("Ошибка вычислений: {} ;", ex);
            showErrorDialog(ex.getMessage());
        } catch (Exception ex) {
            LOG.fatal("Ошибка вычислений: {} ;", ex);
            showErrorDialog("Ошибка вычислений: " + ex.getMessage());
        }
    }

    /**
     * Парсит входные параметры из полей ввода.
     *
     * @return Объект с параметрами расчета
     * @throws NumberFormatException Если неверный формат чисел
     */
    private CalculationParameters parseInputParameters() throws NumberFormatException {
        TestFunction function = (TestFunction) functionComboBox.getSelectedItem();
        MethodWrapper methodWrapper = (MethodWrapper) methodComboBox.getSelectedItem();
        double x0 = Double.parseDouble(x0Field.getText());
        double minX = Double.parseDouble(minXField.getText());
        double maxX = Double.parseDouble(maxXField.getText());
        int steps = Integer.parseInt(stepsField.getText());
        int interpolationPoints = Integer.parseInt(interpolationPointsField.getText());

        return new CalculationParameters(function, methodWrapper, x0, minX, maxX, steps, interpolationPoints);
    }

    /**
     * Проверяет корректность введенных параметров расчета.
     *
     * @param params Параметры расчета
     * @throws IllegalArgumentException Если параметры некорректны
     */
    private void validateParameters(CalculationParameters params) throws IllegalArgumentException {
        if (params.getSteps() <= 0) {
            throw new IllegalArgumentException("Количество шагов должно быть положительным");
        }
        if (params.getMaxX() <= params.getMinX()) {
            LOG.fatal("Max X должен быть больше Min X;");
            throw new IllegalArgumentException("Max X должен быть больше Min X");
        }
    }

    /**
     * Настраивает менеджеры функций для расчетов.
     *
     * @param function Выбранная тестовая функция
     */
    private void setupFunctionManagers(TestFunction function) throws Exception {
        modelFunction = new FunctionManager(function.getExpression());
        derivativeFunction = new FunctionManager(getDerivativeExpression(function));
    }

    /**
     * Возвращает строковое представление производной для указанной функции.
     *
     * @param function Тестовая функция
     * @return Строковое выражение производной
     */
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
     * Выполняет численный расчет ОДУ.
     * <p>
     * Алгоритм:
     * <ol>
     * <li>Создает RightCalculator для вычисления правых частей ОДУ</li>
     * <li>Инициализирует метод Рунге-Кутты через MethodWrapper</li>
     * <li>Выбирает режим расчета (с интерполяцией/без) на основе параметров</li>
     * <li>Использует RungeKuttaSolver для выполнения вычислений</li>
     * </ol>
     *
     * @param params Параметры расчета
     * @param h      Шаг интегрирования
     * @return Список точек решения, где каждый элемент - массив [x, y, ...]
     * @see RungeKuttaSolver#solveWithInterpolation
     */

    private List<double[]> performCalculation(CalculationParameters params, double h) {
        RightCalculator calculator = (t, y, f, parm) -> {
            try {
                //f[0] = params.getFunction().derivative(t);            // ◄◄◄ Производная из строки
                // f[0] = params.getFunction().numericalDerivative(t);   // ◄◄◄ Вычисление производной
                f[0] = derivativeFunction.compute(t);     // ◄◄◄ Динамический вызов
            } catch (Exception ex) {
                LOG.fatal("Ошибка при расчете: '{}';", ex);
            }
            return true;
        };

        RungeKuttaMethod method = params.getMethodWrapper().createMethod(calculator);
        double[] y0Arr = {params.getFunction().value(params.getX0())};

        int interpolationPoints = params.getInterpolationPoints();
        if (interpolationPoints > 0 && method.supportsInterpolation()) {
            return RungeKuttaSolver.solveWithInterpolation(
                    method, params.getMinX(), y0Arr, h, params.getSteps(), interpolationPoints, null
            );
        } else {
            return RungeKuttaSolver.solve(
                    method, params.getMinX(), y0Arr, h, params.getSteps(), null
            );
        }
    }
    /**
     * Подготавливает данные для визуализации.
     * <p>
     * Вычисляет:
     * <ul>
     * <li>Численное решение во всех точках</li>
     * <li>Аналитическое решение для сравнения</li>
     * <li>Значения производных (если включено отображение)</li>
     * <li>Локальные и глобальные ошибки</li>
     * </ul>
     * 
     * @param params   Параметры расчета
     * @param solution Результаты численного решения
     * @param h        шаг (может отличаться от реального при интерполяции)
     * @return Структура данных для визуализации
     */
 private VisualizationData prepareVisualizationData(
        CalculationParameters params,
        List<double[]> solution,
        double h
) {
    double minX = params.getMinX();
    double maxX = params.getMaxX();
    int n = solution.size();

    // Calculate step between points based on actual solution size
    double stepIncrement = (maxX - minX) / (n - 1);

    List<Double> xValues = new ArrayList<>(n);
    List<Double> yValues = new ArrayList<>(n);
    List<Double> exactValues = new ArrayList<>(n);
    List<Double> derivativeValues = new ArrayList<>();
    List<Double> errorValues = new ArrayList<>(n);

    double maxError = 0;
    double sumError = 0;

    for (int k = 0; k < n; k++) {
        double x = minX + k * stepIncrement;
        xValues.add(x);

        double numY = solution.get(k)[0];
        yValues.add(numY);

        double exactY = params.getFunction().value(x);
        exactValues.add(exactY);

        double error = Math.abs(numY - exactY);
        errorValues.add(error);

        sumError += error;
        if (error > maxError) {
            maxError = error;
        }

        if (derivativeCheckBox.isSelected()) {
            derivativeValues.add(params.getFunction().numericalDerivative(x));
        }
    }

    double avgError = sumError / n;

    return new VisualizationData(
            xValues, yValues, exactValues, derivativeValues, errorValues,
            maxError, avgError
    );
}

    /**
     * Обновляет график на основе подготовленных данных.
     *
     * @param data Данные для визуализации
     */
    private void updateGraph(VisualizationData data) {
        graphPanel.setData(data.getXValues(), data.getYValues(), data.getExactValues());
        graphPanel.setDerivativeData(data.getDerivativeValues());
        graphPanel.setShowDerivative(derivativeCheckBox.isSelected());
        graphPanel.setErrorData(data.getErrorValues());
        graphPanel.setShowError(errorCheckBox.isSelected());
        graphPanel.setMaxError(data.getMaxError());
        graphPanel.repaint();
    }

    /**
     * Генерирует текстовый отчет о результатах расчета.
     *
     * @param params Параметры расчета
     * @param solution Результаты расчета
     * @param data Данные визуализации
     * @param h Шаг интегрирования
     */
    private void generateReport(
            CalculationParameters params,
            List<double[]> solution,
            VisualizationData data,
            double h
    ) {
        StringBuilder sb = new StringBuilder();
        appendHeaderInfo(sb, params, h);
        appendResultsTable(sb, params, solution, data, h);
        appendErrorStatistics(sb, data);

        resultArea.setText(sb.toString());
        tabbedPane.setSelectedIndex(0); // Переключаем на вкладку с текстовыми результатами
    }

    /**
     * Добавляет заголовочную информацию в отчет.
     */
    private void appendHeaderInfo(StringBuilder sb, CalculationParameters params, double h) {
        sb.append("Метод: ").append(params.getMethodWrapper().toString()).append("\n");
        sb.append("Функция: ").append(params.getFunction().toString()).append("\n");
        sb.append("Параметры: minX = ").append(params.getMinX())
                .append(", maxX = ").append(params.getMaxX())
                .append(", y0 = ").append(params.getFunction().value(params.getX0()))
                .append(", шагов = ").append(params.getSteps())
                .append(", h = ").append(String.format("%.6f", h)).append("\n\n");
    }

    /**
     * Добавляет таблицу результатов в отчет.
     */
    private void appendResultsTable(
            StringBuilder sb,
            CalculationParameters params,
            List<double[]> solution,
            VisualizationData data,
            double h
    ) {
        sb.append("Результаты:\n");
        sb.append(String.format("%-10s %-15s %-15s %-15s\n", "x", "Численное", "Точное", "Ошибка"));

        for (int i = 0; i < solution.size(); i++) {
            double x = params.getMinX() + i * h;
            double numY = solution.get(i)[0];
            double exactY = data.getExactValues().get(i);
            double error = data.getErrorValues().get(i);

            sb.append(String.format("%-10.4f %-15.8f %-15.8f %-15.8e\n",
                    x, numY, exactY, error));
        }
    }

    /**
     * Добавляет статистику ошибок в отчет.
     */
    private void appendErrorStatistics(StringBuilder sb, VisualizationData data) {
        sb.append("\nСтатистика ошибок:\n");
        sb.append(String.format("Средняя ошибка: %.16e\n", data.getAvgError()));
        sb.append(String.format("Максимальная ошибка: %.16e\n", data.getMaxError()));
    }

    /**
     * Отображает диалоговое окно с сообщением об ошибке.
     *
     * @param message Текст сообщения об ошибке
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Точка входа в приложение: - Устанавливает системный стиль интерфейса -
     * Создает и отображает главное окно - Центрирует окно на экране
     *
     * @param args Аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOG.fatal("Ошибка: '{}';", e);
            }

            RungeKuttaGUI gui = new RungeKuttaGUI();
            gui.setLocationRelativeTo(null);
            gui.setVisible(true);
        });
    }

    /**
     * Параметры расчета для одного запуска.
     * <p>
     * Служит контейнером для передачи данных между компонентами системы.
     * Все поля объявлены final для обеспечения неизменности во время расчета.
     */
    private static class CalculationParameters {

        /**
         * Выбранная тестовая функция для решения.
         */
        private final TestFunction function;

        /**
         * Обёртка для создания метода Рунге-Кутты.
         */
        private final MethodWrapper methodWrapper;

        /**
         * Начальное значение независимой переменной (x₀).
         */
        private final double x0;

        /**
         * Нижняя граница интервала интегрирования.
         */
        private final double minX;

        /**
         * Верхняя граница интервала интегрирования.
         */
        private final double maxX;

        /**
         * Количество шагов интегрирования.
         */
        private final int steps;

        /**
         * Количество точек интерполяции между узлами сетки.
         * <p>
         * Значение игнорируется если:
         * <ul>
         * <li>Метод не поддерживает интерполяцию (supportsInterpolation() == false)</li>
         * <li>Значение ≤ 0</li>
         * </ul>
         */
        private final int interpolationPoints;

        /**
         * Создаёт объект с параметрами расчета.
         *
         * @param function Тестовая функция
         * @param methodWrapper Обёртка метода Рунге-Кутты
         * @param x0 Начальное значение x
         * @param minX Минимальное значение x
         * @param maxX Максимальное значение x
         * @param steps Количество шагов
         */
        public CalculationParameters(TestFunction function, MethodWrapper methodWrapper,
            double x0, double minX, double maxX, int steps, int interpolationPoints) {
            
            this.function = function;
            this.methodWrapper = methodWrapper;
            this.x0 = x0;
            this.minX = minX;
            this.maxX = maxX;
            this.steps = steps;
            this.interpolationPoints = interpolationPoints;
        }

        /**
         * Возвращает тестовую функцию.
         */
        public TestFunction getFunction() {
            return function;
        }

        /**
         * Возвращает обёртку метода Рунге-Кутты.
         */
        public MethodWrapper getMethodWrapper() {
            return methodWrapper;
        }

        /**
         * Возвращает начальное значение x.
         */
        public double getX0() {
            return x0;
        }

        /**
         * Возвращает нижнюю границу интервала.
         */
        public double getMinX() {
            return minX;
        }

        /**
         * Возвращает верхнюю границу интервала.
         */
        public double getMaxX() {
            return maxX;
        }

        /**
         * Возвращает количество шагов интегрирования.
         */
        public int getSteps() {
            return steps;
        }

        public int getInterpolationPoints() {
            return interpolationPoints;
        }

    }

    /**
     * Контейнер для данных визуализации.
     * <p>
     * Оптимизирован для быстрого доступа при рендеринге графиков.
     * Все коллекции инициализируются с предсказуемой емкостью.
     */
    private static class VisualizationData {

        /**
         * Значения аргумента по оси X.
         */
        private final List<Double> xValues;

        /**
         * Численные значения решения (Y).
         */
        private final List<Double> yValues;

        /**
         * Точные значения функции.
         */
        private final List<Double> exactValues;

        /**
         * Значения производной в точках расчета.
         * <p>
         * Особенности:
         * <ul>
         * <li>Рассчитывается только при включенной опции показа производной</li>
         * <li>Использует численное дифференцирование тестовой функции</li>
         * <li>Не используется в методах решения - только для визуализации</li>
         * </ul>
         */
        private final List<Double> derivativeValues;

        /**
         * Значения ошибок (|yₙᵤₘ - yₑₓₐ꜀ₜ|).
         */
        private final List<Double> errorValues;

        /**
         * Максимальная ошибка на интервале.
         */
        private final double maxError;

        /**
         * Средняя ошибка на интервале.
         */
        private final double avgError;

        /**
         * Создаёт объект с данными для визуализации.
         *
         * @param xValues Список значений X
         * @param yValues Список численных решений Y
         * @param exactValues Список точных значений Y
         * @param derivativeValues Список значений производной
         * @param errorValues Список ошибок
         * @param maxError Максимальная ошибка
         * @param avgError Средняя ошибка
         */
        public VisualizationData(List<Double> xValues, List<Double> yValues, List<Double> exactValues,
                List<Double> derivativeValues, List<Double> errorValues,
                double maxError, double avgError) {
            this.xValues = xValues;
            this.yValues = yValues;
            this.exactValues = exactValues;
            this.derivativeValues = derivativeValues;
            this.errorValues = errorValues;
            this.maxError = maxError;
            this.avgError = avgError;
        }

        /**
         * Возвращает значения аргумента X.
         */
        public List<Double> getXValues() {
            return xValues;
        }

        /**
         * Возвращает численные решения Y.
         */
        public List<Double> getYValues() {
            return yValues;
        }

        /**
         * Возвращает точные значения функции.
         */
        public List<Double> getExactValues() {
            return exactValues;
        }

        /**
         * Возвращает значения производной.
         */
        public List<Double> getDerivativeValues() {
            return derivativeValues;
        }

        /**
         * Возвращает значения ошибок.
         */
        public List<Double> getErrorValues() {
            return errorValues;
        }

        /**
         * Возвращает максимальную ошибку.
         */
        public double getMaxError() {
            return maxError;
        }

        /**
         * Возвращает среднюю ошибку.
         */
        public double getAvgError() {
            return avgError;
        }
    }
}