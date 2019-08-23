package io.anuke.mnet;

public class DCType{

    public static final String TIME_OUT = "TIME_OUT";
    public static final String CLOSED = "CLOSED";
    public static final String SERVER_SHUTDOWN = "SERVER_SHUTDOWN";


    //Закрыть сокет можно только если:
    //1. Сокет в состоянии подключён...
    //2. Сокет в состоянии подключается...

    //Если клиент
    //1. Состояние в CLOSED
    //2. Уведомить сервер
    //3. Закрыть сокет
    //4. Листнеры

    //Если сервер-subsocket
    //1. Состояние в CLOSED
    //2. Уведомить клиента
    //3. Убрать из списка
    //4. Листнеры

    //Если сервер
    //1. Всем состояние в CLOSED
    //2. Уведомить клиентов
    //3. Отчистить спиоск
    //4. Листнеры
}
