
package agentes;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Random;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.KStar;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.Id3;
import weka.core.Instances;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

public class AgenteDM extends Agent {

    public class Clasificar extends OneShotBehaviour {

        private String resultado = "";
        private final int numIteraciones = 10;
        //private double porcentaje = 60.0;
        private double porcentaje;
        
        public void action() {
            //Recive el conjunto de datos
            ACLMessage msg = this.myAgent.blockingReceive();//accede al agente que tenga la accion y lo bloquea para que solo quede esperando el mensaje        
            String contenido = msg.getContent();//recibimos el mensaje
            //Separamos el mensaje para tener porcentaje y contenido
            StringTokenizer tokens=new StringTokenizer(contenido, "***");
            String porc=tokens.nextToken();
            porcentaje=Double.parseDouble(porc);
            String mensaje=tokens.nextToken();
            StringReader sr = new StringReader(mensaje); // El mensaje tipo String lo convertimos a un StringReader
            BufferedReader br = new BufferedReader(sr); // el StringReader lo convertimos a un Buffer

            try {
                Instances data = new Instances(br);//definimos un objeto que contendra los datos a clasificar
                ReplaceMissingValues filtro = new ReplaceMissingValues();
                filtro.setInputFormat(data);
                
                Instances newData = Filter.useFilter(data, filtro);
                NumericToNominal filtro2 = new NumericToNominal();
                filtro2.setInputFormat(newData);
                
                newData = Filter.useFilter(newData, filtro2);
                
                newData.setClassIndex(newData.numAttributes() - 1);//Seleccionamos cual sera el atributo clase
                br.close();//cerramos el objeto buffer  
                newData.randomize(new Random());         //barajamos de forma aleatoria los datos
                
                //hacemos particion de datos para entrenamiento y test
                int trainSize = (int) Math.round(newData.numInstances() * porcentaje / 100); 
                int testSize = newData.numInstances() - trainSize; 
                Instances train = new Instances(newData, 0, trainSize); 
                Instances test = new Instances(newData, trainSize, testSize); 
                            
                
                int aciertosJ48 = 0;
                int aciertosMLP = 0;
                int aciertosNB = 0;
                int aciertosK2 = 0;
                int aciertosZeroR = 0;
                int aciertosId3 = 0;
                
                //Calculamos los aciertos para cada modelo en 10 iteraciones
                for (int i = 0; i < numIteraciones ; i++){
                    aciertosJ48  += clasificarJ48(train, test);
                    aciertosMLP += clasificarMLP(train, test);
                    aciertosNB += clasificarNB(train, test);
                    aciertosK2 += clasificarKS(train, test);
                    aciertosZeroR += clasificarZeroR(train, test);
                    aciertosId3 += clasificarId3(train, test);
                }
                
                //calculamos la media de aciertos
                calcularResultadoFinal("J48", (int) Math.round(aciertosJ48/numIteraciones));
                calcularResultadoFinal("MLP", (int) Math.round(aciertosMLP/numIteraciones));
                calcularResultadoFinal("NB", (int) Math.round(aciertosNB/numIteraciones));
                calcularResultadoFinal("KStar", (int) Math.round(aciertosK2/numIteraciones));
                calcularResultadoFinal("ZeroR", (int) Math.round(aciertosZeroR/numIteraciones));
                calcularResultadoFinal("Id3", (int) Math.round(aciertosId3/numIteraciones));
                
                //envia el resultado final al agente Pantalla
                enviarResultado(resultado);
                
            } catch (Exception e) {
                System.out.println("El error es" + e.getMessage());
                JFrame MiVentana = new JFrame("Error"); //llamamos a la clase y creamos un objeto llamado MiVentana 
                JOptionPane.showMessageDialog(MiVentana, e.getMessage());
            }
        }
        
        /**
         * Devuelve el número de instancias correctas
         * @param data
         * @return 
         */
        private int clasificarJ48(Instances train, Instances test){
            try{
                J48 j48 = new J48(); // Creamos un clasidicador J48
                j48.buildClassifier(train);//creamos el clasificador  del J48 con los datos de entrenamiento
                
                Evaluation evalJ48 = new Evaluation(train);//Creamos un objeto para la validacion del modelo con redBayesiana
                evalJ48.evaluateModel(j48, test);       //evaluamos con los datos de test
                
                return (int) evalJ48.correct();         //devolvemos instancias correctamente clasificadas
            }catch(Exception e){
                System.out.println("El error es" + e.getMessage());
                return 0;
            }
        }
        
