package ru.rkapp;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Панель для визуализации графиков численного и точного решений ОДУ.
 * 
 * <p>Особенности:
 * <ul>
 * <li>Автоматическое масштабирование под данные</li>
 * <li>Отображение сетки координат</li>
 * <li>Легенда с обозначениями графиков</li>
 * <li>Подписи осей координат</li>
 * <li>Антиалиасинг для сглаживания линий</li>
 * <li>Поддержка нескольких графиков: решения, производные, ошибки</li>
 * </ul>
 */
public class GraphPanel extends JPanel {
    
    /**
     * Значения аргумента по оси X для всех графиков.
     */
    private List<Double> xValues;
    
    /**
     * Значения численного решения (метод Рунге-Кутты).
     */
    private List<Double> yNumerical;
    
    /**
     * Значения точного аналитического решения.
     */
    private List<Double> yExact;
    
    /**
     * Значения ошибок вычислений между численным и точным решениями.
     */
    private List<Double> errorValues;
    
    /**
     * Флаг отображения графика ошибки (true - показать, false - скрыть).
     */
    private boolean showError = false;
    
    /**
     * Цвет для отображения графика ошибки.
     */
    private static final Color ERROR_COLOR = new Color(255, 153, 102);
    
    /**
     * Отступы от краев панели в пикселях для области рисования.
     */
    private static final int PAD = 80;
    
    /**
     * Длина делений на осях координат в пикселях.
     */
    private static final int TICK_LENGTH = 5;
    
    /**
     * Количество делений на осях координат.
     */
    private static final int NUM_TICKS = 10;
    
    /**
     * Форматтер для числовых подписей осей.
     */
    private static final DecimalFormat DF = new DecimalFormat("#.###");
    
    /**
     * Цвет фона панели.
     */
    private static final Color BG_COLOR = new Color(240, 240, 240);
    
    /**
     * Цвет линий сетки координат.
     */
    private static final Color GRID_COLOR = new Color(200, 200, 200);
    
    /**
     * Цвет осей координат и их подписей.
     */
    private static final Color AXIS_COLOR = Color.BLACK;
    
    /**
     * Цвет линии численного решения.
     */
    private static final Color NUMERICAL_COLOR = new Color(30, 120, 200);
    
    /**
     * Цвет линии точного решения.
     */
    private static final Color EXACT_COLOR = new Color(200, 50, 50);
    
