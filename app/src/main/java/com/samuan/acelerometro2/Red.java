package com.samuan.acelerometro2;

import android.util.Log;

/**
 * Clase para construir la red neuronal completa.
 * Se trata de un perceptrón multicapa.
 *
 * Created by SAMUAN on 06/04/2015.
 */
public class Red {

    private double[] vector_entrada;
    private double[] vector_parcial;
    private double[] vector_salida;

    private double[][] sinapsisA; //relaciona la entrada con la capa oculta
    private double[][] sinapsisB; //relaciona la capa oculta con la capa de salida

    private double[] biasA; //capa oculta
    private double[] biasB; //capa de salida

    private int numEntradas;
    private int numCapaOculta;
    private int numCapaSalida;



    /**
     * Constructor por defecto. Se inicia la red llamando a los métodos adecuados.
    */
    public Red(){   }

    /**
     * Calcula los valores de salida de la red completa.
     *
     */
    public void calcular(){
        //vector entrada por sinapsisA
        double suma=0;
        for(int i=0;i<sinapsisA.length;i++){ //i es cada neurona de esta capa
            suma=0;
            for(int j=0;j<sinapsisA[0].length;j++) { //j es numero de entradas.
                suma +=vector_entrada[j]*sinapsisA[i][j];
               // Log.i("CALCULAR","calculo: "+vector_entrada[j]+" "+sinapsisA[i][j]+" "+suma );
            }
            suma=suma+biasA[i];
            vector_parcial[i]=logsig(suma);
        }

        //vector parcial por sinapsisB
        for(int i=0;i<sinapsisB.length;i++){
            suma=0;
            for(int j=0;j<sinapsisB[i].length;j++) {
                suma +=vector_parcial[j]*sinapsisB[i][j];
            }
            suma=suma+biasB[i];
            vector_salida[i]=logsig(suma);
        }
    }

    /**
     * Función de activación
     * Se utiliza una función sigmoidal.
     *
     * @param val valor de x
     * @return resultado
     */
    private double logsig(double val){
        return 1/(1+ Math.exp(-val));
    }

    /*********** MÉTODOS PARA INICIO DE RED *******/

    /**
     * Inicia la red para calcular salidas.
     *
     * @param numEntradas Cantidad de datos de entrada
     * @param numCapaOculta Cantidad de neuronas en capa oculta
     * @param numCapaSalida Cantidad de neuronas en capa de salida
     */
    public void iniciarRed(int numEntradas, int numCapaOculta, int numCapaSalida) {
        this.numEntradas=numEntradas;
        this.numCapaOculta=numCapaOculta;
        this.numCapaSalida=numCapaSalida;

        vector_entrada=new double[this.numEntradas];
        vector_parcial=new double[this.numCapaOculta];
        vector_salida=new double[this.numCapaSalida];

        sinapsisA=new double[numEntradas][numCapaOculta];
        sinapsisB=new double[numCapaOculta][numCapaSalida];

        biasA=new double[numCapaOculta];
        biasB=new double[numCapaSalida];
    }



    /************* MÉTODOS GET SET ****************************/

    public double[] getVector_entrada() {
        return vector_entrada;
    }

    public void setVector_entrada(double[] vector_entrada) {
        this.vector_entrada = vector_entrada;
    }

    public double[] getVector_salida() {
        return vector_salida;
    }

    public double[] getVector_parcial() {
        return vector_parcial;
    }

    public void setVector_parcial(double[] vector_parcial) {
        this.vector_parcial = vector_parcial;
    }

    public void setVector_salida(double[] vector_salida) {
        this.vector_salida = vector_salida;
    }

    public double[] getBiasA() {
        return biasA;
    }

    public void setBiasA(double[] biasA) {
        this.biasA = biasA;
    }

    public double[] getBiasB() {
        return biasB;
    }

    public void setBiasB(double[] biasB) {
        this.biasB = biasB;
    }

    public double[][] getSinapsisB() {
        return sinapsisB;
    }

    public void setSinapsisB(double[][] sinapsisB) {
        this.sinapsisB = sinapsisB;
    }

    public double[][] getSinapsisA() {
        return sinapsisA;
    }

    public void setSinapsisA(double[][] sinapsisA) {
        this.sinapsisA = sinapsisA;
    }



    /******************* METODOS DE IMPRESION DE DATOS ********************/

    public void imprimirSinapsis(){
        for(int i=0;i<sinapsisA.length;i++) {
            String linea="";
            for (int j = 0; j < sinapsisA[0].length; j++) {
                linea += " "+sinapsisA[i][j];
            }
            Log.i("SINAPSIS", linea);
        }
    }

    public void imprimirVectorParcial(){
        for(int i=0;i<vector_parcial.length;i++){
            Log.i("PARCIAL", " " + vector_parcial[i]);
        }
    }


}
