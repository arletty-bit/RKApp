package ru.rkapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpacecraftStateGraphPanel extends JPanel {
    private List<Double> timeValues;
    private List<Double> xValues;
    private List<Double> yValues;
    private List<Double> zValues;
    private List<Double> vxValues;
    private List<Double> vyValues;
    private List<Double> vzValues;
    
    private JCheckBox xCheckBox, yCheckBox, zCheckBox;
    private JCheckBox vxCheckBox, vyCheckBox, vzCheckBox;
    
    private static final Color[] COLORS = {
        Color.RED, Color.GREEN, Color.BLUE,
        Color.MAGENTA, Color.CYAN, Color.ORANGE
    };

    public SpacecraftStateGraphPanel() {
        setLayout(new BorderLayout());
        
        // Панель с флажками
        JPanel checkBoxPanel = new JPanel(new GridLayout(6, 1));
        xCheckBox = new JCheckBox("X", true);
        yCheckBox = new JCheckBox("Y", true);
        zCheckBox = new JCheckBox("Z", true);
        vxCheckBox = new JCheckBox("Vx", true);
        vyCheckBox = new JCheckBox("Vy", true);
        vzCheckBox = new JCheckBox("Vz", true);
        
        checkBoxPanel.add(xCheckBox);
        checkBoxPanel.add(yCheckBox);
        checkBoxPanel.add(zCheckBox);
        checkBoxPanel.add(vxCheckBox);
        checkBoxPanel.add(vyCheckBox);
        checkBoxPanel.add(vzCheckBox);
        
        add(checkBoxPanel, BorderLayout.WEST);
        
        // Добавляем слушателей для обновления графика
        ActionListener repaintListener = e -> repaint();
        xCheckBox.addActionListener(repaintListener);
        yCheckBox.addActionListener(repaintListener);
        zCheckBox.addActionListener(repaintListener);
        vxCheckBox.addActionListener(repaintListener);
        vyCheckBox.addActionListener(repaintListener);
        vzCheckBox.addActionListener(repaintListener);
    }

    public void setData(
        List<Double> timeValues,
        List<Double> xValues, List<Double> yValues, List<Double> zValues,
        List<Double> vxValues, List<Double> vyValues, List<Double> vzValues
    ) {
        this.timeValues = timeValues;
        this.xValues = xValues;
        this.yValues = yValues;
        this.zValues = zValues;
        this.vxValues = vxValues;
        this.vyValues = vyValues;
        this.vzValues = vzValues;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        if (timeValues == null || timeValues.isEmpty()) {
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        int padding = 80;
        int labelPadding = 30;
        
        // Рассчитываем границы данных
        double minTime = Collections.min(timeValues);
        double maxTime = Collections.max(timeValues);
        
        // Собираем все выбранные ряды данных
        List<List<Double>> selectedSeries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        if (xCheckBox.isSelected()) {
            selectedSeries.add(xValues);
            labels.add("X");
        }
        if (yCheckBox.isSelected()) {
            selectedSeries.add(yValues);
            labels.add("Y");
        }
        if (zCheckBox.isSelected()) {
            selectedSeries.add(zValues);
            labels.add("Z");
        }
        if (vxCheckBox.isSelected()) {
            selectedSeries.add(vxValues);
            labels.add("Vx");
        }
        if (vyCheckBox.isSelected()) {
            selectedSeries.add(vyValues);
            labels.add("Vy");
        }
        if (vzCheckBox.isSelected()) {
            selectedSeries.add(vzValues);
            labels.add("Vz");
        }
        
        if (selectedSeries.isEmpty()) {
            return;
        }
        
        // Находим общие min и max по Y
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        
        for (List<Double> series : selectedSeries) {
            for (Double value : series) {
                if (value < minY) minY = value;
                if (value > maxY) maxY = value;
            }
        }
        
        double rangeY = maxY - minY;
        if (rangeY < 1e-10) {
            minY -= 1;
            maxY += 1;
            rangeY = 2;
        }
        
        // Рассчитываем масштабы
        double xScale = (width - 2 * padding - labelPadding) / (maxTime - minTime);
        double yScale = (height - 2 * padding - labelPadding) / rangeY;
        
        // Рисуем оси
        g2.setColor(Color.BLACK);
        g2.drawLine(padding, height - padding, width - padding, height - padding); // X
        g2.drawLine(padding, height - padding, padding, padding); // Y
        
        // Подписи осей
        g2.drawString("Время, с", width / 2, height - padding / 2);
        g2.drawString("Значение", padding / 2, height / 2);
        
        // Рисуем графики для каждого выбранного компонента
        for (int i = 0; i < selectedSeries.size(); i++) {
            List<Double> series = selectedSeries.get(i);
            g2.setColor(COLORS[i % COLORS.length]);
            
            for (int j = 0; j < timeValues.size() - 1; j++) {
                double x1 = timeValues.get(j);
                double y1 = series.get(j);
                double x2 = timeValues.get(j + 1);
                double y2 = series.get(j + 1);
                
                int xCoord1 = padding + (int) ((x1 - minTime) * xScale);
                int yCoord1 = height - padding - (int) ((y1 - minY) * yScale);
                int xCoord2 = padding + (int) ((x2 - minTime) * xScale);
                int yCoord2 = height - padding - (int) ((y2 - minY) * yScale);
                
                g2.drawLine(xCoord1, yCoord1, xCoord2, yCoord2);
            }
        }
        
        // Рисуем легенду
        int legendX = width - padding - 100;
        int legendY = padding + 20;
        
        for (int i = 0; i < labels.size(); i++) {
            g2.setColor(COLORS[i % COLORS.length]);
            g2.fillRect(legendX, legendY + i * 20, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString(labels.get(i), legendX + 15, legendY + i * 20 + 10);
        }
    }
}