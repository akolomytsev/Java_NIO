package com.cloudstorage.lesson02;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class IOUtils {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path testPath = Path.of("."); // Класс основной, прописывает путь к файлу в корень проекта (root)
//        System.out.println(testPath); // выводим в консоль то место где лежит наш проект
//        System.out.println(testPath.toAbsolutePath()); // выводим обсолютный путь

        String root = "client"; //
        Path path = Path.of(""); // создание обьекта в корне проекта
        Path path1 = Paths.get("client" + File.separator + "1.txt"); // второй способ прописывания пути к файлу, сложно если путь длинный
        Path path2 = Paths.get("client" + File.separator, "dir1", "dir2", "2.txt"); // третий способ,
        Path path3 = Path.of(root, "dir1", "dir2", "3.txt"); // четвертый способ, просто от рута
        Path path4 = Path.of(root); // путь к корневой папке (клиент)

//        path.toAbsolutePath().iterator().forEachRemaining(System.out::println); // прописываем все дерриктории нашего пути

        WatchService service = FileSystems.getDefault().newWatchService(); // аналог слушателя (создание сервиса слушателя)

        path4.register(service, // указали события для слушателя и указали путь по которому слушать
                StandardWatchEventKinds.ENTRY_CREATE, // создание файла
                StandardWatchEventKinds.ENTRY_DELETE, // удаление
                StandardWatchEventKinds.ENTRY_MODIFY); // изменение
//

        // немного не корректное отслеживание событий, все через один поток
//        WatchKey key; // пробуем воспользоваться слушателем
//        String notification = "Event type: %s. File: %s\n"; // сообщение в консоль что бы видно что произошло
//        while ((key = service.take()) != null) { // пробуем пробежать по командам
//            for (WatchEvent event : key.pollEvents()) { // пройдемся по всем событиям
//                System.out.printf(notification, event.kind(), event.context()); //выводим в консоль уведомления по операции и
//            }
//            key.reset(); // восстановили ключь что бы он мог использоваться дальше
//        }


        // более корректный метод отслеживания событий
//        new Thread(() -> { //создаем новый поток
//            String notification = "Event type: %s. File: %s\n"; // сообщение в консоль, что произощло
//            while (true) {
//                try {
//                    WatchKey key = service.take(); // делаем проверку команд по ключам (отбираем ключи)
//                    if (key.isValid()) { //проверяем ключ
//                        List<WatchEvent<?>> watchEvents = key.pollEvents(); //
//                        for (WatchEvent<?> event : watchEvents) { // пройдемся по всем событиям
//                            System.out.printf(notification, event.kind(), event.context()); // выводим в консоль
//                        }
//                    }
//
//                    key.reset(); // возвратили ключь для дальнейшего использования
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        // проверка на существование
        System.out.println("1.txt exists: " + Files.exists(path1)); // проверяем есть ли такой файл по такому пути

        // создание файла
        Path path5 = Path.of("client", "dir1", "dir2", "4.txt"); // прописываем путь
        if (!Files.exists(path5)) {//проверяем что нет такого пути и файла
            Files.createFile(path5);//создаем
        }

        // перемещение файла
//        Path path6 = Files.move(path5, Path.of("client", "dir1", "dir2", "5.txt")); // прописываем откуда копируем (path5) и куда (все после ,)
        Path path6 = Path.of("client", "dir1", "dir2", "5.txt"); // создали путь для следующего примера

        // копирование ()
        Path path7 = Files.copy(path6, Path.of("client", "dir1", "dir2", "6.txt"), StandardCopyOption.REPLACE_EXISTING); //StandardCopyOption.REPLACE_EXISTING нужен для простого копирования

        // запись в файл
        Files.writeString(path7, "\n\nNew String\n", StandardOpenOption.APPEND); // StandardOpenOption.APPEND настройка как копировать (с удалением или вставить в конец или еще как то)

        // удаление
//        Files.delete(path2);

        // создание директорий и поддиректорий
        Files.createDirectories(Path.of("client", "dir0", "dir4", "dir5"));  // создает полную прописанную структуру директорий

        // обход дерева файлов и директорий (можно организовать поиск файла)
//        Files.walkFileTree(path4, new FileVisitor<Path>() {
//            @Override
//            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {  // метод отрабатывает перед посещением директории
//                System.out.println("pre - " + dir.getFileName());   //выводим в консоль
//                return FileVisitResult.CONTINUE;  // дальнейшие действия (продолжаем, )
//            }
//
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {  // метод отрабатывает когда мы посещаем какой то файл
//                System.out.println("visit file - " + file.getFileName());
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException { // метод отрабатывает когда посещение файла закончилось ошибкой
//                System.out.println("visit failed file - " + file.getFileName());
//                return FileVisitResult.TERMINATE;
//            }
//
//            @Override
//            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {// метод отрабатывает когда мы прошли какую нибудь директорию
//                System.out.println("post - " + dir.getFileName());
//                return FileVisitResult.CONTINUE;
//            }
//        });

        // поиск файла
        Files.walkFileTree(path4, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {   //
                if ("5.txt".equals(file.getFileName().toString())) {  // если есть файл 5.txt то
                    System.out.println(file.getFileName() + " is founded. Path: " + file.toAbsolutePath());   // выводим в консоль результат с именем и путем
                    return FileVisitResult.CONTINUE;   // можно прописать, ищем все или выходим как найдем первый вариант
                }
                return FileVisitResult.CONTINUE;  //выходим и продолжаем работу
            }
        });
    }
}
