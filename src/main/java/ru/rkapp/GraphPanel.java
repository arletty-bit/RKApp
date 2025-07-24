package ru.rkapp;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Панель для визуализации графиков численного и точного решений ОДУ.
 *
 * <p>
 * Особенности:
 * <ul>
 * <li>Автоматическое масштабирование под данные</li>
 * <li>Отображение сетки координат</li>
 * <li>Легенда с обозначениями графиков</li>
 * <li>Подписи осей координат</li>
 * <li>Антиалиасинг для сглаживания линий</li>
 * </ul>
 */
public class GraphPanel extends JPanel {

    // Данные для отображения
    private List<Double> xValues;      // Значения X
    private List<Double> yNumerical;   // Численное решение
    private List<Double> yExact;       // Точное решение
    private List<Double> errorValues;   // Значения ошибок
    private boolean showError = false;
    private static final Color ERROR_COLOR = new Color(220, 0, 100); // Малиновый

    // Константы оформления
    private static final int PAD = 80;           // Отступы от краев
    private static final int TICK_LENGTH = 5;    // Длина засечек на осях
    private static final int NUM_TICKS = 10;     // Количество делений на осях
    private static final DecimalFormat DF = new DecimalFormat("#.###"); // Формат чисел

    // Цвета
    private static final Color BG_COLOR = new Color(240, 240, 240);    // Цвет фона
    private static final Color GRID_COLOR = new Color(200, 200, 200);  // Цвет сетки
    private static final Color AXIS_COLOR = Color.BLACK;               // Цвет осей
    private static final Color NUMERICAL_COLOR = new Color(30, 120, 200); // Цвет численного решения
    private static final Color EXACT_COLOR = new Color(200, 50, 50);   // Цвет точного решения

