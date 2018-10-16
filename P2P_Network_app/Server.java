
//References : https://www.tutorialspoint.com/javaexamples/net_multisoc.htm
//              https://stackoverflow.com/questions/29339933/read-and-write-files-in-java-using-separate-threads?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {
    public static ConcurrentHashMap<String, String> connectedClients = new ConcurrentHashMap<String, String>();
    public static String[] groups={"group1", "group2", "group3"};
    public static ConcurrentHashMap<String, Socket> socketConcurrentHashMap = new ConcurrentHashMap<String, Socket>();

    Socket csocket;
    Server(Socket csocket) {
        this.csocket = csocket;
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ssock = new ServerSocket(55555);
        System.out.println("Listening");

        while (true) {
            Socket sock = ssock.accept();
            System.out.println("Client connected");
            new Thread(new Server(sock)).start();
        }
    }
    public void run() {
        try {
            String clientName="";
            String clientSentence;
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(csocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(csocket.getOutputStream());
            outToClient.writeBytes("Success| You joined the server\n\r");
            while (true){
                clientSentence = inFromClient.readLine();
                String[] welcomeString = clientSentence.split(" ");
                if (clientSentence.equals(null)){
                    outToClient.writeBytes("Error| Enter the username: Format: server hello <username>\n\r");
                    continue;
                } else  if (welcomeString[0].equals("server") && welcomeString[1].equals("hello") && welcomeString.length==3){
                    synchronized (this) {
                        if (connectedClients.containsKey(welcomeString[2])){
                            outToClient.writeBytes("Error|This username is taken, try another one\n\r");
                            continue;
                        }
                        connectedClients.put(welcomeString[2], "");
                    }
                    outToClient.writeBytes("Success| username added\n\r");
                    clientName=clientName+welcomeString[2];
                    socketConcurrentHashMap.put(clientName, csocket);
                    break;

                } else {
                    outToClient.writeBytes("Error| Wrong format. Follow: server hello <username>\n\r");
                }
            }
            while(true) {
                clientSentence = inFromClient.readLine();
                String[] requestString = clientSentence.split(" ");
                if (clientSentence.equals(null)) {
                    outToClient.writeBytes("Error| No command was entered\n\r");
                    continue;
                } else
                if (requestString[0].equals("server") && requestString[1].equals("groupslist") && requestString.length == 2) {
                    String group1 = "group1: ";
                    String group2 = " | group2: ";
                    String group3 = " | group3: ";
                    for (Object o : connectedClients.keySet()) {
                        if (connectedClients.get(o).equals("group1")) {
                            group1 = group1 + o + " ";
                        }
                    }
                    for (Object o : connectedClients.keySet()) {
                        if (connectedClients.get(o).equals("group2")) {
                            group2 = group2 + o + " ";
                        }
                    }
                    for (Object o : connectedClients.keySet()) {
                        if (connectedClients.get(o).equals("group3")) {
                            group3 = group3 + o + " ";
                        }
                    }
                    String grouplist = group1 + group2 + group3;
                    outToClient.writeBytes("Success| "+grouplist+"\n\r");
                    continue;


                } else if (requestString[0].equals("server") && requestString[1].equals("join") && requestString.length == 3) {
                    if (requestString[2].equals("group1") || requestString[2].equals("group2") || requestString[2].equals("group3")) {
                        synchronized (this) {
                            connectedClients.put(clientName, requestString[2]);
                            outToClient.writeBytes("Success| You entered the "+requestString[2]+"\n\r");
                            continue;
                        }
                    } else {
                        outToClient.writeBytes("Error| Such group does not exist, try again\n\r");
                        continue;
                    }

                } else if (requestString[0].equals("server") && requestString[1].equals("members") && requestString.length==2){
                    synchronized (this) {
                        String members="";
                        String currentGroup=connectedClients.get(clientName);
                        if (!currentGroup.equals("")){
                            for (Object o : connectedClients.keySet()) {
                                if (connectedClients.get(o).equals(currentGroup)) {
                                    members=members+o+", ";
                                }
                            }
                            outToClient.writeBytes("Success| Members of the current group: "+members+"\n\r");
                            continue;
                        }   else {
                            outToClient.writeBytes("Error| You are not in any group\n\r");
                            continue;
                        }


                    }
                } else if (requestString[0].equals("server") && requestString[1].equals("leave") && requestString.length==2){
                    synchronized (this) {
                        if (!connectedClients.get(clientName).equals("")){
                            connectedClients.put(clientName, "");
                            outToClient.writeBytes("Success| You left the group"+connectedClients.get(clientName)+"\n\r");
                            continue;
                        }
                        else {
                            outToClient.writeBytes("Error| You are currently not in any group, so cannot leave\n\r");
                            continue;
                        }

                    }
                } else if (requestString[0].equals("toall")){
                    synchronized (this) {
                        if (!connectedClients.get(clientName).equals("")){
                            String msg="";
                            for (int i=1; i<requestString.length; i++){
                                msg=msg+" "+requestString[i];
                            }
                            groupMessage(msg, connectedClients.get(clientName), clientName);
                            outToClient.writeBytes("Success| Message sent to everyone in the current group\n\r");
                            continue;

                        }
                        else {
                            outToClient.writeBytes("Error| You cannot write messages, cause you are not in any group\n\r");
                            continue;
                        }

                    }
                }
                if (requestString[0].equals("server") && requestString[1].equals("exit") && requestString.length==2){
                    synchronized (this){
                        connectedClients.remove(clientName);
                        outToClient.writeBytes("Success| Exiting the server...\n\r");
                    }
                    break;
                }

                for (Object o : connectedClients.keySet()) {
                    if (connectedClients.get(o).equals(connectedClients.get(clientName))) {
                        if (requestString[0].equals(o)){
                            synchronized (this) {
                                if (connectedClients.get(clientName).equals(connectedClients.get(requestString[0]))){
                                    String msg="";
                                    for (int i=1; i<requestString.length; i++){
                                        msg=msg+" "+requestString[i];
                                    }
                                    directMessage(msg, requestString[0], clientName);
                                    outToClient.writeBytes("Success| Message sent to "+requestString[0]+"\n\r");
                                    continue;


                                }
                                else {
                                    outToClient.writeBytes("Error| You cannot write messages, cause you are not in any group\n\r");

                                }

                            }

                        }

                    }

                }


            }

            csocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }

    }
    public void groupMessage(String msg, String groupName, String sender){
        for (Object o : connectedClients.keySet()) {
            if (connectedClients.get(o).equals(groupName)) {
                String receiver=(String)o;
                Socket sock = socketConcurrentHashMap.get(receiver);
                try {
                    DataOutputStream outToClient = new DataOutputStream(sock.getOutputStream());
                    outToClient.writeBytes("Public message from "+sender+":"+msg+"\n\r");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void directMessage(String msg, String receiver, String sender){

        Socket sock=socketConcurrentHashMap.get(receiver);
        try {
            DataOutputStream outToClient = new DataOutputStream(sock.getOutputStream());
            outToClient.writeBytes("Private message from "+sender+":"+msg+"\n\r");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}