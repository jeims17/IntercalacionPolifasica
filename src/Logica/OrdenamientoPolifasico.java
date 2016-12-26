/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logica;

import Datos.Archivo;
import java.io.File;
import java.io.FileReader;
import javax.swing.JOptionPane;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author davidvalladarez
 */
public class OrdenamientoPolifasico {
    private String campoOrdenamiento;
    private int numeroArchivosAuxiliares;
    private String nombreArchivoOrigen;
    private File archivoOrigen;
    private File[] archivosAuxiliares;
    private int numeroCampoOrdenamiento = 0;
    
    public OrdenamientoPolifasico(int numeroArchivosAuxiliares , String nombreArchivoOrigen , String campoOrdenamiento) {
        this.numeroArchivosAuxiliares = numeroArchivosAuxiliares;
        this.nombreArchivoOrigen = nombreArchivoOrigen;
        this.campoOrdenamiento = campoOrdenamiento;
    }
    
    public void ordenamientoPolifasico() throws IOException{
        if(verificacionArchivoOrigen() && verificacionNumeroArchivosAuxiliares()){
            // m - 1 archivos de Entrada
            int numeroArchivosEntrada = numeroArchivosAuxiliares - 1;
            archivosAuxiliares = new File[numeroArchivosAuxiliares];
            // Creamos los archivos CSV Auxiliares
            for (int i = 0 ; i < numeroArchivosAuxiliares ; i++){
                archivosAuxiliares[i] = new File(String.valueOf(i) + ".csv");
            }
            
            /*
                Creamos un Arreglo de Flujos para poder guardar los m-1 "Flujos de Archivos de Entrada"
                y el "Flujo Archivo de Salida Restante"
            */
            Object flujos[] = new Object[numeroArchivosAuxiliares];
            /*
                Creamos un Arreglo de Indices para poder guardar la posicion de los 
                "Fulos de Entrada" y el "Flujo de Salida" ya que en el Ordenamiento Polifasico
                variara el flujo de archivo de Salida
            */
            int indices[] = new int[numeroArchivosAuxiliares];
            int indicesAuxiliares[] = new int[numeroArchivosAuxiliares];
            /*
                Creamos un Arreglo Comparador para almacenar el Contenido de Registros
                a comparar.
                La clase Archivo estara en la Capa de Datos
            */
            Archivo comparador[] = new Archivo[numeroArchivosEntrada];
            
            /*
                La variable tramos nos va a decir cuando termino la ordenacion
            */
            int tramos;
            int j = 0;
            String []cabeceras = extraerCabeceras(new CsvReader(nombreArchivoOrigen));
            
            boolean registrosActivos[] = new boolean[numeroArchivosEntrada];
            
            try{
                //Inicializacion del Arreglo Indices
                for (int i = 0 ; i < numeroArchivosAuxiliares ; i++){
                    indices[i] = i;
                }
                tramos = distribucionRegistros(numeroArchivosEntrada);
                do{
                    int numRegistrosSeleccionados = (tramos < numeroArchivosEntrada) ? tramos : numeroArchivosEntrada;
                    for (int i = 0 ; i < numRegistrosSeleccionados ; i++){
                        flujos[indices[i]] = new CsvReader(archivosAuxiliares[indices[i]].getName());
                        indicesAuxiliares[i] = indices[i];
                    }
                    j = verArchivoSalida(archivosAuxiliares);
                    
          
                    tramos = 0;
                   
                    flujos[j] = new CsvWriter(String.valueOf(j) + ".csv");
                    CsvReader flujoEntrada = null;
                    for (int i = 0 ; i < numRegistrosSeleccionados ; i++){
                        flujoEntrada = (CsvReader) flujos[indicesAuxiliares[i]];
                        String []c = null;
                        if(flujoEntrada.readHeaders()) {
                            c = flujoEntrada.getHeaders(); 
                        }
                        flujoEntrada.readRecord();
                        System.out.println(flujoEntrada.get(cabeceras[0]) + " , " + i);
                        //if (archivosAuxiliares[i].length() != 28 )
                            comparador[i] = new Archivo(Integer.parseInt(flujoEntrada.get(c[0])), flujoEntrada.get(c[1]), Boolean.parseBoolean(flujoEntrada.get(c[2])), flujoEntrada.get(c[3]));
                        
                    }
                    
                    while(numRegistrosSeleccionados > 0){
                        
                        tramos++;
                        for (int i = 0 ; i < numRegistrosSeleccionados ; i++){
                            registrosActivos[i] = true;
                        }
                        
                        CsvWriter flujoSalida = (CsvWriter) flujos[indices[j]];
                        
                        while(!finTramo(registrosActivos, numRegistrosSeleccionados)){
                            int indiceMenor = 0;
                            switch(numeroCampoOrdenamiento){
                                case 0:
                                    indiceMenor = minimoCampo0(comparador, registrosActivos, numRegistrosSeleccionados);
                                    break;
                                case 1:
                                    indiceMenor = minimoCampo1(comparador, registrosActivos, numRegistrosSeleccionados);
                                    break;
                                case 2:
                                    indiceMenor = minimoCampo2(comparador, registrosActivos, numRegistrosSeleccionados);
                                    break;
                                case 3:
                                    indiceMenor = minimoCampo3(comparador , registrosActivos, numRegistrosSeleccionados);
                                    break;
                            }
                          
                            flujoEntrada = (CsvReader) flujos[indicesAuxiliares[indiceMenor]];
                           
                            escribirRegistro(comparador[indiceMenor] , flujoSalida);
                            Archivo anterior = new Archivo(comparador[indiceMenor].getCampo1(), comparador[indiceMenor].getCampo2(), comparador[indiceMenor].getCampo3(), comparador[indiceMenor].getCampo4());
                            try{
                                
                                flujoEntrada.readRecord();
                                comparador[indiceMenor].setCampo1(Integer.parseInt(flujoEntrada.get(cabeceras[0])));
                                comparador[indiceMenor].setCampo2(flujoEntrada.get(cabeceras[1]));
                                comparador[indiceMenor].setCampo3(Boolean.parseBoolean(flujoEntrada.get(cabeceras[2])));
                                comparador[indiceMenor].setCampo4(flujoEntrada.get(cabeceras[3]));
                                switch(numeroCampoOrdenamiento){
                                    case 0:
                                        if (anterior.getCampo1() > comparador[indiceMenor].getCampo1()){
                                            registrosActivos[indiceMenor] = false;
                                        }
                                        break;
                                    case 1:
                                        if(anterior.getCampo2().compareTo(comparador[indiceMenor].getCampo2()) > 0){
                                            registrosActivos[indiceMenor] = false;
                                        }
                                        break;
                                    case 2:
                                        if(String.valueOf(anterior.getCampo3()).compareTo(String.valueOf(comparador[indiceMenor].getCampo3())) > 0){
                                            registrosActivos[indiceMenor] = false;
                                        }
                                        break;
                                    case 3:
                                         if(anterior.getCampo4().compareTo(comparador[indiceMenor].getCampo4()) > 0){
                                            registrosActivos[indiceMenor] = false;
                                        }
                                        break;
                                }
                                
                            }catch(Exception e){
                                numRegistrosSeleccionados--;
                                flujoEntrada.close();
                                indicesAuxiliares[indiceMenor] = indicesAuxiliares[numRegistrosSeleccionados];
                                indices[indiceMenor] = indices[numRegistrosSeleccionados];
                                registrosActivos[indiceMenor] = registrosActivos[numRegistrosSeleccionados];
                                registrosActivos[numRegistrosSeleccionados] = false;
                            }
                        }
                    }
                    
                    CsvWriter flujoSalida = (CsvWriter) flujos[j];
                    flujoSalida.close();
                    refrescar(archivoOrigen, archivosAuxiliares[j]);
                    for (int i = 0 ; i < numeroArchivosAuxiliares ; i++){
                        archivosAuxiliares[i].delete();
                    }
                    tramos = distribucionRegistros(numeroArchivosEntrada);
                    for (int i = 0 ; i < numeroArchivosAuxiliares ; i++){
                        System.out.println("Peso : " + i + " : " + archivosAuxiliares[i].length());
                    }
                }while(tramos > 1);
            }catch(Exception e){
               e.printStackTrace();
            }
            
        }else{
            JOptionPane.showMessageDialog(null, "Error de Ingreso de Datos");
        }
    }
//----------------------------- Bloque de Verificacion ----------------------
    public boolean verificacionArchivoOrigen(){
        archivoOrigen = new File(nombreArchivoOrigen);
        if(archivoOrigen.exists()){
            return true;
        }else{
            return false;
        }
    }
    public boolean verificacionNumeroArchivosAuxiliares(){
        if (numeroArchivosAuxiliares > 2){
            return true;
        }else{
            return false;
        }
    }
//------------------------------------------------------------------------
    
