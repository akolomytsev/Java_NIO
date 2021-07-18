package com.cloudstorage.lesson02;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class NioTelnetServer {
    //перечень команд
    private static final String LS_COMMAND = "\n\rls            view all files from current directory"; //
    private static final String MKDIR_COMMAND = "\n\rmkdir         directory creation";  //
    private static final String TOUCH_COMMAND = "\n\rtouch          creating a new file";
    private static final String CD_COMMAND = "\n\rcd            сhanging the current position";
    private static final String RM_COMMAND = "\n\rrm            deleting a file or directory";
    private static final String COPY_COMMAND = "\n\rcopy          copying a file or directory";
    private static final String CAT_COMMAND = "\n\rcat           outputting the contents of a text file";
    private static final String CHANGENIC_COMMAND = "\n\rchangenic     username change\n\r";
    Path serverRoot = Path.of("server");

    private final ByteBuffer buffer = ByteBuffer.allocate(512); // создали буфер

    private Map<SocketChannel, String> clients = new HashMap<SocketChannel, String>(); // для сохранения подключаемых клиентов (адрес IP и имя есть)


    public NioTelnetServer() throws Exception { //
        ServerSocketChannel server = ServerSocketChannel.open(); // создали серверный поток
        server.bind(new InetSocketAddress(5679)); // задали порт
        server.configureBlocking(false); // сняли блок
        Selector selector = Selector.open(); // объект работающий с ключиками каких то действий

        server.register(selector, SelectionKey.OP_ACCEPT); // регистрация всего что подсоединяется
        System.out.println("Server started"); // вывод в консоль
        while (server.isOpen()) { //ждем подключаемых
            selector.select(); //
            Set<SelectionKey> selectionKeys = selector.selectedKeys(); // выбираем ключи
            Iterator<SelectionKey> iterator = selectionKeys.iterator(); // проход по ключам итератором
            while (iterator.hasNext()) { // пройтись по итератору
                SelectionKey key = iterator.next(); //
                if (key.isAcceptable()) { // еслю ключ для приема
                    handleAccept(key, selector); // то переходим в метод
                } else if (key.isReadable()) { // если ключ для чтения
                    handleRead(key, selector); // то переходим в метод
                }
                iterator.remove(); // обнуляем
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException { // метод приема на сервер
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept(); // берем подключившийся канал и передаем ключ на прием
        channel.configureBlocking(false); // говорим что он работает в не блокирующем режиме
        System.out.println("Client connected. IP:" + channel.getRemoteAddress()); // выводим в консоль что клиент подключился
        channel.register(selector, SelectionKey.OP_READ, "skjghksdhg"); // говорим что канал работает только на чтение и вводим дополнительное сообщения для отработки в дебаге
        channel.write(ByteBuffer.wrap("Hello user!\n\r To change the name dial changename\n\r".getBytes(StandardCharsets.UTF_8))); // отправляем приветственное сообщение пользователю
        channel.write(ByteBuffer.wrap("Enter --help for support info\n\r".getBytes(StandardCharsets.UTF_8))); // подсказка про --help, ввел и список команд выведен
    }


    private void handleRead(SelectionKey key, Selector selector) throws IOException { // метод на чтение
        SocketChannel channel = (SocketChannel) key.channel(); // берем канал
        SocketAddress client = channel.getRemoteAddress(); // который к нам подключен
        int readBytes = channel.read(buffer); // считываем буфер

        clients.putIfAbsent(channel, "user");

        if (readBytes < 0) { // если буфер меньше 0
            channel.close(); // то закрываем канал
            return; // выход
        } else if (readBytes == 0) { // если буфер равно 0 то просто возвращаемя
            return; // выход
        }

        buffer.flip(); // переход на чтение
        StringBuilder sb = new StringBuilder(); // собираем всю полученную информацию
        while (buffer.hasRemaining()) { // считываем в цикле
            sb.append((char) buffer.get()); // по одному символу
        }
        buffer.clear(); // чистим буфер

        // TODO: 21.06.2021
        // touch (filename) - создание файла
        // mkdir (dirname) - создание директории
        // cd (path | ~ | ..) - изменение текущего положения
        // rm (filename / dirname) - удаление файла / директории
        // copy (src) (target) - копирование файлов / директории
        // cat (filename) - вывод содержимого текстового файла
        // changenick (nickname) - изменение имени пользователя

        // добавить имя клиента

        if (key.isValid()) { // проверяем на валидность
            String name = client.toString();
            String command = sb.toString()// по факту просто преобразуем в строку
                    .replace("\n", "") // если есть добавленные консолью нивидимые файлы то заменяем на пустоту
                    .replace("\r", ""); // то же самое
            String[] arr = command.split(" ", 3);

            int arrLength = arr.length;
            if (arrLength < 2) {
                arr = new String[]{arr[0], ""};


            }

            if ("--help".equals(command)) { // если команда --help то выводим имеющиеся команды
                sendMessage(LS_COMMAND, selector, client); //
                sendMessage(MKDIR_COMMAND, selector, client); //
                sendMessage(TOUCH_COMMAND, selector, client); //
                sendMessage(CD_COMMAND, selector, client); //
                sendMessage(RM_COMMAND, selector, client); //
                sendMessage(COPY_COMMAND, selector, client); //
                sendMessage(CAT_COMMAND, selector, client); //
                sendMessage(CHANGENIC_COMMAND, selector, client); //
            } else if ("ls".equals(arr[0])) {
                sendMessage(getFilesList().concat("\n\r"), selector, client); // выводим содержимое текущей директории в столбик
            } else if ("mkdir".equals(arr[0])) {
                sendMessage(createDirectory(arr[1], serverRoot).concat("\n\r"), selector, client);// создаем новую директорию
            } else if ("touch".equals(arr[0])) {
                sendMessage(createFile(arr[1], serverRoot).concat("\n\r"), selector, client);// создаем файл
            } else if ("cd".equals(arr[0])) {
                sendMessage(changeDirectory(arr[1]).concat("\n\r"), selector, client);// переход по директориям
            } else if ("rm".equals(arr[0])) {
                sendMessage(deleteObject(arr[1], serverRoot).concat("\n\r"), selector, client);// удаляем файл или директорию
            } else if ("copy".equals(arr[0])) {
                if (arrLength < 3) {
                    arr = new String[]{arr[0], "", ""};
                }
                sendMessage(copying(arr[1], arr[2], serverRoot).concat("\n\r"), selector, client);// копируем
            } else if ("cat".equals(arr[0])) {
                sendMessage(textOutput(arr[1], serverRoot).concat("\n\r"), selector, client);// выводим содержимое выбранного файла
            } else if ("changenic".equals(arr[0])) {
                sendMessage(NikName(arr[1]).concat("\n\r"), selector, client);// переименовываем клиента
            }
        }
    }

    //
    private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException { //
        for (SelectionKey key : selector.keys()) { // нужно пройтись по ключам команд
            if (key.isValid() && key.channel() instanceof SocketChannel) { // проверить на валидность и кто подключился
                if (((SocketChannel) key.channel()).getRemoteAddress().equals(client)) { // проверить тот ли это клиент, конкретный
                    ((SocketChannel) key.channel()).write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8))); //отправляем сообщение
                }
            }
        }
    }

    //метод для вывода содержимого директории
    private String getFilesList() { // запихиваем все файлы в лист для отправки клиенту
        String[] servers = new File(String.valueOf(Path.of(String.valueOf(serverRoot)))).list(); //
        return String.join(" ", servers);
        //
    }

    // создание директории или директрорий
    private String createDirectory(String dirName, Path serverRoot) throws IOException {
        if (!Files.exists(Path.of(String.valueOf(serverRoot), dirName))) {
            Files.createDirectories(Path.of(String.valueOf(serverRoot), dirName));
            return "change directory " + dirName;
        } else {
            return "Wrong directory or path name";
        }
    }

    //создание файла
    private String createFile(String fileName, Path serverRoot) throws IOException {
        if (!Files.exists(Path.of(String.valueOf(serverRoot), fileName))) {
            Files.createFile(Path.of(String.valueOf(serverRoot), fileName));
            return fileName;
        } else {
            return "Wrong file name";
        }
    }

    //переходы по директроиям
    private String changeDirectory(String s) {
        if (Files.exists(Path.of(String.valueOf(serverRoot), s))) {
            if (s.equals("..") && serverRoot.equals(Path.of("server"))) {
                return "you cannot go above the directory " + serverRoot;
            } else if (s.equals("..")) {
                serverRoot = serverRoot.getParent();
                return "change directory " + serverRoot;
            } else if (s.equals(".")) {
                serverRoot = Path.of("server");
                return "change directory " + serverRoot;
            } else {
                serverRoot = Path.of(String.valueOf(serverRoot), s);
                return "change directory " + serverRoot;
            }
        } else {
            return "Directory " + s + " doesn't exists\n";
        }
    }

    //удаление директории
    private String deleteObject(String name, Path serverRoot) throws IOException {
        if (Files.exists(Path.of(String.valueOf(serverRoot), name))) {
            Files.delete(Path.of(String.valueOf(serverRoot), name));
            return "Delete: " + name;
        } else {
            return "Sorry, some bullshit has happened";

        }
    }

    //копирование
    private String copying(String path1, String path2, Path serverRoot) throws IOException {
        if (Files.exists(Path.of(String.valueOf(serverRoot), path1))) {
            Files.copy(Path.of("server" + File.separator + path1), Path.of("server" + File.separator + path2), StandardCopyOption.REPLACE_EXISTING);
            return "Copy: " + path1;
        } else {
            return "Sorry, some bullshit has happened";
        }
    }

    //вывод текстового файла клиенту
    private String textOutput(String file, Path serverRoot) throws IOException {
        if (Files.exists(Path.of(String.valueOf(serverRoot), file))) {
            String d = Files.readString(Path.of(String.valueOf(serverRoot), file));
            return String.join(" ", d);
        } else {
            return "Sorry, some bullshit has happened";
        }
    }

    // смена имени клиента
    private String NikName(String name) {
        if (name.equals("")) {
            return "your nickname" + clients.values();
        } else {
            clients.replaceAll((k, v) -> name);
            return "New nickname " + name;
        }
    }

    public static void main(String[] args) throws Exception {
        new NioTelnetServer();
    }
}
