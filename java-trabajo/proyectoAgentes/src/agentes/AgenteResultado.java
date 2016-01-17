
package agentes;

import jade.core.behaviours.OneShotBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.*;


public class AgenteResultado extends GuiAgent {

    private JfrmAgenteResultado vres;//interfaz
    AgenteResultado ag = this;//variable que se almacena el mismo para ser accedido por la accion del agente
    
    @Override
    protected void onGuiEvent(GuiEvent ge) {
    }

    public class mostrarResultado extends OneShotBehaviour {

        public void action() {
            ACLMessage msg = this.myAgent.blockingReceive();//accede al agente que tenga la accion y lo bloquea para que solo quede esperando el mensaje                  
            vres.setVisible(true);//solo se muestra la interfaz cuando se ha recibo el mensaje
            String mensaje = msg.getContent();//Obtenemos el resultado
            
            mostrarHistograma(mensaje);
            vres.mostrarResultado(mensaje); //se llama la funcion mostrar resultado que se encuentra en el Jfrm
            
        }
        
        private void mostrarHistograma(String mensaje){
            try{
                String [] aux = mensaje.split("\n");
                
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                for(int i = 0; i < 6; i++){
                    String[] aux2 = aux[i].split(" ");
                    //Label para mostrar numero de aciertos
                    String label=aux2[0]+"("+aux2[1]+")";
                    dataset.setValue(Integer.parseInt(aux2[1]), "Aciertos", label);
                    
                }

                JFreeChart chart = ChartFactory.createBarChart("Media de aciertos en 10 iteraciones", "Modelos", "Aciertos", dataset, PlotOrientation.VERTICAL, false, true, false);
                CategoryPlot p = chart.getCategoryPlot();
                p.setDomainGridlinesVisible(true);
                p.setRangeGridlinePaint(Color.black);
                
                ChartFrame frame = new ChartFrame("Histograma TP6 SATD", chart);
                frame.setVisible(true);
                frame.setSize(620, 400);
            }catch(Exception e) {
                System.err.println("Error mostrar histograma");
            }
        }
        
    }

    /*Asignacion de comportamientos*/
    protected void setup() {
        vres = new JfrmAgenteResultado(ag);//se instancia la interfaz
        mostrarResultado cs = new mostrarResultado();
        this.addBehaviour(cs);
    }
    

}
