package net.fabricmc.example;
import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.Scanner;

public class ServerThread extends Thread{
    private final Socket socket;
    private final Queue<Task> q;
    private final boolean cancel[];

    public ServerThread(Socket socket, Queue<Task> q, boolean[] cancel){
        this.socket = socket;
        this.q = q;
        this.cancel = cancel;
    }

    public void run(){
        try{
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String text;
            do{
                text = reader.readLine();
                scanText(text);
            } while(!text.equals("END"));
            socket.close();
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void scanText(String text){
        // Pattern for TaskGoXZ: GoXZ {x} {z}
        System.out.println("INFO GOT SOME COMMAND: " + text);
        Scanner s = new Scanner(text);
        String type = s.next();
        switch (type){
            case "GoXZ":
                int x = s.nextInt();
                int z = s.nextInt();
                TaskGoXZ t = new TaskGoXZ(x, z);
                System.out.println("Adding task to queue: " + t.toString());
                this.q.add(t);
            case "Cancel":
                cancel[0] = true;

        }
    }

}