    public int distribucionRegistros(int numeroArchivosEntrada) throws FileNotFoundException, IOException{
        int tramos = 0;
        //Creamos el Flujo de Entrada para poder leer los registros del Archivo Original
        CsvReader flujoEntrada = new CsvReader(new FileReader(nombreArchivoOrigen));
       
        String []cabeceras;
        cabeceras = extraerCabeceras(flujoEntrada);
        
        for (int i = 0 ; i < cabeceras.length ; i++){
            if (cabeceras[i].equals(this.campoOrdenamiento)){
                numeroCampoOrdenamiento = i;
                break;
            }
        }
        
        /*
            Creamos un Arreglo de de Flujo de Salida ya que se Crearan los Archivos 
            Auxiliares y se distribuiran los registros
        */
        CsvWriter flujoSalida[] = new CsvWriter[numeroArchivosEntrada];
        
        for (int i = 0 ; i < numeroArchivosEntrada ; i++){
            flujoSalida[i] = new CsvWriter(String.valueOf(i) + ".csv");
            ponerCabeceras(flujoSalida[i]);
        }
        
        Archivo actual;
        Archivo anterior;
        anterior = new Archivo(-999999999, "aaaaa", false, "00/00/0000");
        int j = 0;
        
        try{
            while(true){
                flujoEntrada.readRecord();
                int campo1 = Integer.parseInt(flujoEntrada.get(cabeceras[0]));
                String campo2 = flujoEntrada.get(cabeceras[1]);
                boolean campo3 = Boolean.parseBoolean(flujoEntrada.get(cabeceras[2]));
                String campo4 = flujoEntrada.get(cabeceras[3]);
                actual = new Archivo(campo1, campo2, campo3, campo4);
                switch(numeroCampoOrdenamiento){
                    case 0:
                        
                        actual = distibucionCampo1(actual , anterior , flujoSalida , flujoEntrada , cabeceras ,j);
                        tramos++;
                        j = (j < numeroArchivosEntrada - 1 ) ? j+1 : 0;
                        escribirRegistro(actual, flujoSalida[j]);
                        anterior.setCampo1(actual.getCampo1());
                        anterior.setCampo2(actual.getCampo2());
                        anterior.setCampo3(actual.getCampo3());
                        anterior.setCampo4(actual.getCampo4());
                        break;
                        
                    case 1:
                        actual = distribucionCampo2(actual , anterior , flujoSalida , flujoEntrada , cabeceras ,j);
                        tramos++;
                        j = (j < numeroArchivosEntrada - 1 ) ? j+1 : 0;
                        escribirRegistro(actual, flujoSalida[j]);
                        anterior.setCampo1(actual.getCampo1());
                        anterior.setCampo2(actual.getCampo2());
                        anterior.setCampo3(actual.getCampo3());
                        anterior.setCampo4(actual.getCampo4());
                        break;
                        
                    case 2:
                        actual = distribucionCampo3(actual , anterior , flujoSalida , flujoEntrada , cabeceras ,j);
                        tramos++;
                        j = (j < numeroArchivosEntrada - 1 ) ? j+1 : 0;
                        escribirRegistro(actual, flujoSalida[j]);
                        anterior.setCampo1(actual.getCampo1());
                        anterior.setCampo2(actual.getCampo2());
                        anterior.setCampo3(actual.getCampo3());
                        anterior.setCampo4(actual.getCampo4());
                        break;
                    case 3:
                       actual = distribucionCampo4(actual , anterior , flujoSalida , flujoEntrada , cabeceras ,j);
                        tramos++;
                        j = (j < numeroArchivosEntrada - 1 ) ? j+1 : 0;
                        escribirRegistro(actual, flujoSalida[j]);
                        anterior.setCampo1(actual.getCampo1());
                        anterior.setCampo2(actual.getCampo2());
                        anterior.setCampo3(actual.getCampo3());
                        anterior.setCampo4(actual.getCampo4());
                        break;
                }
            }
        }catch(Exception e){
            tramos++;
            System.out.println("*** Numero de Tramos **** : " + tramos);
            for (int i = 0 ; i < numeroArchivosEntrada ; i++){
                flujoSalida[i].close();
            }
            return tramos;
        }
    }
    
