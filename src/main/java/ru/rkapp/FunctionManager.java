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

class StringJavaSource extends SimpleJavaFileObject {
    private final String code;

    StringJavaSource(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
              Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}

class FunctionManager {

    private final Map<String, byte[]> classBytes = new HashMap<>();
    private final Class<?> functionClass;
    private final Object functionInstance;
    private final Method functionMethod;

    public FunctionManager(String functionCode) throws Exception {
        // Генерация исходного кода
String source = "public class DynamicFunction {\n" +
               "    public double F(double x) {\n" +
               "        return " + functionCode + ";\n" +
               "    }\n" +
               "}";

        // Компиляция в памяти
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
        JavaFileManager fileManager = new MemoryJavaFileManager(stdFileManager, classBytes);

        // Создание объекта с исходным кодом
        JavaFileObject javaObject = new StringJavaSource("DynamicFunction", source);
//        JavaFileObject javaObject = new SimpleJavaFileObject(URI.create("string:///DynamicFunction.java"), JavaFileObject.Kind.SOURCE) {
//            @Override
//            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
//                return source;
//            }
//        };

        // Компиляция
        compiler.getTask(null, fileManager, null, null, null, Arrays.asList(javaObject)).call();
        fileManager.close();

        // Загрузка класса через кастомный ClassLoader
        MemoryClassLoader classLoader = new MemoryClassLoader(classBytes);
        functionClass = classLoader.loadClass("DynamicFunction");
        functionInstance = functionClass.getDeclaredConstructor().newInstance();
        functionMethod = functionClass.getMethod("F", double.class);
    }

    public double compute(double x) throws Exception {
        return (Double) functionMethod.invoke(functionInstance, x);
    }

    // Кастомный FileManager для перехвата байт-кода
    static class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, byte[]> classBytes;

        MemoryJavaFileManager(JavaFileManager fileManager, Map<String, byte[]> classBytes) {
            super(fileManager);
            this.classBytes = classBytes;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                return new MemoryJavaFileObject(className);
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        private class MemoryJavaFileObject extends SimpleJavaFileObject {
            private final String name;

            MemoryJavaFileObject(String name) {
                super(URI.create("string:///" + name.replace('.', '/') + ".class"), Kind.CLASS);
                this.name = name;
            }

            @Override
            public OutputStream openOutputStream() {
                return new OutputStream() {
                    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    @Override
                    public void write(int b) {
                        baos.write(b);
                    }

                    @Override
                    public void close() {
                        classBytes.put(name, baos.toByteArray());
                    }
                };
            }
        }
    }

    // Кастомный ClassLoader для загрузки классов из памяти
    static class MemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytes;

        MemoryClassLoader(Map<String, byte[]> classBytes) {
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}