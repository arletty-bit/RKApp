package ru.rkapp.methods;

import ru.rkapp.RungeKuttaMethod;
import ru.rkapp.RightCalculator;
/**
 *
 * Реализация метода DOPRI853 из книги Хайрера-Нёрсетта-Ваннера (Hairer, Nørsett, Wanner "Solving Ordinary Differential Equations I")
 * 
 */



public class DormandPrince853Integrator extends RungeKuttaMethod {
    private static final double sqrt6 = Math.sqrt(6.0);
    private static final int STAGES = 13;
    private double[] lastDerivative; // Для реализации FSAL
    private boolean firstStep = true;

    // Коэффициенты c
    private static final double[] C = {
        (12.0 - 2.0 * sqrt6) / 135.0,
        (6.0 - sqrt6) / 45.0,
        (6.0 - sqrt6) / 30.0,
        (6.0 + sqrt6) / 30.0,
        1.0/3.0,
        1.0/4.0,
        4.0/13.0,
        127.0/195.0,
        3.0/5.0,
        6.0/7.0,
        1.0,
        1.0,
        1.0/10.0 // Для интерполяции (не используется в основном шаге)
    };

    // Основные коэффициенты b (8-го порядка)
    private static final double[] B8 = {
        104257.0/1920240.0,
        0.0,
        0.0,
        0.0,
        0.0,
        3399327.0/763840.0,
        66578432.0/35198415.0,
        -1674902723.0/288716400.0,
        54980371265625.0/176692375811392.0,
        -734375.0/4826304.0,
        171414593.0/851261400.0,
        137909.0/3084480.0,
        0.0 // Последняя стадия не используется в сумме
    };

    // Коэффициенты таблицы A
    private static final double[][] A = {
        {},
        {(6.0 - sqrt6) / 180.0},
        {(6.0 - sqrt6) / 120.0, (6.0 - sqrt6) / 40.0},
        {(462.0 + 107.0 * sqrt6) / 3000.0, 0.0, (-402.0 - 197.0 * sqrt6) / 1000.0},
        {1.0/27.0, 0.0, 0.0, (16.0 + sqrt6) / 108.0},
        {19.0/512.0, 0.0, 0.0, (118.0 + 23.0 * sqrt6) / 1024.0, -9.0/512.0},
        {13772.0/371293.0, 0.0, 0.0, (51544.0 + 4784.0 * sqrt6) / 371293.0, -5688.0/371293.0, 3072.0/371293.0},
        {58656157643.0/93983540625.0, 0.0, 0.0, (-1324889724104.0 - 318801444819.0 * sqrt6) / 626556937500.0, 96044563816.0/3480871875.0, 5682451879168.0/281950621875.0, -165125654.0/3796875.0},
        {8909899.0/18653125.0, 0.0, 0.0, (-4521408.0 - 1137963.0 * sqrt6) / 2937500.0, 96663078.0/4553125.0, 2107245056.0/137915625.0, -4913652016.0/147609375.0, -78894270.0/3880452869.0},
        {-20401265806.0/21769653311.0, 0.0, 0.0, (354216.0 + 94326.0 * sqrt6) / 112847.0, -43306765128.0/5313852383.0, -20866708358144.0/1126708119789.0, 14886003438020.0/654632330667.0, 35290686222309375.0/14152473387134411.0, -1477884375.0/485066827.0},
        {39815761.0/17514443.0, 0.0, 0.0, (-3457480.0 - 960905.0 * sqrt6) / 551636.0, -844554132.0/47026969.0, 8444996352.0/302158619.0, -2509602342.0/877790785.0, -28388795297996250.0/3199510091356783.0, 226716250.0/18341897.0, 1371316744.0/2131383595.0},
        {58656157643.0/93983540625.0, 0.0, 0.0, (-1324889724104.0 - 318801444819.0 * sqrt6) / 626556937500.0, 96044563816.0/3480871875.0, 5682451879168.0/281950621875.0, -165125654.0/3796875.0, 8909899.0/18653125.0, -20401265806.0/21769653311.0, 39815761.0/17514443.0, 0.0},
        {14005451.0/335480064.0, 0.0, 0.0, 0.0, 0.0, -59238493.0/1068277825.0, 181606767.0/758867731.0, 561292985.0/797845732.0, -1041891430.0/1371343529.0, 760417239.0/1151165299.0, 118820643.0/751138087.0, -528747749.0/2220607170.0}
    };

    // Веса для оценки ошибки (5-го порядка)
    private static final double[] E5 = {
        -116092271.0 / 8848465920.0,
        0.0,
        0.0,
        0.0,
        0.0,
        1871647.0 / 1527680.0,
        69799717.0 / 140793660.0,
        -1230164450203.0 / 739113984000.0,
        -464500805.0 / 1389975552.0,
        -1606764981773.0 / 19613062656000.0,
        137909.0 / 6168960.0
    };

    public DormandPrince853Integrator(RightCalculator calculator) {
        super(calculator);
    }

    @Override
    public boolean step(double t, double[] y, double h, double[] yNew, Object parm) {
        final int n = y.length;
        double[][] k = new double[STAGES][n];
        double[] temp = new double[n];
        boolean reuseDerivative = !firstStep && (lastDerivative != null);

        // Стадия 1: Используем последнюю производную при FSAL
        if (reuseDerivative) {
            System.arraycopy(lastDerivative, 0, k[0], 0, n);
        } else {
            if (!rightCalculator.compute(t, y, k[0], parm)) {
                return false;
            }
        }

        // Вычисляем промежуточные стадии (2-13)
        for (int i = 1; i < STAGES; i++) {
            System.arraycopy(y, 0, temp, 0, n);
            for (int j = 0; j < i; j++) {
                double aij = (j < A[i-1].length) ? A[i-1][j] : 0.0;
                if (aij != 0.0) {
                    for (int m = 0; m < n; m++) {
                        temp[m] += h * aij * k[j][m];
                    }
                }
            }
            double tStage = t + C[i-1] * h;
            if (!rightCalculator.compute(tStage, temp, k[i], parm)) {
                return false;
            }
        }

        // Вычисляем новое состояние (8-й порядок)
        System.arraycopy(y, 0, yNew, 0, n);
        for (int j = 0; j < STAGES; j++) {
            if (B8[j] != 0.0) {
                for (int m = 0; m < n; m++) {
                    yNew[m] += h * B8[j] * k[j][m];
                }
            }
        }

        // Сохраняем производную для FSAL (k[13] для следующего шага)
        lastDerivative = k[STAGES-1];
        firstStep = false;
        return true;
    }

    public double estimateError(double[][] k, double h, int n) {
        double error = 0.0;
        for (int i = 0; i < n; i++) {
            double errSum = 0.0;
            for (int j = 0; j < E5.length; j++) {
                errSum += E5[j] * k[j][i];
            }
            error += errSum * errSum;
        }
        return Math.sqrt(error / n) * Math.abs(h);
    }

    public void reset() {
        firstStep = true;
        lastDerivative = null;
    }
}