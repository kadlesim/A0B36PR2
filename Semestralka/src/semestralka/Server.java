/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semestralka;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Simon
 */
public class Server {    

    private static int uniqueId;
    // ArrayList pro zapamatovani klientu
    private ArrayList<VlaknoClient> arrl;
    private ServerGUI serGUI;
    private SimpleDateFormat sdf;
    //cislo pornu na kterem budem poslouchat
    private int port;
    //boolean na vypnuti
    private boolean pokracuj;
    
    public Server(int port, ServerGUI serGUI){
        this.port = port;
        this.serGUI = serGUI;
        sdf = new SimpleDateFormat("HH:mm:ss");
        arrl = new ArrayList<VlaknoClient>();
    }
    
    public void start(){
        pokracuj = true;
        // vytvorime server socket a cekame na pripojeni
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //smicka cekajici na pripojeni
            while(pokracuj){
                zobraz("Server ceka na zakazniky na portu "+ port);
                Socket socket = serverSocket.accept();
                if(!pokracuj){ break; }
                VlaknoClient v = new VlaknoClient(socket); //vytvorime vlakno a dame ho do seznamu
                arrl.add(v);
                v.start();                
            } 
            try {
                 serverSocket.close();
                 for(int i = 0; i < arrl.size(); ++i) {
                        VlaknoClient vc = arrl.get(i);
	                    try {
                                vc.socIn.close();
                                vc.socOut.close();
                                vc.socket.close();
	                    }
	                    catch(IOException ioE) {}
                 }
            }catch(Exception e){
                zobraz("Vyjimka zavira sever i klienta " + e);
            }
        }catch (IOException e) {
            String zpr = sdf.format(new Date()) + "Vyjimka na mpvem ServerSocketu: " + e + "\n";
            zobraz(zpr);
	}        
    }
    
    protected void stop() {
        pokracuj = false;
        try {
            new Socket("localhost", port);
        } catch(Exception e) {}
    }
    
    private void zobraz(String zpr){
        String time = sdf.format(new Date()) + " " + zpr; 
        serGUI.appendEvent(time + "\n");
    }
    
    //posleme zpravu vsem klientum
    public synchronized void vysilani(String zprava){
        String time = sdf.format(new Date());
        String zpravaLf = time + " " + zprava + "\n";
        serGUI.appendRoom(zpravaLf);
        // cyklime v protismeru, kvuli moznosti odpojeni jednoho z klientu
        for(int i = arrl.size(); --i >= 0;) {
            VlaknoClient vt = arrl.get(i);
            // napiseme klientovi, pokud se nepovede, smazeme ho
            if(!vt.napisZpr(zpravaLf)) {
                arrl.remove(i);
	        zobraz("Odpojeny uzivatel " + vt.uzivJmeno + ", byl odebran ze seznamu.");
            }
        }
    }
    //pro ty co pouzili tlacitko ODPOJIT
    synchronized void remove(int id){
        for(int i = 0; i < arrl.size(); ++i) {
            VlaknoClient vt = arrl.get(i);
            if(vt.id == id) {
                arrl.remove(i);
                return;
            }
        }
    }
    
    public static void main(String[] args){
        int portNum = 1500;
        switch(args.length){
           case 1:
                try {
                    portNum = Integer.parseInt(args[0]);
                } catch(Exception e) {
                    System.out.println("Spatne cislo portu.");
//                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
//                System.out.println("Usage is: > java Server [portNumber]");
                return;
        }
        Server server = new Server(portNum);
        server.start();
    }
    
    class VlaknoClient extends Thread{
        //socket na poslouchani
        Socket socket;
        ObjectInputStream socIn;
        ObjectOutputStream socOut;
        //moje id po lehci odpojeni
        int id;
        String uzivJmeno;
        Zprava zpr;
        String date;
        
        VlaknoClient(Socket socket){
            id = ++uniqueId;
            this.socket = socket;
            System.out.println("Vlakno se pokusi vytvorit I/O Stream");
            try {
                // prvne output
                socOut = new ObjectOutputStream(socket.getOutputStream());
                socIn = new ObjectInputStream(socket.getInputStream());
                uzivJmeno = (String) socIn.readObject();
                zobraz(uzivJmeno + " se prave pripojil.");
            } catch (IOException e) {
                zobraz("vyjimka pri vztvareni noveho I/O Streamu: " + e);
                return;
            }
            catch (ClassNotFoundException e) {}
            date = new Date().toString() + "\n";        
        }
        //pobezi furt
        @Override
        public void run() {
            boolean pokracuj = true;
            while(pokracuj) {
                try {
                    zpr = (Zprava) socIn.readObject();
                } catch (IOException e) {
                    zobraz(uzivJmeno + " Vyjimka pri cteni streamu: " + e);
                    break;             
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                String zprava = zpr.getZprava(); 
                // Switch na typ obdrzene zpravy
                switch(zpr.getType()) { 
                    case Zprava.ZPRAVA:
                        vysilani(uzivJmeno + ": " + zprava);
                        break;
                    case Zprava.ODPOJIT:
                        zobraz(uzivJmeno + " odpojen .");
                        pokracuj = false;
                        break;
                    case Zprava.PRITOMNI:
                        napisZpr("Seznam pripojenych " + sdf.format(new Date()) + "\n");
                    for(int i = 0; i < arrl.size(); ++i) {
                        VlaknoClient vc = arrl.get(i);
                        napisZpr((i+1) + ") " + vc.uzivJmeno + " since " + vc.date);
                    }
                    break;
                }                
            }
            remove(id);
            close();
        }
        
        //pokusim e se vse zavrit
        public void close(){
            try {
                if(socOut != null) {
                    socOut.close();
                }
            }
            catch(Exception e) {}
            try {
                if(socIn != null) {
                    socIn.close();
                }
            }
            catch(Exception e) {};
            try {
                if(socket != null) {
                    socket.close();
                }
            }
            catch (Exception e) {}
        }
        
        private boolean napisZpr(String zpr) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                socOut.writeObject(zpr);
            }
            catch(IOException e) {
                zobraz("Problem pri posilani zpravy uzivateli " + uzivJmeno);
                zobraz(e.toString());
            }
            return true;
        }
    }    
}
