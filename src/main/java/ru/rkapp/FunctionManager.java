package ru.rkapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import javax.tools.*;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Динамически компилирует и выполняет пользовательские математические функции,
 * заданные в виде строкового выражения. Использует Java Compiler API для генерации
 * класса с методом вычисления во время выполнения.
 * 
 * <p><b>Принцип работы:</b>
 * <ol>
 *   <li>Генерирует исходный код класса {@code DynamicFunction} с методом {@code F(double x)}
 *   <li>Компилирует код в памяти без записи на диск
 *   <li>Загружает скомпилированный класс через кастомный ClassLoader
 *   <li>Создает экземпляр класса и кэширует метод вычисления
 * </ol>
 * 
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>Изолированная среда выполнения: каждый экземпляр управляет собственным классом
 *   <li>Автоматическая компиляция при инициализации
 *   <li>Потокобезопасность: экземпляр функции можно использовать многократно
 *   <li>Поддержка любых Java-выражений с одной переменной (x)
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * FunctionManager fm = new FunctionManager("Math.sin(x) + 1");
 * double result = fm.compute(0.5); // ≈ 1.4794
 * </pre>
 * 
 * <p><b>Ограничения:</b>
 * <ul>
 *   <li>Требует наличие компилятора JDK (не работает в JRE без tools.jar)
 *   <li>Выражение должно использовать только переменную 'x'
 *   <li>Поддерживает только возврат значений типа double
 * </ul>
 */
class FunctionManager {
    
    private static final Logger LOG = LogManager.getLogger(FunctionManager.class);

    // Хранилище сгенерированных байт-кодов [имя класса -> байт-код]
    private final Map<String, byte[]> classBytes = new HashMap<>();
    
    // Динамически загруженный класс функции
    private final Class<?> functionClass;
    
    // Экземпляр сгенерированного класса
    private final Object functionInstance;
    
    // Кэшированный метод вычисления F(double)
    private final Method functionMethod;

    /**
     * Инициализирует менеджер функций, компилируя переданное выражение.
     *
     * @param functionCode математическое выражение на Java (например, "x * x + 2")
     * @throws Exception при ошибках компиляции, загрузки класса или создания экземпляра
     */
    public FunctionManager(String functionCode) throws Exception {
        // Генерация исходного кода класса с методом F()
        String source = "public class DynamicFunction {\n" +
                       "    public double F(double x) {\n" +
                       "        return " + functionCode + ";\n" +  // Встраивание пользовательского кода
                       "    }\n" +
                       "}";

        // Получение системного компилятора Java (требует JDK)
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK компилятор не доступен (требуется tools.jar)");
        }
        
        // Стандартный файловый менеджер с настройками по умолчанию
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(
            null,  // Диагностический листенер (null для отключения)
            null,  // Локализация
            null   // Кодировка
        );
        
        // Кастомный файловый менеджер для перехвата байт-кода
        JavaFileManager fileManager = new MemoryJavaFileManager(stdFileManager, classBytes);

        // Создание виртуального исходного файла в памяти
        JavaFileObject javaObject = new StringJavaSource("DynamicFunction", source);

        // Запуск процесса компиляции
        JavaCompiler.CompilationTask task = compiler.getTask(
            null,    // Поток вывода (null = System.err)
            fileManager,  // Кастомный файловый менеджер
            null,    // Диагностический листенер
            null,    // Опции компилятора
            null,    // Классы для аннотационной обработки
            Arrays.asList(javaObject)  // Исходные файлы
        );
        
        // Выполнение компиляции и проверка результата
        if (!task.call()) {
            throw new RuntimeException("Ошибка компиляции функции");
        }
        fileManager.close();  // Важно: освобождение системных ресурсов

        // Загрузка класса через кастомный загрузчик
        MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        functionClass = classLoader.loadClass("DynamicFunction");
        
        // Создание экземпляра скомпилированного класса
        functionInstance = functionClass.getDeclaredConstructor().newInstance();
        
