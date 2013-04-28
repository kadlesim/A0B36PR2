/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semestralka;

import java.io.*;
import java.net.*;

/**
 *
 * @author Simon
 */
public class MSThread extends Thread{
    private Socket socket = null;

    public MSThread(Socket socket) {
	super("MSThread");
	this.socket = socket;
    }

    @Override
    public void run() {

	try {
	    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(
				    new InputStreamReader(
				    socket.getInputStream()));

	    String inputLine, outputLine;
//	    outputLine = ;
//	    out.println(outputLine);
            BufferedReader buffIn = new BufferedReader(new InputStreamReader(System.in));

	    while ((inputLine = in.readLine()) != null) {
		outputLine = buffIn.readLine();
		out.println(outputLine);
		if (outputLine.equals("exit")){
		    break;
                }
	    }
	    out.close();
	    in.close();
	    socket.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}