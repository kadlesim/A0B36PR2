/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semestralka;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Simon
 */
public class Client {
    private ObjectInputStream socIn; //cte ze socketu
    private ObjectOutputStream socOut; //zapisuje do socketu
    private Socket socket;    
    private ClientGUI cliGUI;
    private String server, jmeno;
    private int port;
    // konstruktor
    Client(String server, String jmeno, int port, ClientGUI cliGUI){
        this.cliGUI=cliGUI;
        this.jmeno=jmeno;
        this.port=port;
        this.server=server;
    }
    // pro zacatek
    public boolean start(){
        // pokus pripojit se k serveru
        try {
            socket = new Socket(server, port);
        } catch(Exception ec){
            zobraz("Chyba pri pripojovani k serveru " + ec);
            return false;
        }
        String zpr = "Pripojeni uspesne " + socket.getInetAddress() + ":" + socket.getPort();
        zobraz(zpr);
        //vytvorime stream
        try {
            socIn  = new ObjectInputStream(socket.getInputStream());
            socOut = new ObjectOutputStream(socket.getOutputStream());
        } catch(IOException eIO){
            zobraz("Vyjimka pri vytvoreni noveho I/O streamu");
            return false;
        }
        //vytvorime vlakno na "poslouchani" serveru
        new PoslechServeru().start();
        //dame serveru vedet nase jmeno(string)
        try {
            socOut.writeObject(jmeno);
        } catch(IOException eIO) {
            zobraz("Vyjimka pri loginu " + eIO);
            odpoj();
            return false;
        }
        return true; //kdyz se povede
    }
    //zobrazi zpravu do GUI
    private void zobraz(String zpr) {
            cliGUI.append(zpr + "\n");      
    }
    //posle zpravu na server
    void posliZpravu(Zprava zpr){
        try {
            socOut.writeObject(zpr);
        } catch(IOException eIO) {
            zobraz("Vyjimka pri odesilani zpravy na server " + eIO);
        }
    }
    // kdyz se neco pokazi tak pozavirame I/O a odpjime se
    private void odpoj(){
        try {
           if(socIn != null) {
                        socIn.close();
                    }
        } catch(Exception e) {
            zobraz("Nic nenadelame");
        }
        try {
            if(socOut != null) {
                socOut.close();
            }
        }
        catch(Exception e) {
            zobraz("Nic nenadelame");
        } 
        try{
            if(socket != null) {
                        socket.close();
                    }
        } catch(Exception e) {
            zobraz("Nic nenadelame");
        }
        if(cliGUI != null) {
            cliGUI.spojeniSelhalo();
        }
    }
    
    public static void main(String[] args){
        // def hodnoty
        int portNum = 1500;
        String serverAdd = "localhost";
        String uzivJmeno = "Pan Nikdo";
        switch(args.length) {
            case 3:
                serverAdd = args[2];
	    case 2:
                try {
                    portNum = Integer.parseInt(args[1]);
                } catch(Exception e) {
	                    System.out.println("Spatne cislo portu.");
//	                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                   return;
	        }
            case 1:
                uzivJmeno = args[0];
            case 0:
                break;
            default:
//                System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
            return;
       }
       //vytvorime si klienta
        Client client = new Client(serverAdd, uzivJmeno, portNum, null);
        //otestujem pripojeni k serveru
	if(!client.start()) {
            return;
        }   
        Scanner scan = new Scanner(System.in);
        //nekoncici smicka na nacteni zpravy
        while(true) {
            System.out.print("> ");
            String msg = scan.nextLine();
            // odpojit kdyz bude zprava ODPOJIT
            if(msg.equalsIgnoreCase("ODPOJIT")) {
                client.posliZpravu(new Zprava(Zprava.ODPOJIT, ""));                
                break;
            }
            // zprava o pritomnych
            else if(msg.equalsIgnoreCase("PRITOMNI")) {
                client.posliZpravu(new Zprava(Zprava.PRITOMNI, ""));              
            }
            else {
                client.posliZpravu(new Zprava(Zprava.ZPRAVA, msg));
            }
        }
        client.odpoj();   
    }
    class PoslechServeru extends Thread {

        @Override
        public void run() {
            while(true) {
	           try {
                       String msg = (String) socIn.readObject();
                       cliGUI.append(msg);	                   
                   } catch(IOException eIO) {
	                    zobraz("Server ukoncil spojeni: " + eIO);
                        cliGUI.spojeniSelhalo();
                    break;
                   } catch(ClassNotFoundException e2) {zobraz("nenastane");}
            }
        }
    }
}
