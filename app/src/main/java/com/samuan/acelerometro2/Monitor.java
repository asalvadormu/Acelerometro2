package com.samuan.acelerometro2;

import android.content.res.Resources;
import android.hardware.SensorEvent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Comprueba los datos del acelerometro.
 *
 * Created by SAMUAN on 13/04/2015.
 */
public class Monitor {

    private float gravedad=9.8066f;

    private LinkedList<Muestra> cola;
    private int tamaLista=500;

    private double umbralGravedad=2.1;

    private long pt=0; //peak time
    private long contadorTiempo=0;
    private String estado="muestreo";
    Muestra[] datos=null;

    private static String TAG="RedNeuronal";
    private Red red;

    public Monitor(Resources resources) {
        FileOperation.fileLogInitialize();
        FileOperation.fileLogWrite(TAG,"Inicio app: "+System.currentTimeMillis());
        FileOperation.fileLogWrite(TAG,"Umbral gravedad: "+umbralGravedad);

        cola =new LinkedList<Muestra>();

        //cargar archivo de pesos
        String linea;
        LinkedList listaDatos1=new LinkedList();
        LinkedList listaDatos2=new LinkedList();
        String marcador="dato0";
        double[] valoresD;
        try{
            InputStream flujo= resources.openRawResource(R.raw.pesos);
            BufferedReader lector= new BufferedReader(new InputStreamReader(flujo));
            while( (linea=lector.readLine())!=null ){
                System.out.println(linea+" "+linea.length());
                if(linea.length()>0 && !linea.startsWith("#")){
                    if(linea.contains("DATA1")){
                        marcador="data1";
                    }else if(linea.contains("DATA2")){
                        marcador="data2";
                    }else{
                        String[] valores = linea.split(",");
                        if(valores.length>1){
                            //  System.out.println("Tamaño vlaores "+valores.length);
                            valoresD=new double[valores.length];
                            for(int i=0;i<valores.length;i++){
                                //System.out.println(""+i);
                                valoresD[i]=Double.parseDouble(valores[i]);
                            }
                            if(marcador.equals("data1")){
                                listaDatos1.add(valoresD);
                            }else if(marcador.equals("data2")){
                                listaDatos2.add(valoresD);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //iniciar red
        //generar las matrices de sinapsis para enviar a red.
        double[][] sinapsisA; //relaciona la entrada con la capa oculta
        int longi=((double[])listaDatos1.get(0)).length; //System.out.println("longi "+longi);
        sinapsisA=new double[listaDatos1.size()][longi];
        for(int j=0;j<listaDatos1.size();j++){
            sinapsisA[j]=(double[])listaDatos1.get(j);
        }

        double[][] sinapsisB; //relaciona la capa oculta con la capa de salida
        int longib=((double[])listaDatos2.get(0)).length;
        sinapsisB=new double[listaDatos2.size()][longib];
        for(int j=0;j<listaDatos2.size();j++){
            sinapsisB[j]=(double[])listaDatos2.get(j);
        }

        red=new Red();
        System.out.println("iniciar red: "+sinapsisA[0].length+" "+sinapsisA.length+" "+sinapsisB.length);
        red.iniciarRed(sinapsisA[0].length,sinapsisA.length,sinapsisB.length);
        red.setSinapsisA(sinapsisA);
        red.setSinapsisB(sinapsisB);

        double[] biasA=new double[sinapsisA.length];
        for(int j=0;j<sinapsisA.length;j++){
            biasA[j]=1;
        }
        red.setBiasA(biasA);

        double[] biasB=new double[sinapsisB.length];
        for(int j=0;j<sinapsisB.length;j++){
            biasB[j]=1;
        }
        red.setBiasB(biasB);
    }

    /**
     * Gestiona los eventos del acelerometro. Si se cumplen las caracteristicas extrae caracteristicas
     * y ejecuta la red neuronal.
     *
     * @param event
     */
    public void gestionar(SensorEvent event) {
        float values[] = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        float xg=x/gravedad; //Divido por gravedad para pasar unidades de m/s^2 a g
        float yg=y/gravedad;
        float zg=z/gravedad;

        long tiempo=event.timestamp;
        double modulo=calcularModulo(xg,yg,zg);

        cargarMuestra(new Muestra(tiempo,modulo));
        Log.i("MONITOR","gestionar "+modulo);
        if(estado.equals("muestreo")){ //se ha detectado un pico de gravedad
            if(modulo>umbralGravedad){
                iniciarPostpeak(modulo,tiempo);
            }
        }

        if(estado.equals("postpeak")){ //Ahora esperamos un tiempo de 2.5 segundos sin picos superiores al umbral.
            contadorTiempo=tiempo-pt;
            if(modulo>umbralGravedad) iniciarPostpeak(modulo,tiempo); //si se detecta un nuevo pico, comenzamos a contar el tiempo de nuevo.
            if(contadorTiempo>2500000000l){
                //generar array de valores.
                datos=new Muestra[cola.size()];
                cola.toArray(datos); //extraigo datos a analizar.
                FileOperation.fileLogWrite(TAG, "Test de actividad ");
                iniciarActivityTest();
                Log.i("Acelerometro","iniciar activity test "+tiempo);
            }
        }

    }

    /**
     * Realizo el test de actividad. Si la actividad es baja se extraen caracteristicas y
     * se pasa a red neuronal.
     *
     * La respuesta de la red neuronal se para a archivo.
     */
    private void iniciarActivityTest(){
        //capturar datos de lista
        estado="activitytest";
        Log.i("Acelerometro","iniciar activity test");

        //calcular AAMV , media de las diferencias.
        long tiempoInicioCalculo=pt+1000000000; //se toma desde 1 sg a 2.5 sg despues del impacto
        int marcador=0;
        double difTotal=0;
        long tiempoFinalCalculo = pt + 2500000000l;
        int marcadorFin=datos.length-1;


        for(int i=0;i<datos.length;i++){
            //buscar el dato con tiempo > tiempoIniciocalculo
            if( datos[i].getTiempo()>tiempoInicioCalculo ){
                marcador=i;
                break;
            }
        }
        for(int i=marcador;i<datos.length;i++){
            if(datos[i].getTiempo()>tiempoFinalCalculo){
                marcadorFin=i;
                break;
            }
        }
        for(int j=marcador;j<marcadorFin;j++){
            double dif=Math.abs( datos[j].getAceleracion() - datos[j+1].getAceleracion() );
            difTotal=difTotal+dif;
        }
        difTotal=difTotal/(datos.length-marcador);
        Log.i(TAG,"Filtro AAMV: "+difTotal);

        FileOperation.fileLogWrite(TAG,"Filtro AAMV: "+difTotal);

        //si valor supera 0.05g entonces se descarta como caida
        //si es menor o igual se considera caida y se envian datos a clasificador
        if(difTotal>0.05){

        }else {
            FileOperation.fileLogWrite(TAG,"Envío a Red Neuronal");
            Log.i(TAG,"Envío a Red Neuronal");
            Log.i(TAG,"tiempo de pico "+pt);
            Extractor extractor = new Extractor(pt, datos);
            double[] resultadoCara=extractor.getCaracteristicas();
            if(resultadoCara!=null){
                red.setVector_entrada(resultadoCara);
                red.calcular();
                double[] laSalida = red.getVector_salida();

                //indicar la salida
                String salida="";
                for(int indice=0;indice<laSalida.length;indice++){
                    salida=salida+" "+laSalida[indice];
                }
                Log.i(TAG,"salida : "+salida);
                FileOperation.fileLogWrite(TAG,"salida "+salida);
            }
        }
        estado="muestreo";
    }

    /**
     * Cambia el estado a "postpeak".
     *
     * @param modulo
     * @param tiempo
     */
    private void iniciarPostpeak(double modulo,long tiempo){
        contadorTiempo=0;
        pt=tiempo;
        estado="postpeak";
        // System.out.println("iniciar post peak "+tiempo);
        FileOperation.fileLogWrite(TAG,"Post peak | Modulo: "+modulo+" Tiempo: "+tiempo);
        Log.i(TAG,"Post peak | Modulo: "+modulo+" Tiempo: "+tiempo);
    }

    /**
     * Añade un objeto muestra a la cola.
     * Si la cola se llena elimina por la cabeza.
     *
     * @param muestra
     */
    private void cargarMuestra(Muestra muestra){
        cola.add(muestra);
        if(cola.size()>tamaLista) cola.poll();
    }

    /**
     * Calcula el módulo del vector aceleración dado por el acelerómetro
     * @param x
     * @param y
     * @param z
     * @return
     */
    private double calcularModulo(double x, double y, double z){
        return Math.sqrt(    Math.pow(x,2) + Math.pow(y,2)+ Math.pow(z,2)   );
    }

}