    public Archivo distibucionCampo1(Archivo actual , Archivo anterior_ , CsvWriter flujoSalida[] , CsvReader flujoEntrada , String cabeceras[] , int j) throws IOException{
        int anterior = anterior_.getCampo1();
        while(anterior <= actual.getCampo1()){
            escribirRegistro(actual , flujoSalida[j]);
            anterior = actual.getCampo1();
            flujoEntrada.readRecord();
            int campo1 = Integer.parseInt(flujoEntrada.get(cabeceras[0]));
            String campo2 = flujoEntrada.get(cabeceras[1]);
            boolean campo3 = Boolean.parseBoolean(flujoEntrada.get(cabeceras[2]));
            String campo4 = flujoEntrada.get(cabeceras[3]);
            actual = null;
            actual = new Archivo(campo1, campo2, campo3, campo4);
        }
        return actual;
    }
    
    public Archivo distribucionCampo2(Archivo actual , Archivo anterior_ , CsvWriter flujoSalida[] , CsvReader flujoEntrada , String cabeceras[] , int j) throws IOException{
        String anterior = anterior_.getCampo2();
        while(anterior.compareTo(actual.getCampo2()) <= 0){
            escribirRegistro(actual , flujoSalida[j]);
            anterior = actual.getCampo2();
            flujoEntrada.readRecord();
            int campo1 = Integer.parseInt(flujoEntrada.get(cabeceras[0]));
            String campo2 = flujoEntrada.get(cabeceras[1]);
            boolean campo3 = Boolean.parseBoolean(flujoEntrada.get(cabeceras[2]));
            String campo4 = flujoEntrada.get(cabeceras[3]);
            actual = null;
            actual = new Archivo(campo1, campo2, campo3, campo4);
        }
        return actual;
    }
    
