/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semestralka;

import java.io.*;
/**
 *
 * @author Simon
 */
public class Zprava implements Serializable{
    protected static final long serialVersion = 1112122200L;
    static final int PRITOMNI = 0, ZPRAVA = 1, ODPOJIT = 2; // SOUBOR = 3;
    private int type;
    private String zprava;
     
    Zprava(int type, String zprava) {
        this.type = type;
        this.zprava = zprava;
    }
     
    int getType() {
        return type;
    }
    String getZprava() {
        return zprava;
    } 
}
