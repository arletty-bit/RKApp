package ru.rkapp;

import ru.rkapp.methods.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import ru.rkapp.Ballistics.SpacecraftForcesCalculator;
import ru.rkapp.Ballistics.SpacecraftMotionSolverV2;

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
     * Выпадающий список для выбора тестовой функции (SIN, COS, EXP и др.)
     */
    private JComboBox<Object> functionComboBox;

    /**
     * Выпадающий список для выбора численного метода (Эйлера, Рунге-Кутты 4-го
     * порядка и др.)
     */
    private JComboBox<MethodWrapper> methodComboBox;

    /**
     * Поле ввода начального значения независимой переменной
     */
    private JTextField x0Field;

    /**
     * Поле ввода начального значения функции
     */
    private JTextField y0Field;

    /**
     * Поле ввода нижней границы интервала интегрирования
     */
    private JTextField minXField;

    /**
     * Поле ввода верхней границы интервала интегрирования
     */
    private JTextField maxXField;

    /**
     * Поле ввода количества шагов интегрирования
     */
    private JTextField stepsField;

    /**
     * Текстовая область для вывода таблицы результатов
     */
    private JTextArea resultArea;

    /**
     * Метка для отображения значения производной в начальной точке
     */
    private JLabel derivativeLabel;

    /**
     * Панель для визуализации графиков решения, точных значений и ошибок
     */
    private GraphPanel graphPanel;

    /**
     * Панель с вкладками (график/таблица результатов)
     */
    private JTabbedPane tabbedPane;

    /**
     * Флажок управления отображением производной на графике
     */
    private JCheckBox derivativeCheckBox;

    /**
     * Флажок управления отображением ошибки на графике
     */
    private JCheckBox errorCheckBox;

    /**
     * Менеджер для вычисления значений тестовой функции
     */
    private FunctionManager modelFunction;

    /**
     * Менеджер для вычисления производных тестовой функции
     */
    private FunctionManager derivativeFunction;
    
    
    
        private JTextField balCoefField;
    private JTextField initialXField, initialYField, initialZField;
    private JTextField initialVxField, initialVyField, initialVzField;
    private JCheckBox gskCheckBox;
    
        private JPanel spacecraftPanel;
    private JPanel commonParamsPanel;
    
        // Добавляем новую панель для графиков состояния КА
    private SpacecraftStateGraphPanel spacecraftStateGraphPanel;
    
    private JTextField stepField;   // для шага интегрирования (сек)
private JTextField timeField;   // для времени прогноза (сек)