    // Шрифты
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12); // Шрифт подписей
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);  // Шрифт заголовка
    private List<Double> yDerivative;   // Новое поле для значений производной
    private boolean showDerivative = false; // Флаг отображения производной

    /**
     * Устанавливает данные для отображения.
     *
     * @param xValues значения по оси X
     * @param yNumerical значения численного решения
     * @param yExact значения точного решения
     */
    public void setData(List<Double> xValues, List<Double> yNumerical, List<Double> yExact) {
        this.xValues = xValues;
        this.yNumerical = yNumerical;
        this.yExact = yExact;
        // Перерисовка панели
        repaint();
    }

    /**
     * Метод отрисовки компонента.
     *
     * @param g графический контекст
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Преобразование в Graphics2D для расширенных возможностей
        Graphics2D g2 = (Graphics2D) g;
        // Включение сглаживания
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Очистка фона
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Проверка наличия данных
        if (xValues == null || xValues.isEmpty()
                || yNumerical == null || yNumerical.isEmpty()
                || yExact == null || yExact.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // Поиск минимальных и максимальных значений
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        // Поиск min и max по X
        for (double x : xValues) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }

        // Поиск min и max по Y для численного решения
        for (double y : yNumerical) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Поиск min и max по Y для точного решения
        for (double y : yExact) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // Поиск min и max по Y для ошибки
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            for (double err : errorValues) {
                minY = Math.min(minY, err);
                maxY = Math.max(maxY, err);
            }
        }

        // Добавление отступов вокруг данных
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX = minX - xRange * 0.05;
        maxX = maxX + xRange * 0.05;
        minY = minY - yRange * 0.1;
        maxY = maxY + yRange * 0.1;

        // Обработка случая постоянной функции
        if (Math.abs(maxY - minY) < 1e-10) {
            minY -= 1;
            maxY += 1;
        }

        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            for (double y : yDerivative) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        // Расчет масштабов
        double xScale = (width - 2 * PAD) / (maxX - minX);
        double yScale = (height - 2 * PAD) / (maxY - minY);

        // Производная
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            g2.setColor(new Color(0, 150, 0)); // Зеленый цвет для производной
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5, 3}, 0)); // Пунктир
            drawPolyline(g2, xValues, yDerivative, minX, minY, xScale, yScale, width, height);
        }

        // Ошибка
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            g2.setColor(ERROR_COLOR);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5, 3}, 0)); // Пунктир
            drawPolyline(g2, xValues, errorValues, minX, minY, xScale, yScale, width, height);
        }

        // Отрисовка сетки
        g2.setColor(GRID_COLOR);
        // Установка пунктирного стиля
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, 0));

        // Вертикальные линии сетки
        for (int i = 0; i <= NUM_TICKS; i++) {
            double value = minX + (maxX - minX) * i / NUM_TICKS;
            int xPos = PAD + (int) (i * (width - 2 * PAD) / NUM_TICKS);
            g2.drawLine(xPos, PAD, xPos, height - PAD);
        }

        // Горизонтальные линии сетки
        for (int i = 0; i <= NUM_TICKS; i++) {
            double value = minY + (maxY - minY) * i / NUM_TICKS;
            int yPos = height - PAD - (int) (i * (height - 2 * PAD) / NUM_TICKS);
            g2.drawLine(PAD, yPos, width - PAD, yPos);
        }

        // Отрисовка осей координат
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(2)); // Толщина линий осей
        // Ось Y
        g2.drawLine(PAD, PAD, PAD, height - PAD);
        // Ось X
        g2.drawLine(PAD, height - PAD, width - PAD, height - PAD);

        // Подписи осей
        g2.setFont(LABEL_FONT);
        g2.drawString("X", width - 20, height - PAD + 20);
        g2.drawString("Y", PAD - 20, 20);

        // Заголовок графика
        g2.setFont(TITLE_FONT);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Сравнение численного и точного решения", width / 2 - 150, 30);

        // Разметка оси X
        g2.setFont(LABEL_FONT);
        g2.setColor(AXIS_COLOR);
        for (int i = 0; i <= NUM_TICKS; i++) {
            double value = minX + (maxX - minX) * i / NUM_TICKS;
            int xPos = PAD + (int) (i * (width - 2 * PAD) / NUM_TICKS);

            // Засечка на оси
            g2.drawLine(xPos, height - PAD - TICK_LENGTH, xPos, height - PAD + TICK_LENGTH);

            // Подпись значения
            String label = formatNumber(value);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, xPos - labelWidth / 2, height - PAD + 30);
        }

        // Разметка оси Y
        for (int i = 0; i <= NUM_TICKS; i++) {
            double value = minY + (maxY - minY) * i / NUM_TICKS;
            int yPos = height - PAD - (int) (i * (height - 2 * PAD) / NUM_TICKS);

            // Засечка на оси
            g2.drawLine(PAD - TICK_LENGTH, yPos, PAD + TICK_LENGTH, yPos);

            // Подпись значения
            String label = formatNumber(value);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, PAD - labelWidth - 10, yPos + 5);
        }

        // Отрисовка численного решения (Рунге-Кутты) ◄◄◄
        g2.setColor(NUMERICAL_COLOR);
        g2.setStroke(new BasicStroke(3)); // Толщина линии
        drawPolyline(g2, xValues, yNumerical, minX, minY, xScale, yScale, width, height);

        // Отрисовка точного решения (пунктиром)
        g2.setColor(EXACT_COLOR);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
        drawPolyline(g2, xValues, yExact, minX, minY, xScale, yScale, width, height);

// Отрисовка легенды
        int legendX = width - 140;
        int legendY = PAD - 65;
        int legendWidth = 140;

// Рассчитываем количество линий в легенде
        int lineCount = 2; // Всегда есть численное и точное
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            lineCount++;
        }
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            lineCount++;
        }

// Динамический расчёт высоты легенды (20px на элемент + 10px отступы)
        int legendHeight = 20 + lineCount * 20;

// Фон легенды
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(legendX - 10, legendY - 10, legendWidth, legendHeight, 15, 15);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(legendX - 10, legendY - 10, legendWidth, legendHeight, 15, 15);

// Текущая позиция Y для элементов легенды
        int currentY = legendY;

// Численное решение
        g2.setColor(NUMERICAL_COLOR);
        g2.fillRect(legendX, currentY, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawString("Численное", legendX + 20, currentY + 10);
        currentY += 20;

// Точное решение
        g2.setColor(EXACT_COLOR);
        g2.fillRect(legendX, currentY, 12, 12);
        g2.setColor(Color.BLACK);
        g2.drawString("Точное", legendX + 20, currentY + 10);
        currentY += 20;

// Производная (если нужно)
        if (showDerivative && yDerivative != null && !yDerivative.isEmpty()) {
            g2.setColor(new Color(0, 150, 0));
            g2.fillRect(legendX, currentY, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString("Производная", legendX + 20, currentY + 10);
            currentY += 20;
        }

// Ошибка (если нужно)
        if (showError && errorValues != null && !errorValues.isEmpty()) {
            g2.setColor(ERROR_COLOR);
            g2.fillRect(legendX, currentY, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString("Ошибка", legendX + 20, currentY + 10);
        }
    }

    /**
     * Форматирует число для отображения на осях. Для малых чисел использует
     * экспоненциальную форму.
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
     * @param xScale масштаб по X
     * @param yScale масштаб по Y
     * @param width ширина области рисования
     * @param height высота области рисования
     */
    private void drawPolyline(Graphics2D g2, List<Double> xValues, List<Double> yValues,
            double minX, double minY, double xScale, double yScale,
            int width, int height) {
        // Проверка наличия достаточного количества точек
        if (xValues.size() < 2) {
            return;
        }

        // Массивы для координат точек
        int[] xPoints = new int[xValues.size()];
        int[] yPoints = new int[xValues.size()];

        // Преобразование координат данных в экранные координаты
        for (int i = 0; i < xValues.size(); i++) {
            double x = xValues.get(i);
            double y = yValues.get(i);
            // Преобразование X: смещение + масштабирование
            xPoints[i] = PAD + (int) ((x - minX) * xScale);
            // Преобразование Y: инверсия (ось Y направлена вниз) + масштабирование
            yPoints[i] = height - PAD - (int) ((y - minY) * yScale);
        }

        // Отрисовка ломаной линии
        g2.drawPolyline(xPoints, yPoints, xPoints.length);
    }

    public void setDerivativeData(List<Double> yDerivative) {
        this.yDerivative = yDerivative;
        repaint();
    }

    public void setShowDerivative(boolean show) {
        this.showDerivative = show;
        repaint();
    }

    public void setErrorData(List<Double> errorValues) {
        this.errorValues = errorValues;
    }

    public void setShowError(boolean show) {
        this.showError = show;
    }
}
