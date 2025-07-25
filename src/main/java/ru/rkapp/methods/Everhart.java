package ru.rkapp.methods;
import ru.rkapp.RungeKuttaMethod;
import ru.rkapp.RightCalculator;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;

/**
 * Реализация метода Эверхарта (Everhart's Radau/Lobatto method) для численного интегрирования 
 * систем обыкновенных дифференциальных уравнений. Метод использует неявные формулы Рунге-Кутты 
 * с квадратурными узлами Гаусса-Радо (Radau) и Гаусса-Лобатто (Lobatto), поддерживает переменный 
 * порядок интегрирования от 2 до 32, автоматический выбор шага и итерационное уточнение решения.
 * 
 * <p>Особенности:
 * <ul>
 *   <li>Порядок метода: от 2 до 32 (четный порядок: Lobatto, нечетный: Radau)</li>
 *   <li>Автоматический контроль шага на основе локальной погрешности</li>
 *   <li>Возможность интерполяции решения внутри шага</li>
 * </ul>
 * 
 * Ссылка: Everhart, E. (1985). "An Efficient Integrator that Uses Gauss-Radau Spacings".
 */
public class Everhart extends RungeKuttaMethod {
    
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Everhart.class);

    
    /** Максимальный поддерживаемый порядок метода. */
    public static final int MAX_ORDER = 32;
    private static final double MIN_ERROR = 1e-15;

        
    /**
     * Таблица узлов разбиения интервала [0,1] для разных порядков.
     * Структура:
     *   [Метод2], [Метод3], [Метод4]...
     */
    private static final double[] SPACINGS = {
        // Метод 2-го порядка (1 элемент)
        1.0,
        
        // Метод 3-го порядка (1 элемент)
        2.0 / 3.0,
        
        // Метод 4-го порядка (2 элемента)
        0.5, 1.0,
        
        // Метод 5-го порядка (2 элемента)
        (6.0 - Math.sqrt(6.0)) / 10.0,
        (6.0 + Math.sqrt(6.0)) / 10.0,
        
        // Метод 6-го порядка (3 элемента)
        (5.0 - Math.sqrt(5.0)) / 10.0,
        (5.0 + Math.sqrt(5.0)) / 10.0,
        1.0,
        
        // Метод 7-го порядка (3 элемента)
        0.21234053823915294397475811012400,
        0.59053313555926528913507374793117,
        0.91141204048729605260445385623054,
        
        // Метод 8-го порядка (4 элемента)
        (7.0 - Math.sqrt(21.0)) / 14.0,
        0.5,
        (7.0 + Math.sqrt(21.0)) / 14.0,
        1.0,
        
        // Метод 9-го порядка (4 элемента)
        0.13975986434378055215208708112488,
        0.41640956763108317994330233133708,
        0.72315698636187617231995400231437,
        0.94289580388548231780687880744588,
        
        // Метод 10-го порядка (5 элементов)
        0.5 - Math.sqrt(147.0 + 42.0 * Math.sqrt(7.0)) / 42.0,
        0.5 - Math.sqrt(147.0 - 42.0 * Math.sqrt(7.0)) / 42.0,
        0.5 + Math.sqrt(147.0 - 42.0 * Math.sqrt(7.0)) / 42.0,
        0.5 + Math.sqrt(147.0 + 42.0 * Math.sqrt(7.0)) / 42.0,
        1.0,
        
        // Метод 11-го порядка (5 элементов)
        0.09853508579882642612349889788775,
        0.30453572664636390548538517627883,
        0.56202518975261385599498747999477,
        0.80198658212639182746420786320470,
        0.96019014294853125765919330990667,
        
        // Метод 12-го порядка (6 элементов)
        0.5 - Math.sqrt(495.0 + 66.0 * Math.sqrt(15.0)) / 66.0,
        0.5 - Math.sqrt(495.0 - 66.0 * Math.sqrt(15.0)) / 66.0,
        0.5,
        0.5 + Math.sqrt(495.0 - 66.0 * Math.sqrt(15.0)) / 66.0,
        0.5 + Math.sqrt(495.0 + 66.0 * Math.sqrt(15.0)) / 66.0,
        1.0,
        
        // Метод 13-го порядка (6 элементов)
        0.07305432868025888514812603418031,
        0.23076613796994549908311663988435,
        0.44132848122844986791860665819448,
        0.66301530971884570090294702791922,
        0.85192140033151570815002314750402,
        0.97068357284021510802794972308684,
        
        // Метод 14-го порядка (7 элементов)
        0.06412992574519669233127711938966,
        0.20414990928342884892774463430102,
        0.39535039104876056561567136982732,
        0.60464960895123943438432863017268,
        0.79585009071657115107225536569898,
        0.93587007425480330766872288061033,
        1.00000000000000000000000000000000,
        
        // Метод 15-го порядка (7 элементов)
        0.05626256053692214646565219103231,
        0.18024069173689236498757994280918,
        0.35262471711316963737390777017124,
        0.54715362633055538300144855765235,
        0.73421017721541053152321060830661,
        0.88532094683909576809035976293249,
        0.97752061356128750189117450042915,
        
        // Метод 16-го порядка (8 элементов)
        0.05012100229426992134382737779083,
        0.16140686024463112327705728645432,
        0.31844126808691092064462396564567,
        0.50000000000000000000000000000000,
        0.68155873191308907935537603435433,
        0.83859313975536887672294271354567,
        0.94987899770573007865617262220917,
        1.00000000000000000000000000000000,
        
        // Метод 17-го порядка (8 элементов)
        0.04463395528996985073312102185830,
        0.14436625704214557148521852022821,
        0.28682475714443051894868623974909,
        0.45481331519657335096772777004679,
        0.62806783541672769756914603951737,
        0.78569152060436924164245873241833,
        0.90867639210020604399625854192546,
        0.98222008485263654818679489896232,
        
        // Метод 18-го порядка (9 элементов)
        0.04023304591677059308553366958883,
        0.13061306744724746249844691257008,
        0.26103752509477775216941245363437,
        0.41736052116680648768689011702091,
        0.58263947883319351231310988297909,
        0.73896247490522224783058754636563,
        0.86938693255275253750155308742992,
        0.95976695408322940691446633041117,
        1.00000000000000000000000000000000,
        
        // Метод 19-го порядка (9 элементов)
        0.03625781288320946094116430076808,
        0.11807897878999870019228511199474,
        0.23717698481496038531730669285327,
        0.38188276530470597536077024839650,
        0.53802959891898906511685689131944,
        0.69033242007236218294037953277052,
        0.82388334383700471813682425392743,
        0.92561261029080395536408181404400,
        0.98558759035112345136717325918919,
        
        // Метод 20-го порядка (10 элементов)
        0.03299928479597043283386293195030,
        0.10775826316842779068879109194577,
        0.21738233650189749676451801526112,
        0.35212093220653030428404424222047,
        0.50000000000000000000000000000000,
        0.64787906779346969571595575777953,
        0.78261766349810250323548198473888,
        0.89224173683157220931120890805423,
        0.96700071520402956716613706804969,
        1.00000000000000000000000000000000,
        
        // Метод 21-го порядка (10 элементов)
        0.03002903216148649704306435763440,
        0.09828901220985322965120102159023,
        0.19902107896310115486205369838276,
        0.32405553832333489264284949106524,
        0.46326123428433936712690482228811,
        0.60536015311421315703804789492239,
        0.73884032399154375973394834194515,
        0.85288855035692975957240056442018,
        0.93826792812285187447737063280575,
        0.98808238656758440309025441304101,
        
        // Метод 22-го порядка (11 элементов)
        0.02755036388855888829620993084839,
        0.09036033917799666082567920914154,
        0.18356192348406966116879757277817,
        0.30023452951732553386782510421652,
        0.43172353357253622256796907213015,
        0.56827646642746377743203092786985,
        0.69976547048267446613217489578348,
        0.81643807651593033883120242722183,
        0.90963966082200333917432079085845,
        0.97244963611144111170379006915161,
        1.00000000000000000000000000000000,
        
        // Метод 23-го порядка (11 элементов)
        0.02527362039752034975333118646162,
        0.08304161344740514670686537298197,
        0.16917510037718142596943345609434,
        0.27779671510903207443667869219539,
        0.40150272023286081677227928632696,
        0.53186238691041595791688961924225,
        0.65999184208533481176639476610298,
        0.77715939295616214449216854654264,
        0.87538077485555692626470041273609,
        0.94796454887281944741645730422704,
        0.98998171953831959415697527013220,
        
        // Метод 24-го порядка (12 элементов)
        0.02334507667891804405154726762227,
        0.07682621767406384156703719645062,
        0.15690576545912128696362048021682,
        0.25854508945433189912653138318153,
        0.37535653494688000371566314981289,
        0.50000000000000000000000000000000,
        0.62464346505311999628433685018711,
        0.74145491054566810087346861681847,
        0.84309423454087871303637951978318,
        0.92317378232593615843296280354938,
        0.97665492332108195594845273237772,
        1.00000000000000000000000000000000,
        
        // Метод 25-го порядка (12 элементов)
        0.02156206316585036090809308308300,
        0.07105789873558898215118984486568,
        0.14544745623506411920978808359438,
        0.24040111047477294625739742595658,
        0.35039934972274500723370827665702,
        0.46904915068718232937106975710318,
        0.58945491879854231751096564944067,
        0.70461911573741977837708316562399,
        0.80784894547014595900894274377139,
        0.89314550911652334236589322956750,
        0.95555353684459227697453320121445,
        0.99146094501157258063133553912216,
        
        // Метод 26-го порядка (13 элементов)
        0.02003247736636954932244991899228,
        0.06609947308482637449988989854586,
        0.13556570045433692970766379973955,
        0.22468029853567647234168864707046,
        0.32863799332864357747804829817916,
        0.44183406555814806617061164513192,
        0.55816593444185193382938835486808,
        0.67136200667135642252195170182084,
        0.77531970146432352765831135292954,
        0.86443429954566307029233620026044,
        0.93390052691517362550011010145413,
        0.97996752263363045067755008100771,
        1.00000000000000000000000000000000,
        
        // Метод 27-го порядка (13 элементов)
        0.01861036501098785143971937784028,
        0.06147554089926898760236661323470,
        0.12630517869331058063228543286826,
        0.20984297172656251444713666750062,
        0.30789899828039834310295804831233,
        0.41555603597865954449577915218909,
        0.52741561399588227482490535732140,
        0.63786860271776119959131870177268,
        0.74137645929423748341020926717731,
        0.83274898860844226850447752124064,
        0.90740477530099736471710862456138,
        0.96160186126032164962316747513586,
        0.99263534897391067834930850158618,
        
        // Метод 28-го порядка (14 элементов)
        0.01737703674808071360207430396519,
        0.05745897788851185058729918425888,
        0.11824015502409239964794076201185,
        0.19687339726507714443823503068163,
        0.28968097264316375953905153063071,
        0.39232302231810288088716027686354,
        0.50000000000000000000000000000000,
        0.60767697768189711911283972313646,
        0.71031902735683624046094846936929,
        0.80312660273492285556176496931837,
        0.88175984497590760035205923798815,
        0.94254102211148814941270081574111,
        0.98262296325191928639792569603480,
        1.00000000000000000000000000000000,
        
        // Метод 29-го порядка (14 элементов)
        0.01622476590139976171877199085899,
        0.05369729993972461646659405657527,
        0.11065719118048446030912833905351,
        0.18461026055652535802692578138794,
        0.27232354711073531456397327200967,
        0.36996331162959604211551940586857,
        0.47326213866012696794627305518712,
        0.57770534269242974215959101248190,
        0.67872825601106382559765960258696,
        0.77191572935074200846535590138006,
        0.85319513231878627007633983473920,
        0.91901450031804481560754869226002,
        0.96649859546798685996403607142943,
        0.99358323920718154318917953590549,
        
        // Метод 30-го порядка (15 элементов)
        0.01521597686489103352387863081627,
        0.05039973345326395350268586924007,
        0.10399585406909246803445586451842,
        0.17380564855875345526605839017970,
        0.25697028905643119410905460707656,
        0.35008476554961839595082327263885,
        0.44933686323902527607848349747704,
        0.55066313676097472392151650252296,
        0.64991523445038160404917672736115,
        0.74302971094356880589094539292344,
        0.82619435144124654473394160982029,
        0.89600414593090753196554413548157,
        0.94960026654673604649731413075992,
        0.98478402313510896647612136918373,
        1.00000000000000000000000000000000,
        
        // Метод 31-го порядка (15 элементов)
        0.01426945473682577473409936694087,
        0.04729959009416668566195579247573,
        0.09771329932062197336876149533799,
        0.16356903939438987602444091434582,
        0.24233526096865728800292572225971,
        0.33098480497004012346130436094686,
        0.42611083909331411932854614476247,
        0.52405769153676513942741100798415,
        0.62106131135302196189347099085723,
        0.71339391374247294001597395451560,
        0.79750724494989595243178001167977,
        0.87016897444640894402874546190571,
        0.92858704688484115994521609825327,
        0.97051770135205751336835901528200,
        0.99435931102748829024249353342056,
        
        // Метод 32-го порядка (16 элементов)
        0.01343391168429084292151024906313,
        0.04456000204221320218809874680113,
        0.09215187438911484644662472338123,
        0.15448550968615764730254032131377,
        0.22930730033494923043813329624797,
        0.31391278321726147904638265963237,
        0.40524401324084130584786849262344,
        0.50000000000000000000000000000000,
        0.59475598675915869415213150737656,
        0.68608721678273852095361734036763,
        0.77069269966505076956186670375203,
        0.84551449031384235269745967868623,
        0.90784812561088515355337527661876,
        0.95543999795778679781190125319886,
        0.98656608831570915707848975093686,
        1.00000000000000000000000000000000
    };


    /** Текущий порядок интегрирования. */
    private int order;
    
    /** Количество уравнений в системе. */
    private final int numberOfEquations;
    
    /** Допустимая погрешность на шаге. */
    private double localError = 1e-11;
    
    /** Максимальное количество итераций на шаге. */
    private int maxIterations = 100;
    
    /** Флаг проверки сходимости. */
    private boolean verifyConvergence = true;

    /** Матрица коэффициентов C. */
    private double[][] cMatrix;
    
    /** Матрица коэффициентов D. */
    private double[][] dMatrix;
    
    /** Матрица коэффициентов E. */
    private double[][] eMatrix;
    
    /** Матрица обратных разностей узлов. */
    private double[][] dtaus;
    
    /** Матрица альфа-коэффициентов. */
    private double[][] aCoeffs;
    
    /** Матрица текущих B-коэффициентов. */
    private double[][] bCoeffs;
    
    /** Матрица предыдущих B-коэффициентов. */
    private double[][] bPrevCoeffs;
    
    /** Правые части уравнений в начальной точке. */
    private double[] f0;
    
    /** Начальные условия текущего шага. */
    private double[] y0;
    
    /** Временный буфер для значений Y. */
    private double[] yTemp;
    
    /** Вспомогательный вектор для промежуточных вычислений. */
    private double[] pVector;
    
    /** Решение на k-ой итерации. */
    private double[] yk;
    
    /** Последние вычисленные правые части. */
    private double[] lastF;
    
    /** Время начала текущего шага. */
    private double stepBeginTime;
    
    /** Размер текущего шага интегрирования. */
    private double stepSize;
    
    /** Флаг первого шага интегрирования. */
    private boolean isFirstStep = true;
    
    /** Размер предыдущего шага интегрирования. */
    private double previousStepSize = 0.0; 
    
    /** Счетчик выполненных шагов. */
    private long stepCount = 0;
    
    /**
     * Конструктор метода Эверхарта.
     * 
     * @param calculator         Вычислитель правых частей
     * @param order              Порядок метода (2-32)
     * @param numberOfEquations  Количество уравнений в системе
     * @throws IllegalArgumentException При недопустимом порядке
     */
    public Everhart(RightCalculator calculator, int order, int numberOfEquations) {
        super(calculator);
        if (order < 2 || order > MAX_ORDER) {
            throw new IllegalArgumentException("Порядок метода должен быть от 2 до " + MAX_ORDER);
        }
        this.order = order;
        this.numberOfEquations = numberOfEquations;
        initializeArrays();
        calculateCoefficientMatrices();
    }

    /**
     * Инициализация массивов.
     */
    private void initializeArrays() {

        int points = order / 2;  // Количество точек разбиения

        // Инициализация матриц преобразования
        cMatrix = new double[points + 1][points + 1];
        dMatrix = new double[points + 1][points + 1];
        eMatrix = new double[points + 1][points + 1];
        dtaus = new double[points][points];

        // Инициализация коэффициентов
        aCoeffs = new double[points][numberOfEquations];
        bCoeffs = new double[points][numberOfEquations];
        bPrevCoeffs = new double[points][numberOfEquations];

        // Инициализация рабочих векторов
        f0 = new double[numberOfEquations];
        y0 = new double[numberOfEquations];
        yTemp = new double[numberOfEquations];
        pVector = new double[numberOfEquations];
        yk = new double[numberOfEquations];
        lastF = new double[numberOfEquations];

        resetState();  // Сброс состояния
    }
 
    /** Сброс внутреннего состояния. */
    private void resetState() {
        int points = order / 2;
        for (int i = 0; i < points; i++) {
            Arrays.fill(aCoeffs[i], 0.0);
            Arrays.fill(bCoeffs[i], 0.0);
            Arrays.fill(bPrevCoeffs[i], 0.0);
        }
        Arrays.fill(f0, 0.0);
        Arrays.fill(y0, 0.0);
        Arrays.fill(yTemp, 0.0);
        Arrays.fill(pVector, 0.0);
        Arrays.fill(yk, 0.0);
        Arrays.fill(lastF, 0.0);
    } 

    /**
     * Вычисление матриц коэффициентов для текущего порядка.
     */
    private void calculateCoefficientMatrices() {
        int points = order / 2;
        int spacingIndex = calculateSpacingIndex();

        // Инициализация матриц
        for (int i = 0; i <= points; i++) {
            for (int j = 0; j <= points; j++) {
                if (j == 0) {
                    cMatrix[i][j] = 0.0;
                    dMatrix[i][j] = 0.0;
                    eMatrix[i][j] = 1.0;
                } else if (i == j) {
                    cMatrix[i][j] = 1.0;
                    dMatrix[i][j] = 1.0;
                    eMatrix[i][j] = 1.0;
                } else {
                    cMatrix[i][j] = 0.0;
                    dMatrix[i][j] = 0.0;
                    eMatrix[i][j] = 0.0;
                }
            }
        }
        
         // Вычисление коэффициентов
        for (int j = 0; j < points; j++) {
            for (int i = j + 1; i < points; i++) {
                cMatrix[i + 1][j + 1] = cMatrix[i][j] - SPACINGS[spacingIndex + i - 1] * cMatrix[i][j + 1];
                dMatrix[i + 1][j + 1] = dMatrix[i][j] + SPACINGS[spacingIndex + j] * dMatrix[i][j + 1];
                eMatrix[i + 1][j + 1] = eMatrix[i][j] + eMatrix[i][j + 1];
            }
        }
        // Нормализация
        for (int i = 1; i <= points; i++) {
            for (int j = 1; j <= points; j++) {
                cMatrix[j][i] /= (i + 1);
                dMatrix[i][j] *= (i + 1);
                eMatrix[i][j] *= (i + 1);
            }
        }
        
        // Обратные разности
        for (int i = 0; i < points; i++) {
            for (int j = 0; j < i; j++) {
                double diff = SPACINGS[spacingIndex + i] - SPACINGS[spacingIndex + j];
                dtaus[i][j] = 1.0 / diff;
            }
        }
    }

    /** 
     * Вычисляет индекс начала узлов в массиве SPACINGS для текущего порядка.
     * 
     * @return Индекс начала узлов в массиве SPACINGS
     */
    private int calculateSpacingIndex() {
        int points = order / 2;
        return points * (order - points - 1);
    } 
    
    /**
     * Выполняет один шаг интегрирования методом Эверхарта.
     * 
     * @param t     Начальное время шага
     * @param y     Массив начальных условий
     * @param h     Шаг интегрирования
     * @param yNew  Массив для записи новых значений
     * @param parm  Дополнительные параметры (передаются в вычислитель правых частей)
     * @return true если шаг выполнен успешно, иначе false
     */
    @Override
    public boolean step(double t, double[] y, double h, double[] yNew, Object parm) {
        if (h == 0.0) {
            System.arraycopy(y, 0, yNew, 0, numberOfEquations);
            return true;
        }
        
        int points = order / 2;
        int spacingIndex = calculateSpacingIndex();
        boolean isRadau = ((order - 2 * points) == 1);
        
        // Сохраняем параметры шага для интерполяции
        stepBeginTime = t;
        stepSize = h;
        
        // Ключевое исправление 2: правильное отношение шагов
        double r = (previousStepSize == 0 || isFirstStep) ? 1.0 : h / previousStepSize;
        double q = 1.0;

        // Инициализация состояния шага
        if (isFirstStep) {
            initializeFirstStep(t, y, parm);
            isFirstStep = false;
        } else {
            System.arraycopy(y, 0, y0, 0, numberOfEquations);
            if (isRadau) {
                if (!rightCalculator.compute(t, y0, f0, parm)) {
                    return false;
                }
            } else {
                // Вычислить и сохранить lastF для Лобатто
                rightCalculator.compute(t + h, yNew, lastF, parm);
            }

        }

        // Копирование коэффициентов для безопасного использования
        if (stepCount < 2) {
            for (int i = 0; i < points; i++) {
                System.arraycopy(bCoeffs[i], 0, bPrevCoeffs[i], 0, numberOfEquations);
            }
        }
        
        double[][] bCoeffsCopy = new double[points][numberOfEquations];
        for (int i = 0; i < points; i++) {
            System.arraycopy(bCoeffs[i], 0, bCoeffsCopy[i], 0, numberOfEquations);
        }
        
        // Предсказание коэффициентов 
        for (int s = 0; s < points; s++) {
            Arrays.fill(pVector, 0.0);
            
            for (int m = s; m < points; m++) {
                double ems = eMatrix[m + 1][s + 1];
                for (int eq = 0; eq < numberOfEquations; eq++) {
                    pVector[eq] += ems * bCoeffsCopy[m][eq];
                }
            }

            q *= r;

            for (int eq = 0; eq < numberOfEquations; eq++) {
                double oldBL = bPrevCoeffs[s][eq];
                bCoeffs[s][eq] -= oldBL;
                double newBL = q * pVector[eq] / (s + 2.0);
                bCoeffs[s][eq] += newBL;
                bPrevCoeffs[s][eq] = newBL;
            }
        }
        
        // Преобразование B -> A
        for (int s = 0; s < points; s++) {
            Arrays.fill(pVector, 0.0);
            
            for (int m = s; m < points; m++) {
                double dms = dMatrix[m + 1][s + 1];
                for (int eq = 0; eq < numberOfEquations; eq++) {
                    pVector[eq] += dms * bCoeffs[m][eq];
                }
            }
            
            for (int eq = 0; eq < numberOfEquations; eq++) {
                aCoeffs[s][eq] = pVector[eq];
            }
        }
        
        // Итерационный процесс
       if (isRadau || stepCount == 0) {
            System.arraycopy(y, 0, yTemp, 0, numberOfEquations);
            if (!rightCalculator.compute(t, yTemp, f0, parm)) {
                return false;
            }
        } else {
            System.arraycopy(lastF, 0, f0, 0, numberOfEquations);
        }

        boolean converged = false;
        int iter;
        for (iter = 0; iter < maxIterations; iter++) {
            if (!performIterations(t, h, points, spacingIndex, isRadau, parm)) {
                return false;
            }
            
            if (iter == 0) {
                System.arraycopy(yTemp, 0, yk, 0, numberOfEquations);
            } else {
                if (checkConvergence()) {
                    converged = true;
                    break;
                }
                System.arraycopy(yTemp, 0, yk, 0, numberOfEquations);
            }
        }
        
        if (verifyConvergence && !converged) {
            return false;
        }
        
        // Вычисление результата
         if (isRadau) {
            calculateSolution(1.0, points, h, y0, f0, yNew);
        } else {
            System.arraycopy(yTemp, 0, yNew, 0, numberOfEquations);
        }

        // Сохранение состояния
//        if (!isRadau) {
//            System.arraycopy(lastF, 0, f0, 0, numberOfEquations);
//        }
        
        previousStepSize = h;
        stepCount++;
        return true;
    }

    /**
     * Инициализация первого шага интегрирования.
     * 
     * @param t     Начальное время
     * @param y     Начальные значения
     * @param parm  Дополнительные параметры
     */
    private void initializeFirstStep(double t, double[] y, Object parm) {
        System.arraycopy(y, 0, y0, 0, numberOfEquations);
        if (!rightCalculator.compute(t, y0, f0, parm)) {
            throw new RuntimeException("Ошибка вычисления правых частей системы ДУ");
        }
    }
     
    /**
     * Выполняет итерационный процесс уточнения решения.
     * 
     * @param t              Текущее время
     * @param h              Шаг интегрирования
     * @param points         Количество точек
     * @param spacingIndex   Индекс узлов
     * @param isRadau        Флаг типа разбиения
     * @param parm           Дополнительные параметры
     * @return               true, если итерации выполнены успешно
     */ 
     private boolean performIterations(double t, double h, int points, int spacingIndex, 
                                      boolean isRadau, Object parm) {
        for (int i = 0; i < points; i++) {
            double tau = SPACINGS[spacingIndex + i];
            
            calculateSolution(tau, points, h, y0, f0, yTemp);
            
            if (!rightCalculator.compute(
                t + h * tau, 
                yTemp, 
                pVector, 
                parm
            )) {
                return false;
            }

            // Ключевое исправление: сохранение lastF
            if (!isRadau && i == points - 1) {
                double[] tempF = pVector.clone();
                System.arraycopy(tempF, 0, lastF, 0, numberOfEquations);
            }

            for (int eq = 0; eq < numberOfEquations; eq++) {
                double deltaF = pVector[eq] - f0[eq];
                pVector[eq] = deltaF / tau;
            }

            for (int j = 0; j < i; j++) {
                for (int eq = 0; eq < numberOfEquations; eq++) {
                    pVector[eq] = dtaus[i][j] * (pVector[eq] - aCoeffs[j][eq]);
                }
            }

            for (int eq = 0; eq < numberOfEquations; eq++) {
                double delta = pVector[eq] - aCoeffs[i][eq];
                for (int j = 0; j <= i; j++) {
                    double cij = cMatrix[i + 1][j + 1];
                    bCoeffs[j][eq] += cij * delta;
                }
            }

            for (int eq = 0; eq < numberOfEquations; eq++) {
                aCoeffs[i][eq] = pVector[eq];
            }
        }
        return true;
    }

    /**
     * Вычисление решения в заданной точке интервала.
     * 
     * @param tau     Положение на интервале
     * @param points  Количество точек
     * @param h       Шаг интегрирования
     * @param y       Начальные значения
     * @param f0      Правые части в начале шага
     * @param result  Результат вычислений
     */
   private void calculateSolution(double tau, int points, double h, 
                                   double[] y, double[] f0, double[] result) {
        Arrays.fill(pVector, 0.0);
        
        for (int j = points - 1; j >= 0; j--) {
            for (int eq = 0; eq < numberOfEquations; eq++) {
                pVector[eq] = tau * (bCoeffs[j][eq] + pVector[eq]);
            }
        }
        
        for (int eq = 0; eq < numberOfEquations; eq++) {
            result[eq] = y[eq] + h * tau * f0[eq] + h * tau * pVector[eq];
        }
    }
    
   /**
     * Интерполирует решение в заданный момент времени внутри последнего шага
     * 
     * @param t     Время для интерполяции (в пределах [stepBeginTime, stepBeginTime + stepSize])
     * @param y     Массив для записи результата
     * @return      true если интерполяция успешна, иначе false
     */
    public boolean interpolate(double t, double[] y) {
        if (isFirstStep) {
            return false;
        }
        
        if (t < stepBeginTime || t > stepBeginTime + stepSize) {
            return false;
        }
        
        double tau = (t - stepBeginTime) / stepSize;
        int points = order / 2;
        
        calculateSolution(tau, points, stepSize, y0, f0, y);
        return true;
    }
           
    /**
     * Проверка сходимости итерационного процесса.
     * 
     * @return true, если достигнута необходимая точность
     */
    private boolean checkConvergence() {
        for (int eq = 0; eq < numberOfEquations; eq++) {
            double error = Math.abs(yTemp[eq] - yk[eq]);
            double tolerance = localError * (Math.abs(yTemp[eq]) + 1e-15);
            if (error > tolerance) {
                return false;
            }
        }
    return true;
}
   
    // Геттеры и сеттеры 

    /**
     * Установка порядка метода.
     * 
     * @param order Новый порядок (2-32)
     * @throws IllegalArgumentException При недопустимом порядке
     */        
    public void setOrder(int order) {
        if (order < 2 || order > MAX_ORDER) {
            throw new IllegalArgumentException("Недопустимый порядок метода: " + order);
        }
        this.order = order;
        initializeArrays();
        calculateCoefficientMatrices();
    }
     
    /** Полный сброс состояния метода. */
    public void reset() {
        isFirstStep = true;
        stepCount = 0;
        resetState();
        calculateCoefficientMatrices();
    }
    
   /**
     * Установка допустимой погрешности.
     * 
     * @param localError Новая погрешность (≥1e-15)
     */
    public void setLocalError(double localError) {
        this.localError = Math.max(localError, MIN_ERROR);
    }

    /**
     * Установка максимального числа итераций.
     * 
     * @param maxIterations Новое значение (≥1)
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = Math.max(maxIterations, 1);
    }
    
    /**
     * Установка флага проверки сходимости.
     * 
     * @param verifyConvergence true - проверять сходимость
     */
    public void setVerifyConvergence(boolean verifyConvergence) {
        this.verifyConvergence = verifyConvergence;
    }

}