        // Получение ссылки на метод вычисления
        functionMethod = functionClass.getMethod("F", double.class);
    }

    /**
     * Вычисляет значение функции в точке x.
     *
     * @param x аргумент функции
     * @return результат вычисления
     * @throws Exception при ошибках вызова метода
     */
    public double compute(double x) throws Exception {
        // Вызов метода через рефлексию с передачей аргумента
        return (Double) functionMethod.invoke(functionInstance, x);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Внутренние классы для работы с виртуальной файловой системой
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Представление исходного кода в памяти.
     * Наследуется от SimpleJavaFileObject для работы с JavaCompiler API.
     */
    static class StringJavaSource extends SimpleJavaFileObject {
        private final String code;  // Исходный код класса

        /**
         * Создает виртуальный исходный файл.
         * 
         * @param name полное имя класса (например, "DynamicFunction")
         * @param code исходный код класса
         */
        StringJavaSource(String name, String code) {
            // Формирование URI по соглашению JavaCompiler для виртуальных файлов
            // Замена точек на слэши требуется для корректного пути
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.code = code;
        }

        /**
         * Возвращает содержимое исходного файла при запросе компилятором.
         *
         * @param ignoreEncodingErrors флаг игнорирования проблем кодировки
         * @return исходный код как CharSequence
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            // Возвращает исходный код при запросе компилятором
            return code;
        }
    }

    /**
     * Кастомный менеджер файлов для перехвата результатов компиляции.
     * Перехватывает сгенерированные байт-коды классов.
     * Сохраняет их в Map вместо записи на диск.
     * Перенаправляет запись .class файлов в память.
     */
    static class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final Map<String, byte[]> classBytes;  // Целевое хранилище байт-кодов

        /**
         * Создает менеджер, делегирующий базовые операции стандартному
         * менеджеру.
         *
         * @param fileManager стандартный файловый менеджер
         * @param classBytes целевая карта для сохранения байт-кодов [имя класса
         * → байт-код]
         */
        MemoryJavaFileManager(JavaFileManager fileManager, Map<String, byte[]> classBytes) {
            super(fileManager);
            this.classBytes = classBytes;
        }

        /**
         * Перехватывает запросы на создание выходных файлов. Для .class файлов
         * возвращает виртуальный файловый объект, остальные запросы делегирует
         * родительскому менеджеру.
         *
         * @param location местоположение файла (например, CLASS_OUTPUT)
         * @param className полное имя класса
         * @param kind тип файла (CLASS, SOURCE и т.д.)
         * @param sibling связанный файл (обычно null)
         * @return виртуальный файловый объект для записи
         */
        @Override
        public JavaFileObject getJavaFileForOutput(
                Location location,
            String className,
            JavaFileObject.Kind kind,
            FileObject sibling
        ) throws IOException {
            // Перехват только создание .class файлов
            if (kind == JavaFileObject.Kind.CLASS) {
                return new MemoryJavaFileObject(className);
            }
            // Для остальных типов файлов используем стандартное поведение
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        /**
         * Внутренний класс для обработки байт-кода класса.
         */
        private class MemoryJavaFileObject extends SimpleJavaFileObject {

            private final String name;  // Полное имя класса (с пакетами)

            /**
             * Создает виртуальный .class файл.
             *
             * @param name полное имя класса (с пакетами)
             */
            MemoryJavaFileObject(String name) {
                // Формирование URI по соглашению для .class файлов
                super(URI.create("string:///" + name.replace('.', '/') + Kind.CLASS.extension),
                        Kind.CLASS);
                this.name = name;
            }

            /**
             * Открывает поток для записи байт-кода класса. При закрытии потока
             * автоматически сохраняет данные в карту classBytes.
             *
             * @return OutputStream с переопределенным методом close()
             */
            @Override
            public OutputStream openOutputStream() {
                // Возвращаем поток, который сохраняет байты в Map при закрытии
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        // Сохранение байт-кода в карту при закрытии потока
                        // Преобразование буфера в массив байтов
                        byte[] byteArray = this.toByteArray();
                        // Сохранение в карту: имя класса → байт-код
                        classBytes.put(name, byteArray);
                    }
                };
            }
        }
    }

    /**
     * Загрузчик классов, загружающий классы напрямую из памяти. Использует
     * предварительно скомпилированные байт-коды.
     */
    static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> classBytes;  // Карта байт-кодов

        /**
         * Создает загрузчик с указанным источником байт-кодов.
         *
         * @param classBytes карта с байт-кодами [имя класса → байт-код]
         */
        MemoryClassLoader(Map<String, byte[]> classBytes) {
            this.classBytes = classBytes;
        }

        /**
         * Находит и загружает класс по его имени. Ищет байт-код в карте и
         * преобразует его в Class-объект.
         *
         * @param name полное имя класса
         * @return загруженный класс
         * @throws ClassNotFoundException если байт-код не найден
         */
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // Поиск байт-кода по имени класса
            byte[] bytes = classBytes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }
            // Преобразование байтов в Class-объект
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}