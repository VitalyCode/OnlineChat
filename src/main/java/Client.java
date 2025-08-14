import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {

    public static void main(String[]args){

        ClientStart clientStart = new ClientStart();

        clientStart.start();
    }
}
