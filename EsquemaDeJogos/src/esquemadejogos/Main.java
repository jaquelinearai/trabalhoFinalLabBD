package esquemadejogos;

import esquemadejogos.JanelaPrincipal;
import esquemadejogos.Conection;

public class Main {

    public static void main(String[] args) {
        /*DBFuncionalidades db = new DBFuncionalidades();
        if (db.conectar()) {
            db.getDDL();
        }*/
        
        Conection c = new Conection();
        c.setVisible(true);
        //JanelaPrincipal j = new JanelaPrincipal();
        //j.ExibeJanelaPrincipal();
        
    }
    
}