private JTextField yearField, monthField, dayField;
private JTextField hourField, minuteField, secondField;

    private JTextField interpolationPointsField;
    
    /**
     * Конструктор главного окна приложения.
     *
     * - Настраивает параметры окна - Создает интерфейс управления -
     * Устанавливает обработчики событий - Задает начальные параметры
     */
    public RungeKuttaGUI() {
         configureMainWindow();
        JTabbedPane inputPanel = createInputPanel();
        JPanel resultPanel = createResultPanel();
        add(inputPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        setupEventHandlers();
        setDefaultParametersForFunction();
        updateDerivative();
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

    private TestFunction getSelectedFunction() {
        Object selected = functionComboBox.getSelectedItem();
        if (selected instanceof StandardTestFunction) {
            return (StandardTestFunction) selected;
        } else if (selected == SpacecraftFunction.SPACECRAFT) {
            return SpacecraftFunction.SPACECRAFT;
    }
    return StandardTestFunction.SIN;
}

    /**
     * Создает панель ввода параметров расчета.
     *
     * @return Панель с компонентами: - Выбор функции/метода - Поля ввода
     * значений - Флажки отображения
     */
    private JTabbedPane createInputPanel() {
        JTabbedPane inputTabs = new JTabbedPane();
        
        // Вкладка общих параметров
        commonParamsPanel = new JPanel(new GridBagLayout());
        commonParamsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputTabs.addTab("Основные параметры", commonParamsPanel);
        
        // Вкладка параметров КА
        spacecraftPanel = new JPanel(new GridBagLayout());
        spacecraftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputTabs.addTab("Параметры КА", spacecraftPanel);
        
        initCommonParams();
        initSpacecraftParams();
        
        return inputTabs;
    }

        private void initCommonParams() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Строка 0: Функция
        gbc.gridx = 0; gbc.gridy = 0;
        commonParamsPanel.add(new JLabel("Функция:"), gbc);
        
        gbc.gridx = 1;
        functionComboBox = new JComboBox<>();
        for (StandardTestFunction func : StandardTestFunction.values()) {
            functionComboBox.addItem(func);
        }
        functionComboBox.addItem(SpacecraftFunction.SPACECRAFT);
        commonParamsPanel.add(functionComboBox, gbc);
        
        // Строка 1: Метод
        gbc.gridx = 0; gbc.gridy = 1;
        commonParamsPanel.add(new JLabel("Метод:"), gbc);
        
        gbc.gridx = 1;
        methodComboBox = new JComboBox<>();
        initializeMethods();
        commonParamsPanel.add(methodComboBox, gbc);
        
        // Строка 2: Флажки
        gbc.gridx = 0; gbc.gridy = 2;
        derivativeCheckBox = new JCheckBox("Показать производную");
        commonParamsPanel.add(derivativeCheckBox, gbc);
        
        gbc.gridx = 1;
        errorCheckBox = new JCheckBox("Показать ошибку");
        errorCheckBox.setSelected(true);
        commonParamsPanel.add(errorCheckBox, gbc);
        
        // Разделитель
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        commonParamsPanel.add(new JSeparator(), gbc);
        
        // Параметры расчета
        gbc.gridwidth = 1;
        gbc.gridy = 4;
        gbc.gridx = 0;
        commonParamsPanel.add(new JLabel("Начальное x:"), gbc);
        
        gbc.gridx = 1;
        x0Field = new JTextField("0.0");
        commonParamsPanel.add(x0Field, gbc);
        
        gbc.gridy = 5;
        gbc.gridx = 0;
        commonParamsPanel.add(new JLabel("Начальное y:"), gbc);
        
        gbc.gridx = 1;
        y0Field = new JTextField("0.0");
        commonParamsPanel.add(y0Field, gbc);
        
        gbc.gridy = 6;
        gbc.gridx = 0;
        commonParamsPanel.add(new JLabel("Min X:"), gbc);
        
        gbc.gridx = 1;
        minXField = new JTextField("0.0");
        commonParamsPanel.add(minXField, gbc);
        
        gbc.gridy = 7;
        gbc.gridx = 0;
        commonParamsPanel.add(new JLabel("Max X:"), gbc);
        
        gbc.gridx = 1;
        maxXField = new JTextField("6.28318530717959");
        commonParamsPanel.add(maxXField, gbc);
        
        gbc.gridy = 8;
        gbc.gridx = 0;
        commonParamsPanel.add(new JLabel("Шаги:"), gbc);
        
        gbc.gridx = 1;
        stepsField = new JTextField("180");
        commonParamsPanel.add(stepsField, gbc);
        
        // Добавление поля для точек интерполяции
    gbc.gridy = 9; // Следующая строка после "Шаги"
    gbc.gridx = 0;
    commonParamsPanel.add(new JLabel("Точки интерполяции:"), gbc);
    
    gbc.gridx = 1;
    interpolationPointsField = new JTextField("0"); // Значение по умолчанию
    interpolationPointsField.setToolTipText("Количество точек интерполяции внутри шага (0 = без интерполяции)");
    commonParamsPanel.add(interpolationPointsField, gbc);
    
    // Сдвигаем остальные элементы ниже
    gbc.gridy = 10;
    gbc.gridx = 0;

    }

    private void initSpacecraftParams() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Координаты
        gbc.gridx = 0; gbc.gridy = 0;
        spacecraftPanel.add(new JLabel("Начальное положение (км):"), gbc);
        
        gbc.gridy = 1;
        spacecraftPanel.add(new JLabel("X:"), gbc);
        gbc.gridx = 1;
        initialXField = new JTextField("-3545479.75");
        spacecraftPanel.add(initialXField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        spacecraftPanel.add(new JLabel("Y:"), gbc);
        gbc.gridx = 1;
        initialYField = new JTextField("6258868.92");
        spacecraftPanel.add(initialYField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        spacecraftPanel.add(new JLabel("Z:"), gbc);
        gbc.gridx = 1;
        initialZField = new JTextField("0.00137977308");
        spacecraftPanel.add(initialZField, gbc);
        
        // Скорости
        gbc.gridx = 0; gbc.gridy = 4;
        spacecraftPanel.add(new JLabel("Начальная скорость (км/с):"), gbc);
        
        gbc.gridy = 5;
        spacecraftPanel.add(new JLabel("Vx:"), gbc);
        gbc.gridx = 1;
        initialVxField = new JTextField("1447.08453");
        spacecraftPanel.add(initialVxField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        spacecraftPanel.add(new JLabel("Vy:"), gbc);
        gbc.gridx = 1;
        initialVyField = new JTextField("809.852736");
        spacecraftPanel.add(initialVyField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        spacecraftPanel.add(new JLabel("Vz:"), gbc);
        gbc.gridx = 1;
        initialVzField = new JTextField("7358.78977");
        spacecraftPanel.add(initialVzField, gbc);
        
        // Разделитель
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        spacecraftPanel.add(new JSeparator(), gbc);
        
        // Дополнительные параметры
        gbc.gridwidth = 1;
        gbc.gridy = 9;
        gbc.gridx = 0;
        spacecraftPanel.add(new JLabel("Баллистический коэффициент:"), gbc);
        
        gbc.gridx = 1;
        balCoefField = new JTextField("0.0143397737");
        spacecraftPanel.add(balCoefField, gbc);
        
        gbc.gridy = 10;
        gbc.gridx = 0;
        spacecraftPanel.add(new JLabel("Система координат:"), gbc);
        
        gbc.gridx = 1;
        gskCheckBox = new JCheckBox("ГСК", true);
        spacecraftPanel.add(gskCheckBox, gbc);
        
        // Добавляем поле для шага интегрирования
    gbc.gridy = 11;
    gbc.gridx = 0;
    spacecraftPanel.add(new JLabel("Шаг интегрирования (сек):"), gbc);
    gbc.gridx = 1;
    stepField = new JTextField("10.0"); // Значение по умолчанию
    spacecraftPanel.add(stepField, gbc);
    
    // Добавляем поле для времени прогноза
    gbc.gridy = 12;
    gbc.gridx = 0;
    spacecraftPanel.add(new JLabel("Время прогноза (сек):"), gbc);
    gbc.gridx = 1;
    timeField = new JTextField("3600.0"); // 1 час по умолчанию
    spacecraftPanel.add(timeField, gbc);
    
     // Поля для даты и времени
    gbc.gridy = 13;
    gbc.gridx = 0;
    spacecraftPanel.add(new JLabel("Дата (ГГГГ ММ ДД):"), gbc);
    gbc.gridx = 1;
    JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    yearField = new JTextField("2023", 4);
    monthField = new JTextField("5", 2);
    dayField = new JTextField("25", 2);
    datePanel.add(yearField);
    datePanel.add(new JLabel("/"));
    datePanel.add(monthField);
    datePanel.add(new JLabel("/"));
    datePanel.add(dayField);
    spacecraftPanel.add(datePanel, gbc);
    
    gbc.gridy = 14;
    gbc.gridx = 0;
    spacecraftPanel.add(new JLabel("Время (чч:мм:сс):"), gbc);
    gbc.gridx = 1;
    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    hourField = new JTextField("13", 2);
    minuteField = new JTextField("01", 2);
    secondField = new JTextField("52.568", 2);
    timePanel.add(hourField);
    timePanel.add(new JLabel(":"));
    timePanel.add(minuteField);
    timePanel.add(new JLabel(":"));
    timePanel.add(secondField);
    spacecraftPanel.add(timePanel, gbc);
    
    }
    /**
     * Инициализирует список доступных методов.
     */
    private void initializeMethods() {
        methodComboBox.addItem(new MethodWrapper("Эверхарт (-:[2,32])", calc -> new Everhart(calc, 15, 1)));
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
//                calc -> new AdaptiveDormandPrince853Integrator(calc, 1e-8, 0.1, 1e-8, 1e-8)));
            
        methodComboBox.addItem(new MethodWrapper("Метод Эверхарта (15)", 
            calc -> new Everhart(calc, 15, 6)));
    
    
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
    }

    /**
     * Создает панель результатов с вкладками для графика и текстовых данных.
     *
     * @return Панель результатов
     */
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        // Вкладка с графиком
        graphPanel = new GraphPanel();
        tabbedPane.addTab("График", new JScrollPane(graphPanel));
        
        // Вкладка с результатами
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setEditable(false);
        tabbedPane.addTab("Результаты", new JScrollPane(resultArea));
        
        // Вкладка для графиков состояния КА
        spacecraftStateGraphPanel = new SpacecraftStateGraphPanel();
        tabbedPane.addTab("Состояние КА", new JScrollPane(spacecraftStateGraphPanel));
        
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton calculateButton = new JButton("Вычислить");
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        calculateButton.addActionListener(this::calculateAction);
        
        JButton clearButton = new JButton("Очистить");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.addActionListener(e -> {
            resultArea.setText("");
            graphPanel.clearMaxError();
            graphPanel.repaint();
        });
        
        panel.add(calculateButton);
        panel.add(clearButton);
        return panel;
    }


    /**
     * Настраивает обработчики событий для компонентов интерфейса.
     */
   private void setupEventHandlers() {
        functionComboBox.addActionListener(e -> {
            updateDerivative();
            setDefaultParametersForFunction();
            
            // Показывать/скрывать вкладку параметров КА
            boolean isSpacecraft = functionComboBox.getSelectedItem() == SpacecraftFunction.SPACECRAFT;
            spacecraftPanel.setVisible(isSpacecraft);
        });
        
        x0Field.addActionListener(e -> updateDerivative());
    }

    /**
     * Устанавливает значения параметров по умолчанию в зависимости от выбранной
     * функции.
     */
   private void setDefaultParametersForFunction() {
        Object selected = functionComboBox.getSelectedItem();
        
        if (selected == StandardTestFunction.LOG) {
            minXField.setText("1");
            maxXField.setText("2");
            stepsField.setText("100");
            x0Field.setText("1");
            y0Field.setText("1");
        } else if (selected == StandardTestFunction.SIN_COS) {
            minXField.setText("0.0");
            maxXField.setText("6.28318530717959");
            stepsField.setText("180");
        }
    }

    /**
     * Обновляет значение производной при изменении функции или начальной точки.
     */
     private void updateDerivative() {
        try {
            if (functionComboBox.getSelectedItem() instanceof StandardTestFunction) {
                StandardTestFunction function = (StandardTestFunction) functionComboBox.getSelectedItem();
                double x0 = Double.parseDouble(x0Field.getText());
                double y0 = function.value(x0);
                y0Field.setText(String.format("%.6f", y0));
            }
        } catch (NumberFormatException e) {
            LOG.fatal("Производная в x0: ошибка ввода числа: {} ;", e);
        } catch (Exception e) {
            LOG.fatal("Производная в x0: ошибка вычисления: {} ;", e);
            y0Field.setText("");
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
        
        if (params.getFunction() == SpacecraftFunction.SPACECRAFT) {
            calculateSpacecraftMotion(params);
            return;
        }

        double h = (params.getMaxX() - params.getMinX()) / params.getSteps();
        setupFunctionManagers(params.getFunction());
        
        // Создаем метод интегрирования
        MethodWrapper methodWrapper = params.getMethodWrapper();
        RungeKuttaMethod method = methodWrapper.createMethod(
            (t, y, f, parm) -> {
                try {
                    f[0] = derivativeFunction.compute(t);
                } catch (Exception ex) {
                    LOG.fatal("Ошибка при расчете: '{}';", ex);
                }
                return true;
            }
        );
        
        // Начальные условия
        double[] y0Arr = { params.getFunction().value(params.getX0()) };
        
        int interpolationPoints = Integer.parseInt(interpolationPointsField.getText());
        
        List<double[]> solution;
        if (interpolationPoints > 0) {
            solution = RungeKuttaSolver.solveWithInterpolation(
                method, 
                params.getMinX(), 
                y0Arr, 
                h, 
                params.getSteps(), 
                interpolationPoints, 
                null
            );
        } else {
            solution = RungeKuttaSolver.solve(
                method, 
                params.getMinX(), 
                y0Arr, 
                        h,
                        params.getSteps(),
                        null
                );
            }

            VisualizationData data = prepareVisualizationData(
                    params,
                    solution,
                    h,
                    interpolationPoints
            );
            updateGraph(data);
            generateReport(params, solution, data, h);

        }    catch (NumberFormatException ex) {
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

    
    
        // Новый метод для расчета движения КА
    private void calculateSpacecraftMotion(CalculationParameters params) {
        try {
            
            // Получаем параметры из GUI
        double step = Double.parseDouble(stepField.getText());
        double tEnd = Double.parseDouble(timeField.getText());
        double bc = Double.parseDouble(balCoefField.getText());
        
         // Получаем дату и время из GUI
        int year = Integer.parseInt(yearField.getText());
        int month = Integer.parseInt(monthField.getText());
        int day = Integer.parseInt(dayField.getText());
        int hour = Integer.parseInt(hourField.getText());
        int minute = Integer.parseInt(minuteField.getText());
            double second = Double.parseDouble(secondField.getText());

            double h = (params.getMaxX() - params.getMinX()) / params.getSteps();

            SpacecraftMotionSolverV2 solver = new SpacecraftMotionSolverV2(15);
            solver.setBallisticCoefficient(bc);
            SpacecraftForcesCalculator calculator = solver.getCalculator();
            calculator.setDateTime(year, month, day, hour, minute, second);

        // Начальные условия
        double[] initialState = new double[6];
        initialState[0] = Double.parseDouble(initialXField.getText());
        initialState[1] = Double.parseDouble(initialYField.getText());
        initialState[2] = Double.parseDouble(initialZField.getText());
        initialState[3] = Double.parseDouble(initialVxField.getText());
        initialState[4] = Double.parseDouble(initialVyField.getText());
        initialState[5] = Double.parseDouble(initialVzField.getText());
            
            // Создаем калькулятор сил
//            SpacecraftForcesCalculator calculator = new SpacecraftForcesCalculator();
            calculator.setBallisticCoefficient(Double.parseDouble(balCoefField.getText()));
            calculator.setCurrentDate(23, 5, 2023); // Установка даты

            // Создаем метод интегрирования
            MethodWrapper methodWrapper = params.getMethodWrapper();
            RungeKuttaMethod method = methodWrapper.createMethod(calculator);

            // Начальные условия
            double[] y0 = new double[6];
            y0[0] = Double.parseDouble(initialXField.getText());
            y0[1] = Double.parseDouble(initialYField.getText());
            y0[2] = Double.parseDouble(initialZField.getText());
            y0[3] = Double.parseDouble(initialVxField.getText());
            y0[4] = Double.parseDouble(initialVyField.getText());
            y0[5] = Double.parseDouble(initialVzField.getText());

//            // Выполняем расчет
//            List<double[]> solution;
//            if (gskCheckBox.isSelected()) {
//                // Решаем в ГСК
//                solution = RungeKuttaSolver.solve(method, params.getMinX(), y0, h, params.getSteps(), null);
//            } else {
//                // Преобразуем в ИСК -> решаем -> преобразуем обратно в ГСК
//                double[] y0ISK = new double[6];
//                calculator.convertGSKtoISK(params.getMinX(), y0, y0ISK);
//                solution = RungeKuttaSolver.solve(method, params.getMinX(), y0ISK, h, params.getSteps(), null);
//
//                // Преобразуем результаты обратно в ГСК для отображения
//                List<double[]> gskSolution = new ArrayList<>();
//                for (int i = 0; i < solution.size(); i++) {
//                    double[] stateISK = solution.get(i);
//                    double[] stateGSK = new double[6];
//                    double t = params.getMinX() + i * h;
//                    calculator.convertISKtoGSK(t, stateISK, stateGSK);
//                    gskSolution.add(stateGSK);
//                }
//                solution = gskSolution;
//            }


     // Выполняем прогноз
        List<double[]> trajectory;
        if (gskCheckBox.isSelected()) {
            trajectory = solver.predictMotionInGSK(initialState, 0.0, tEnd, step);
        } else {
            trajectory = solver.predictMotionInISK(initialState, 0.0, tEnd, step);
        }

        
        
            // Подготовка данных для 3D визуализации
            VisualizationData data = prepareSpacecraftVisualizationData(trajectory, step);
            updateGraph(data);
            generateSpacecraftReport(params, trajectory, data, step);
            
            
                        // Подготавливаем данные для графика состояния КА
            List<Double> timeValues = new ArrayList<>();
            List<Double> xList = new ArrayList<>();
            List<Double> yList = new ArrayList<>();
            List<Double> zList = new ArrayList<>();
            List<Double> vxList = new ArrayList<>();
            List<Double> vyList = new ArrayList<>();
            List<Double> vzList = new ArrayList<>();

            for (int i = 0; i < trajectory.size(); i++) {
                double t = i * step;
                double[] state = trajectory.get(i);

                timeValues.add(t);
                xList.add(state[0]);
                yList.add(state[1]);
                zList.add(state[2]);
                vxList.add(state[3]);
                vyList.add(state[4]);
                vzList.add(state[5]);
            }

            // Передаем данные на график состояния КА
            spacecraftStateGraphPanel.setData(
                timeValues, 
                xList, yList, zList,
                vxList, vyList, vzList
            );
            
                    graphPanel.setSpacecraftTrajectory(xList, yList, zList);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ошибка ввода параметров КА");
        } catch (Exception ex) {
            LOG.error("Ошибка при расчете движения КА", ex);
            showErrorDialog("Ошибка расчета: " + ex.getMessage());
        }
    }

    // Метод для подготовки данных визуализации движения КА
private VisualizationData prepareSpacecraftVisualizationData(
        List<double[]> solution, 
        double step) { // используем реальный шаг

    List<Double> timeValues = new ArrayList<>();
    List<Double> xValues = new ArrayList<>();
    List<Double> yValues = new ArrayList<>();
    List<Double> zValues = new ArrayList<>();
    
    for (int i = 0; i < solution.size(); i++) {
        double t = i * step; // правильное время с реальным шагом
        double[] state = solution.get(i);
        
        timeValues.add(t);
        xValues.add(state[0]);
        yValues.add(state[1]);
        zValues.add(state[2]);
    }

    return new VisualizationData(
            timeValues,
            xValues,
            yValues,
            zValues,
            new ArrayList<>(), 
            new ArrayList<>(), 
            new ArrayList<>(),
            0, 0,
            true
    );
}


    // Метод для генерации отчета по движению КА
private void generateSpacecraftReport(
        CalculationParameters params, 
        List<double[]> solution, 
        VisualizationData data, 
        double step) { // реальный шаг
    
    StringBuilder sb = new StringBuilder();
    appendSpacecraftHeaderInfo(sb, params, step); // передаем реальный шаг
    appendSpacecraftResultsTable(sb, solution, step); // передаем реальный шаг
    
    resultArea.setText(sb.toString());
    tabbedPane.setSelectedIndex(0);
}

    
private void appendSpacecraftHeaderInfo(StringBuilder sb, CalculationParameters params, double step) {
    sb.append("Метод: ").append(params.getMethodWrapper().toString()).append("\n");
    sb.append("Функция: ").append(params.getFunction().toString()).append("\n");
    sb.append("Бал. коэффициент: ").append(balCoefField.getText()).append("\n");
    sb.append("Начальные условия:\n");
    sb.append(String.format("  Положение: [%s, %s, %s] км\n", 
        initialXField.getText(), initialYField.getText(), initialZField.getText()));
    sb.append(String.format("  Скорость: [%s, %s, %s] км/с\n", 
        initialVxField.getText(), initialVyField.getText(), initialVzField.getText()));
    sb.append("Параметры расчета:\n");
    sb.append(String.format("  Время прогноза: %.1f с\n", Double.parseDouble(timeField.getText())));
    sb.append(String.format("  Шаг интегрирования: %.6f с\n", step)); // используем реальный шаг
    sb.append("Система координат: ").append(gskCheckBox.isSelected() ? "ГСК" : "ИСК").append("\n\n");
}
 // Обновленный метод таблицы результатов
private void appendSpacecraftResultsTable(
        StringBuilder sb,
        List<double[]> solution, 
        double step) { // реальный шаг
    
    sb.append("Результаты:\n");
    sb.append(String.format("%-10s %-15s %-15s %-15s %-15s %-15s %-15s\n", 
        "t, с", "X, км", "Y, км", "Z, км", "Vx, км/с", "Vy, км/с", "Vz, км/с"));

    for (int i = 0; i < solution.size(); i++) {
        double t = i * step; // правильное время с реальным шагом
        double[] state = solution.get(i);
        
        sb.append(String.format("%-10.1f %-15.3f %-15.3f %-15.3f %-15.6f %-15.6f %-15.6f\n",
            t, state[0], state[1], state[2], state[3], state[4], state[5]));
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

        return new CalculationParameters(function, methodWrapper, x0, minX, maxX, steps);
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
    if (function == StandardTestFunction.SIN) {
        return "Math.cos(x)";
    } else if (function == StandardTestFunction.COS) {
        return "-Math.sin(x)";
    } else if (function == StandardTestFunction.EXP) {
        return "Math.exp(x)";
    } else if (function == StandardTestFunction.QUAD) {
        return "2 * x";
    } else if (function == StandardTestFunction.SIN_COS) {
        return "Math.cos(x)*Math.cos(10*x) - 10*Math.sin(x)*Math.sin(10*x)";
    } else if (function == StandardTestFunction.LOG) {
        return "1.0 / x";
    } else {
        throw new IllegalArgumentException("Неизвестная функция");
    }
}

    /**
     * Выполняет расчет методом Рунге-Кутты.
     *
     * @param params Параметры расчета
     * @param h Шаг интегрирования
     * @return Список решений
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

        return RungeKuttaSolver.solve(
                method, params.getMinX(), y0Arr, h, params.getSteps(), null
        );
    }

    /**
     * Подготавливает данные для визуализации результатов расчета.
     *
     * @param params Параметры расчета
     * @param solution Результаты расчета
     * @param h Шаг интегрирования
     * @return Объект с данными для визуализации
     */
    private VisualizationData prepareVisualizationData(
            CalculationParameters params,
            List<double[]> solution,
            double h,
            int interpolationPoints
    ) {
        List<Double> xValues = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();
        List<Double> exactValues = new ArrayList<>();
        List<Double> derivativeValues = new ArrayList<>();
        List<Double> errorValues = new ArrayList<>();

        double minX = params.getMinX();
        int totalPoints = solution.size();
        double stepH = h / (interpolationPoints + 1);

        double maxError = 0;
        double sumError = 0;

        double currentX = params.getMinX();

    for (int i = 0; i < totalPoints; i++) {
        // Рассчитываем текущее время
        double x = minX + i * stepH;
        xValues.add(x);
        
        double numY = solution.get(i)[0];
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
    
    double avgError = solution.isEmpty() ? 0 : sumError / solution.size();
    
    return new VisualizationData(
            xValues,
            xValues,
            yValues,
            new ArrayList<>(), // Empty zValues for non-spacecraft
            exactValues,
            derivativeValues,
            errorValues,
            maxError,
            avgError,
            false
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

    if (data.isSpacecraftData()) {
        // Для данных КА используем отдельные списки
        graphPanel.setSpacecraftTrajectory(
            data.getXValues(), 
            data.getYValues(), 
            data.getZValues()
        );
    } else {
        // Для обычных функций
        graphPanel.setData(
            data.getXValues(), 
            data.getYValues(), 
            data.getExactValues()
        );
    }
    
        // Всегда сбрасываем производные и ошибки
    graphPanel.setDerivativeData(new ArrayList<>());
    graphPanel.setErrorData(new ArrayList<>());
    
    graphPanel.setShowDerivative(false);
    graphPanel.setShowError(false);
    graphPanel.clearMaxError();
    
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
            // Тест преобразования координат
    SpacecraftForcesCalculator calc = new SpacecraftForcesCalculator();
    double[] gskState = {-3545479.75, 6258868.92, 0.00137977308, 1447.08453, 809.852736, 7358.78977};
    double[] iskState = new double[6];
    calc.convertGSKtoISK(0, gskState, iskState);
    System.out.println("ИСК: " + Arrays.toString(iskState));
    
    // Тест правых частей
    double[] y = {-3545479.75, 6258868.92, 0.00137977308, 1447.08453, 809.852736, 7358.78977};
    double[] f = new double[6];
    calc.compute(0, y, f, null);
    System.out.println("Правые части: " + Arrays.toString(f));
    }

    /**
     * Хранит параметры для выполнения расчета.
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
                double x0, double minX, double maxX, int steps) {
            this.function = function;
            this.methodWrapper = methodWrapper;
            this.x0 = x0;
            this.minX = minX;
            this.maxX = maxX;
            this.steps = steps;
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
    }

    /**
     * Хранит данные для визуализации результатов расчета.
     */
    private static class VisualizationData {

            private final List<Double> timeValues; // Новое поле для времени
            
        /**
         * Значения аргумента по оси X.
         */
        private final List<Double> xValues;

        /**
         * Численные значения решения (Y).
         */
        private final List<Double> yValues;
        
        
         private final List<Double> zValues;

        /**
         * Точные значения функции.
         */
        private final List<Double> exactValues;

        /**
         * Значения производной функции.
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
        
         
             private boolean isSpacecraftData;

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
    public VisualizationData( List<Double> timeValues, List<Double> xValues, List<Double> yValues, 
                           List<Double> zValues,
                           List<Double> exactValues, List<Double> derivativeValues,
                           List<Double> errorValues, double maxError, double avgError,
                           boolean isSpacecraft) {
        this.timeValues = timeValues;
    this.xValues = xValues;
    this.yValues = yValues;
    this.zValues = zValues;
    this.exactValues = exactValues;
    this.derivativeValues = derivativeValues;
    this.errorValues = errorValues;
    this.maxError = maxError;
    this.avgError = avgError;
    this.isSpacecraftData = isSpacecraft;
}

        /**
         * Возвращает значения аргумента X.
         */
        public List<Double> getXValues() {
            return xValues;
        }

            public List<Double> getZValues() {
        return zValues;
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
        
            public List<Double> getTimeValues() {
        return timeValues;
    }
    public boolean isSpacecraftData() {
        return isSpacecraftData;
    }
    }
}
