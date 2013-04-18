/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semestralka;

import java.awt.*;
import javax.swing.*;
/**
 *
 * @author Simon
 */
public class ClientGUI extends JFrame{
    
    static JButton send, sendF, fff;
//    static JTextPane recived, toSend;
    static TextArea recived, toSend;
    
    public ClientGUI() throws HeadlessException{
        super("---Chat---");
        this.setSize(700, 550);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(null);
               
        send = new JButton("Send");
        send.setBounds(610, 480, 70, 30);
        this.add(send);
        
        sendF = new JButton("Send File");
        sendF.setBounds(450, 480, 140, 30);
        this.add(sendF);
        
        toSend = new TextArea();
        toSend.setBounds(10, 320, 670, 150);
        toSend.setEditable(true);
        this.add(toSend);
        
        recived = new TextArea();
//        recived.setSize(480, 350);
        recived.setBounds(10, 10, 670, 300);
        recived.setEditable(false);
        this.add(recived);
    }
    
}
