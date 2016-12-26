/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Datos;

/**
 *
 * @author davidvalladarez
 */
public class Archivo {
    private int campo1;
    private String campo2;
    private boolean campo3;
    private String campo4;

    public Archivo(int campo1, String campo2, boolean campo3, String campo4) {
        this.campo1 = campo1;
        this.campo2 = campo2;
        this.campo3 = campo3;
        this.campo4 = campo4;
    }

    /**
     * @return the campo1
     */
    public int getCampo1() {
        return campo1;
    }

    /**
     * @param campo1 the campo1 to set
     */
    public void setCampo1(int campo1) {
        this.campo1 = campo1;
    }

    /**
     * @return the campo2
     */
    public String getCampo2() {
        return campo2;
    }

    /**
     * @param campo2 the campo2 to set
     */
    public void setCampo2(String campo2) {
        this.campo2 = campo2;
    }

    /**
     * @return the campo3
     */
    public boolean getCampo3() {
        return campo3;
    }

    /**
     * @param campo3 the campo3 to set
     */
    public void setCampo3(boolean campo3) {
        this.campo3 = campo3;
    }

    /**
     * @return the campo4
     */
    public String getCampo4() {
        return campo4;
    }

    /**
     * @param campo4 the campo4 to set
     */
    public void setCampo4(String campo4) {
        this.campo4 = campo4;
    }
    
    
    
}
