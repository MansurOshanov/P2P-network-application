
import java.io.*;
import java.net.*;

public class Client {


    public static void main(String argv[]) throws Exception
    {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("localhost", 55555);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        ThreadRead t1=new ThreadRead(inFromServer, clientSocket);
        ThreadWrite t2= new ThreadWrite(inFromUser, outToServer, clientSocket);
        t1.init();
        t1.start();
        t2.init();
        t2.start();
    }

}

class ThreadRead extends Thread {
    private BufferedReader inFromServer;
    private Socket clientSocket;
    String rec;
    Thread t=null;

    public ThreadRead(BufferedReader inFromServer, Socket clientSocket){
        this.inFromServer=inFromServer;
        this.clientSocket=clientSocket;

    }

    public void run() {
        while(true){
            try {
                rec= inFromServer.readLine();
                System.out.println(rec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
    public void init(){
        if (t==null){
            t=new Thread(this);
        }
    }

}

class ThreadWrite extends Thread {
    private BufferedReader inFromUser;
    private  DataOutputStream outToServer;
    private Socket clientSocket;
    String rec;
    Thread t=null;

    public ThreadWrite(BufferedReader inFromUser, DataOutputStream outToServer, Socket clientSocket){
        this.inFromUser=inFromUser;
        this.outToServer=outToServer;
        this.clientSocket=clientSocket;

    }

    public void run() {
        while(true){
            try {
                outToServer.writeBytes(inFromUser.readLine() + '\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    public void init(){
        if (t==null){
            t=new Thread(this);
        }
    }
}