    public Archivo distribucionCampo3(Archivo actual , Archivo anterior_ , CsvWriter flujoSalida[] , CsvReader flujoEntrada , String cabeceras[] , int j) throws IOException{
        String anterior = String.valueOf(anterior_.getCampo3());
        while(anterior.compareTo(String.valueOf(actual.getCampo3())) <= 0){
            escribirRegistro(actual , flujoSalida[j]);
            anterior = String.valueOf(actual.getCampo3());
            flujoEntrada.readRecord();
            int campo1 = Integer.parseInt(flujoEntrada.get(cabeceras[0]));
            String campo2 = flujoEntrada.get(cabeceras[1]);
            boolean campo3 = Boolean.parseBoolean(flujoEntrada.get(cabeceras[2]));
            System.out.println("Campo 3 : " + campo3);
            String campo4 = flujoEntrada.get(cabeceras[3]);
            actual = null;
            actual = new Archivo(campo1, campo2, campo3, campo4);
        }
        return actual;
    }
    
    public Archivo distribucionCampo4(Archivo actual , Archivo anterior_ , CsvWriter flujoSalida[] , CsvReader flujoEntrada , String cabeceras[] , int j) throws IOException, ParseException{
        SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
        Date fecha1 = formateador.parse(anterior_.getCampo4());
        Date fecha2 = formateador.parse(actual.getCampo4());
        
        while(fecha1.before(fecha2) ||  fecha1.equals(fecha2)){
            escribirRegistro(actual , flujoSalida[j]);
            fecha1 = formateador.parse(actual.getCampo4());
            flujoEntrada.readRecord();
            int campo1 = Integer.parseInt(flujoEntrada.get(cabeceras[0]));
            String campo2 = flujoEntrada.get(cabeceras[1]);
            boolean campo3 = Boolean.parseBoolean(flujoEntrada.get(cabeceras[2]));
            String campo4 = flujoEntrada.get(cabeceras[3]);
            actual = null;
            actual = new Archivo(campo1, campo2, campo3, campo4);
        }
        return actual;
    }
    public int minimoCampo0(Archivo [] r, boolean [] activo, int n){
		 int i, indice;
		 int m;
		 i = indice = 0;
		 m = 999999;
		 for ( ; i < n; i++)
		 {
			 if (activo[i] && r[i].getCampo1() < m)
			 {
				 m = r[i].getCampo1();
				 indice = i;
			 }
		 }
		 return indice;
    }
    
