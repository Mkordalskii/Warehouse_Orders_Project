package server;
import java.io.*;
import java.time.LocalDateTime;
public class Logger {
    private String fileName;
    public Logger(String fileName) {
        this.fileName = fileName;
    }
    public synchronized void saveLog(String msg) { //zsynchronizowana żeby kilka wątków nie zapisywało jednocześnie
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(fileName, true))) {
            printWriter.println(LocalDateTime.now() + ": " + msg);
        } catch (Exception e) { System.out.println(e + "błąd zapisu do pliku"); }
    }
}