    /**
     * Шрифт для подписей осей координат.
     */
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);
    
    /**
     * Шрифт для заголовка графика.
     */
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    
    /**
     * Значения производной функции.
     */
    private List<Double> yDerivative;
    
    /**
     * Флаг отображения производной на графике (true - показать, false - скрыть).
     */
    private boolean showDerivative = false;
    
    /**
     * Максимальное значение ошибки для отображения в специальном поле.
     */
    private double maxError = Double.NaN;

    /**
     * Устанавливает основные данные для отображения графиков.
     *
     * @param xValues значения по оси X
     * @param yNumerical значения численного решения
     * @param yExact значения точного решения
     */
    public void setData(List<Double> xValues, List<Double> yNumerical, List<Double> yExact) {
        this.xValues = xValues;
        this.yNumerical = yNumerical;
        this.yExact = yExact;
        repaint();
    }

    /**
     * Устанавливает значения производной для отображения на графике.
     *
     * @param yDerivative список значений производной функции
     */
    public void setDerivativeData(List<Double> yDerivative) {
        this.yDerivative = yDerivative;
        repaint();
    }

    /**
     * Управляет отображением графика производной.
     *
     * @param show true - показать производную, false - скрыть
     */
    public void setShowDerivative(boolean show) {
        this.showDerivative = show;
        repaint();
    }

    /**
     * Устанавливает значения ошибок для отображения.
     *
     * @param errorValues список значений ошибок
     */
    public void setErrorData(List<Double> errorValues) {
        this.errorValues = errorValues;
    }

    /**
     * Управляет отображением графика ошибки.
     *
     * @param show true - показать ошибку, false - скрыть
     */
    public void setShowError(boolean show) {
        this.showError = show;
    }

    /**
     * Устанавливает максимальное значение ошибки для отображения.
     *
     * @param maxError максимальное значение ошибки
     */
    public void setMaxError(double maxError) {
        this.maxError = maxError;
    }

    /**
     * Сбрасывает отображение максимальной ошибки.
     */
    public void clearMaxError() {
        this.maxError = Double.NaN;
    }

    /**
     * Основной метод отрисовки компонента.
     *
     * @param g графический контекст для рисования
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        enableAntiAliasing(g2);
        clearBackground(g2);
        
        if (!hasValidData()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        
        // Вычисление границ данных и масштабов
        double[] bounds = calculateDataBounds();
        double minX = bounds[0];
        double maxX = bounds[1];
        double minY = bounds[2];
        double maxY = bounds[3];
        double xScale = (width - 2 * PAD) / (maxX - minX);
        double yScale = (height - 2 * PAD) / (maxY - minY);

        // Отрисовка графиков
        drawDerivative(g2, minX, minY, xScale, yScale, width, height);
        drawError(g2, minX, minY, xScale, yScale, width, height);
        drawGrid(g2, minX, maxX, minY, maxY, width, height);
        drawAxes(g2, minX, maxX, minY, maxY, width, height);
        drawTitle(g2, width);
        drawNumericalSolution(g2, minX, minY, xScale, yScale, width, height);
        drawExactSolution(g2, minX, minY, xScale, yScale, width, height);
        drawLegend(g2, width, height);
        drawMaxError(g2, width, height);
    }

    /**
     * Включает сглаживание для графики.
     *
     * @param g2 графический контекст
     */
    private void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Очищает фон панели.
     *
     * @param g2 графический контекст
     */
    private void clearBackground(Graphics2D g2) {
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Проверяет наличие валидных данных для отрисовки.
     *
     * @return true - данные доступны, false - данные отсутствуют
     */
    private boolean hasValidData() {
        return xValues != null && !xValues.isEmpty()
                && yNumerical != null && !yNumerical.isEmpty()
                && yExact != null && !yExact.isEmpty();
    }

    /**
     * Вычисляет границы данных с учетом всех видимых графиков.
     *
     * @return массив границ [minX, maxX, minY, maxY]
     */
    private double[] calculateDataBounds() {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        // Границы по X
        for (double x : xValues) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }

        // Границы по Y для численного решения
        for (double y : yNumerical) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Границы по Y для точного решения
        for (double y : yExact) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Учет ошибки при отображении
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            for (double err : errorValues) {
                minY = Math.min(minY, err);
                maxY = Math.max(maxY, err);
            }
        }

        // Учет производной при отображении
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            for (double y : yDerivative) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        // Добавление отступов
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX = minX - xRange * 0.05;
        maxX = maxX + xRange * 0.05;
        minY = minY - yRange * 0.1;
        maxY = maxY + yRange * 0.1;

        // Обработка постоянной функции
        if (Math.abs(maxY - minY) < 1e-10) {
            minY -= 1;
            maxY += 1;
        }

        return new double[]{minX, maxX, minY, maxY};
    }

    /**
     * Отрисовывает график производной.
     *
     * @param g2 графический контекст
     * @param minX минимальное значение X
     * @param minY минимальное значение Y
     * @param xScale масштаб по оси X
     * @param yScale масштаб по оси Y
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawDerivative(Graphics2D g2, double minX, double minY, 
                               double xScale, double yScale, 
                               int width, int height) {
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            g2.setColor(new Color(0, 150, 0));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5, 3}, 0));
            drawPolyline(g2, xValues, yDerivative, minX, minY, xScale, yScale, width, height);
        }
    }

    /**
     * Отрисовывает график ошибки.
     *
     * @param g2 графический контекст
     * @param minX минимальное значение X
     * @param minY минимальное значение Y
     * @param xScale масштаб по оси X
     * @param yScale масштаб по оси Y
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawError(Graphics2D g2, double minX, double minY, 
                          double xScale, double yScale, 
                          int width, int height) {
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            g2.setColor(ERROR_COLOR);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5, 3}, 0));
            drawPolyline(g2, xValues, errorValues, minX, minY, xScale, yScale, width, height);
        }
    }

    /**
     * Отрисовывает координатную сетку.
     *
     * @param g2 графический контекст
     * @param minX минимальное значение X
     * @param maxX максимальное значение X
     * @param minY минимальное значение Y
     * @param maxY максимальное значение Y
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawGrid(Graphics2D g2, double minX, double maxX, 
                         double minY, double maxY, 
                         int width, int height) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, 0));

        // Вертикальные линии
        for (int i = 0; i <= NUM_TICKS; i++) {
            int xPos = PAD + i * (width - 2 * PAD) / NUM_TICKS;
            g2.drawLine(xPos, PAD, xPos, height - PAD);
        }

        // Горизонтальные линии
        for (int i = 0; i <= NUM_TICKS; i++) {
            int yPos = height - PAD - i * (height - 2 * PAD) / NUM_TICKS;
            g2.drawLine(PAD, yPos, width - PAD, yPos);
        }
    }

    /**
     * Отрисовывает оси координат и их подписи.
     *
     * @param g2 графический контекст
     * @param minX минимальное значение X
     * @param maxX максимальное значение X
     * @param minY минимальное значение Y
     * @param maxY максимальное значение Y
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawAxes(Graphics2D g2, double minX, double maxX, 
                         double minY, double maxY, 
                         int width, int height) {
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(2));
        
        // Оси
        g2.drawLine(PAD, PAD, PAD, height - PAD); // Y
        g2.drawLine(PAD, height - PAD, width - PAD, height - PAD); // X

        // Подписи осей
        g2.setFont(LABEL_FONT);
        g2.drawString("X", width - 20, height - PAD + 20);
        g2.drawString("Y", PAD - 20, 20);

        // Разметка оси X
        for (int i = 0; i <= NUM_TICKS; i++) {
            double value = minX + (maxX - minX) * i / NUM_TICKS;
            int xPos = PAD + i * (width - 2 * PAD) / NUM_TICKS;
            
            g2.drawLine(xPos, height - PAD - TICK_LENGTH, 
                       xPos, height - PAD + TICK_LENGTH);
            
            String label = formatNumber(value);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, xPos - labelWidth / 2, height - PAD + 30);
        }

        // Разметка оси Y
        for (int i = 0; i <= NUM_TICKS; i++) {
            double value = minY + (maxY - minY) * i / NUM_TICKS;
            int yPos = height - PAD - i * (height - 2 * PAD) / NUM_TICKS;
            
            g2.drawLine(PAD - TICK_LENGTH, yPos, 
                       PAD + TICK_LENGTH, yPos);
            
            String label = formatNumber(value);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, PAD - labelWidth - 10, yPos + 5);
        }
    }

    /**
     * Отрисовывает заголовок графика.
     *
     * @param g2 графический контекст
     * @param width ширина панели
     */
    private void drawTitle(Graphics2D g2, int width) {
        g2.setFont(TITLE_FONT);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Сравнение численного и точного решения", width / 2 - 150, 30);
    }

    /**
     * Отрисовывает график численного решения.
     *
     * @param g2 графический контекст
     * @param minX минимальное значение X
     * @param minY минимальное значение Y
     * @param xScale масштаб по оси X
     * @param yScale масштаб по оси Y
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawNumericalSolution(Graphics2D g2, double minX, double minY, 
                                      double xScale, double yScale, 
                                      int width, int height) {
        g2.setColor(NUMERICAL_COLOR);
        g2.setStroke(new BasicStroke(3));
        drawPolyline(g2, xValues, yNumerical, minX, minY, xScale, yScale, width, height);
    }

    /**
     * Отрисовывает график точного решения.
     *
     * @param g2 графический контекст
     * @param minX минимальное значение X
     * @param minY минимальное значение Y
     * @param xScale масштаб по оси X
     * @param yScale масштаб по оси Y
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawExactSolution(Graphics2D g2, double minX, double minY, 
                                  double xScale, double yScale, 
                                  int width, int height) {
        g2.setColor(EXACT_COLOR);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
        drawPolyline(g2, xValues, yExact, minX, minY, xScale, yScale, width, height);
    }

    /**
     * Отрисовывает легенду графика.
     *
     * @param g2 графический контекст
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawLegend(Graphics2D g2, int width, int height) {
        int legendX = width - 140;
        int legendY = PAD - 65;
        int legendWidth = 140;
        int lineCount = 2; // Численное и точное
        
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            lineCount++;
        }
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            lineCount++;
        }
        
        int legendHeight = 20 + lineCount * 20;
        
        // Фон легенды
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(legendX - 10, legendY - 10, legendWidth, legendHeight, 15, 15);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(legendX - 10, legendY - 10, legendWidth, legendHeight, 15, 15);
        
        // Элементы легенды
        int currentY = legendY;
        drawLegendItem(g2, legendX, currentY, NUMERICAL_COLOR, "Численное");
        currentY += 20;
        drawLegendItem(g2, legendX, currentY, EXACT_COLOR, "Точное");
        currentY += 20;
        
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            drawLegendItem(g2, legendX, currentY, new Color(0, 150, 0), "Производная");
            currentY += 20;
        }
        
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            drawLegendItem(g2, legendX, currentY, ERROR_COLOR, "Ошибка");
        }
    }

    /**
     * Отрисовывает элемент легенды.
     *
     * @param g2 графический контекст
     * @param x координата X элемента
     * @param y координата Y элемента
     * @param color цвет маркера
     * @param label текст подписи
     */
    private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillRect(x, y, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawString(label, x + 20, y + 10);
    }

    /**
     * Отображает максимальную ошибку в специальном поле.
     *
     * @param g2 графический контекст
     * @param width ширина панели
     * @param height высота панели
     */
    private void drawMaxError(Graphics2D g2, int width, int height) {
        if (!Double.isNaN(maxError)) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String errorText = "Ошибка: " + String.format("%.16e", maxError);
            g2.drawString(errorText, width - 230, height - 20);
        }
    }

    /**
     * Форматирует число для отображения на осях.
     * Использует экспоненциальную форму для очень малых чисел.
     *
     * @param value форматируемое значение
     * @return строковое представление числа
     */
    private String formatNumber(double value) {
        if (Math.abs(value) < 1e-4 && value != 0) {
            return String.format("%.2e", value);
        }
        return DF.format(value);
    }

    /**
     * Рисует ломаную линию по заданным точкам.
     *
     * @param g2 графический контекст
     * @param xValues значения X
     * @param yValues значения Y
     * @param minX минимальное значение X в данных
     * @param minY минимальное значение Y в данных
     * @param xScale масштаб по оси X
     * @param yScale масштаб по оси Y
     * @param width ширина области рисования
     * @param height высота области рисования
     */
    private void drawPolyline(Graphics2D g2, List<Double> xValues, List<Double> yValues,
            double minX, double minY, double xScale, double yScale,
            int width, int height) {
        if (xValues.size() < 2) {
            return;
        }

        int[] xPoints = new int[xValues.size()];
        int[] yPoints = new int[xValues.size()];

        for (int i = 0; i < xValues.size(); i++) {
            double x = xValues.get(i);
            double y = yValues.get(i);
            xPoints[i] = PAD + (int) ((x - minX) * xScale);
            yPoints[i] = height - PAD - (int) ((y - minY) * yScale);
        }

        g2.drawPolyline(xPoints, yPoints, xPoints.length);
    }
}