    public int minimoCampo1(Archivo [] r, boolean [] activo, int n){
		 int i, indice;
		 String m;
		 i = indice = 0;
		 m = "zzzzzzzz";
		 for ( ; i < n; i++)
		 {
			 if (activo[i] && r[i].getCampo2().compareTo(m) < 0)
			 {
				 m = r[i].getCampo2();
				 indice = i;
			 }
		 }
		 return indice;
    }
    
    public int minimoCampo2(Archivo [] r, boolean [] activo, int n){
		 int i, indice;
		 String m;
		 i = indice = 0;
		 m = "false";
		 for ( ; i < n; i++)
		 {
			 if (activo[i] && String.valueOf(r[i].getCampo3()).compareTo(m) < 0)
			 {
				 m = String.valueOf(r[i].getCampo3());
				 indice = i;
			 }
		 }
		 return indice;
    }
    
    public int minimoCampo3(Archivo [] r, boolean [] activo, int n) throws ParseException{
		 int i, indice;
		 i = indice = 0;
                 SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
                 Date m = formateador.parse("00/00/0000");
		
		 for ( ; i < n; i++)
		 {
                         Date fecha1 = formateador.parse(r[i].getCampo4());
			 if (activo[i] && fecha1.before(m) ||  fecha1.equals(m))
			 {
				 m = formateador.parse(r[i].getCampo4());
				 indice = i;
			 }
		 }
		 return indice;
    }
    
    public boolean finTramo(boolean registrosActivos[] , int n){
        boolean s = true;
        for(int k = 0; k < n; k++)
        {
            if (registrosActivos[k])	
            s = false;
        }
        return s;
    }
    
    public String[] extraerCabeceras(CsvReader flujoEntrada) throws IOException{
          String []cabeceras = new String[4];
        //Extraemos el nombre de la cabecera 
        if(flujoEntrada.readHeaders()) {
            cabeceras = flujoEntrada.getHeaders(); 
        } 
        return cabeceras;
    }
    
    public void ponerCabeceras(CsvWriter flujoSalida) throws IOException{
        flujoSalida.write("campo1");
        flujoSalida.write("campo2");
        flujoSalida.write("campo3");
        flujoSalida.write("campo4");
        flujoSalida.endRecord(); 
    }
    
    public void escribirRegistro(Archivo actual , CsvWriter flujoSalida ) throws IOException{
        flujoSalida.write(String.valueOf(actual.getCampo1()));
        flujoSalida.write(actual.getCampo2());
        System.out.println("Campo 3 : " + actual.getCampo3());
        flujoSalida.write(String.valueOf(actual.getCampo3()));
        flujoSalida.write(actual.getCampo4());
        flujoSalida.endRecord();
    }
    public int verArchivoSalida(File f[]){
        for (int i = 0 ; i < numeroArchivosAuxiliares ; i++){
            if (f[i].length() == 0){
                return i;
            }
        }
        return numeroArchivosAuxiliares - 1;
    }
    
    public void refrescar(File archivoOriginal , File archivoSalida) throws FileNotFoundException, IOException{
        archivoOriginal.delete();
       // archivoOriginal = new File("ArchivoOriginal.csv");
        CsvWriter flujoSalida = new CsvWriter("ArchivoOriginal.csv");
        ponerCabeceras(flujoSalida);
        CsvReader flujoEntrada = new CsvReader(archivoSalida.getName());
        String cabeceras[] = null;
        if(flujoEntrada.readHeaders()) {
            cabeceras = flujoEntrada.getHeaders(); 
        }
        Archivo registro;
        try{
            while(true){
                flujoEntrada.readRecord();
                int campo1 = Integer.parseInt(flujoEntrada.get(cabeceras[0]));
                String campo2 = flujoEntrada.get(cabeceras[1]);
                boolean campo3 = Boolean.parseBoolean(flujoEntrada.get(cabeceras[2]));
                String campo4 = flujoEntrada.get(cabeceras[3]);
                registro = null;
                registro = new Archivo(campo1, campo2, campo3, campo4);
                escribirRegistro(registro, flujoSalida);
            }
        }catch(Exception e){
            flujoSalida.close();
            System.out.println("*** Se a Refrescado ****");
        }
    }
}