package com.cloudstorage.lesson02;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class BufferInfo {
    public static void main(String[] args) throws IOException {
        /*

        pos = 0   3                 cap = 7
        -----------------------------
        | X | X | X |   |   |   |   |
        -----------------------------
        0         3                 lim = 7

        position
        limit
        capacity
        ------
        mark



         */

        FileChannel channel = new RandomAccessFile("client" + File.separator + "1.txt", "rw").getChannel();   //RandomAccessFile позволяет и считывать и записывать
        ByteBuffer buffer = ByteBuffer.allocate(10); // размер буфера (от фонаря)

        channel.read(buffer); // считывание в буфер
        buffer.flip(); // limit = pos, pos = 0 // переход в режим чтения

        System.out.println(buffer); //выводим информацию о буфере(limit,capacity,position)
//        while (buffer.hasRemaining()) { // считываем и проверяем не дошли ли мы до конца, проверка
//            System.out.print((char) buffer.get()); // выводим содержимое файла
//        }
//        System.out.println("\n" + buffer); //выводим информацию о буфере(limit,capacity,position)


        //работа и с русским алфавитом
        byte[] byteBuf = new byte[10]; // делаем дополнительный буфер равный 10 из за того что русский алфавит занимает по 2 байта на букву
        int pos = 0; // позиция 0
        while (buffer.hasRemaining()) { // считываем и проверяем не дошли ли мы до конца, проверка
            byteBuf[pos++] = buffer.get(); // простой метод считывания
        }
        System.out.println(new String(byteBuf, StandardCharsets.UTF_8)); //выводим в консоль с учетом перевода в StandardCharsets.UTF_8

        buffer.rewind();// переводит в режим записи
    }
}