        private int clasificarNB(Instances train, Instances test){
            try{
                NaiveBayes model=new NaiveBayes(); //Creamos el clasificador de Bayes
                model.buildClassifier(train); //Creamos el clasificador con datos de entrenamiento
                Evaluation evalBayes = new Evaluation(test); //Creamos objeto para validacion
                evalBayes.evaluateModel(model,test);   //Evaluamos con test
                return (int) evalBayes.correct();  //devolvemos instancias correctamente clasificadas
                
            }
            catch(Exception e){
                System.out.println("El error es" + e.getMessage());
                return 0;
            }
        }
        
        private int clasificarMLP(Instances train, Instances test){
            try{
                MultilayerPerceptron mlp = new MultilayerPerceptron(); // Creamos un clasificador
                //ajustamos mlp (capas ocultas... etc)
                mlp.setOptions(Utils.splitOptions("-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H a"));
                mlp.buildClassifier(train);//creamos el clasificador con los datos de entrenamiento
                
                Evaluation evalMlp = new Evaluation(train);//Creamos un objeto para la validacion del modelo con redBayesiana con los datos de test
                evalMlp.evaluateModel(mlp, test);
                
                return (int) evalMlp.correct();         //devolvemos instancias correctamente clasificadas
            }catch(Exception e){
                System.out.println("El error es" + e.getMessage());
                return 0;
            }
        }
        
        private int clasificarKS(Instances train, Instances test){
            try{
               
                
                KStar ks=new KStar();
                ks.buildClassifier(train);
                
                Evaluation evalKS = new Evaluation(test); //Creamos objeto para validacion
                evalKS.evaluateModel(ks, test);
                return (int) evalKS.correct();  //devolvemos instancias correctamente clasificadas
                
            }
            catch(Exception e){
                System.out.println("+El error es" + e.getMessage());
                return 0;
            }
        }
        
        private int clasificarZeroR(Instances train, Instances test){
            try{
                ZeroR zR=new ZeroR(); //Creamos el clasificador de ZeroR
                zR.buildClassifier(train); //Creamos el clasificador con datos de entrenamiento
                Evaluation evalzR = new Evaluation(test); //Creamos objeto para validacion
                evalzR.evaluateModel(zR,test);   //Evaluamos con test
                return (int) evalzR.correct();  //devolvemos instancias correctamente clasificadas
                
            }
            catch(Exception e){
                System.out.println("El error es" + e.getMessage());
                return 0;
            }
        }
        
        private int clasificarId3(Instances train, Instances test){
            try{
                Id3 id3 = new Id3(); // Creamos un clasidicador J48
                id3.buildClassifier(train);//creamos el clasificador  del J48 con los datos 
                
                Evaluation evalId3 = new Evaluation(train);//Creamos un objeto para la validacion del modelo con redBayesiana
                evalId3.evaluateModel(id3, test);       //evaluamos con los datos de test
                
                return (int) evalId3.correct();     //devolvemos instancias correctamente clasificadas
            }catch(Exception e){
                System.out.println("El error es" + e.getMessage());
                return 999999;  
            }
        }
        
        
        /**
         * Respuesta sera una cadena de texto que contendra la estructura: 
         * modelo naciertos\nmodelo naciertos\nmodelo naciertos.
         * para luego en pantalla poder parsearlo y formar el histograma
         */
        private void calcularResultadoFinal(String modelo, int nAciertos){
            try{
                resultado += modelo + " "+nAciertos+"\n";
            }
            catch(Exception e){}
            
        }
        
        private void enviarResultado(String res){
            try
            {
                ACLMessage resultado = new ACLMessage(ACLMessage.CONFIRM);//se define objeto de tipo mensaje
                resultado.setContent(res);//se le añade el contenido al objeto de tipo mensaje
                resultado.addReceiver(new AID("pantalla", AID.ISLOCALNAME));//AID= Agent identification, se le añade a quien se le envia
                this.myAgent.send(resultado); //el agente actual envia el mensaje
            }
            catch(Exception err){
               System.out.println("El error es" + err.getMessage());
               JFrame MiVentana = new JFrame("Error"); //llamamos a la clase y creamos un objeto llamado MiVentana 
               JOptionPane.showMessageDialog(MiVentana, err.getMessage());
            }
        }
        
    }

    
    /*Asignacion de comportamientos*/
    protected void setup() {
        //Clasificar_J48 cs = new Clasificar_J48();
        Clasificar cs = new Clasificar();
        this.addBehaviour(cs);
    }
}
