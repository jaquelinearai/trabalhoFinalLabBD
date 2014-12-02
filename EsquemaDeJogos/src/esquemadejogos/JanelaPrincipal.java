package esquemadejogos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


public class JanelaPrincipal {
    
    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JPanel teste;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JTextArea jTAreaDDL;
    JTextField username;
    JTextField password;
    JPanel pPainelUserInfo;
    JScrollPane pPainelDDL = null;
    JTabbedPane tabbedPane;
    JScrollPane pPainelDeExibicaoDeDados = null;
    JTable jt;
    JTable selectTable;
    JPanel pPainelDeInsecaoDeDados;
    JPanel pPainelDeExcluirDados;
    JPanel pPainelDeAtualizarDados;
    JPanel pPainelDeResumoDeJogos;
    DBFuncionalidades bd;
    JPanelComponents pc;
    JButton buttonInsert;
    Boolean isOnDDL;
    Boolean isOnSel;
    Boolean isOnIns;
    
    public JanelaPrincipal(Connection connection) {
        j = new JFrame("ICMC-USP - SCC0541 - Pratica 10");
        j.setSize(700, 500);
        j.setLayout(new BorderLayout());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*Painel da parte superior (north) - com combobox e outras informações*/
        pPainelDeCima = new JPanel();
        j.add(pPainelDeCima, BorderLayout.NORTH);
        jc = new JComboBox();
        pPainelDeCima.add(jc);

        /*Painel da parte inferior (south) - com área de status*/
        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea();
        jtAreaDeStatus.setText("Aqui é sua área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);

        /*Painel tabulado na parte central (CENTER)*/
        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);

        /*Tab de exibicao*/
        /*Mudada para JScrollPane por JPanel nao exibir o titulo das JTables por algum motivo*/
        pPainelDeExibicaoDeDados = new JScrollPane();
        pPainelDeExibicaoDeDados.setLayout(new ScrollPaneLayout());
        tabbedPane.add(pPainelDeExibicaoDeDados, "Listar");
        
        /*Cria a tab de insercao*/
        pPainelDeInsecaoDeDados = new JPanel();
        tabbedPane.add(pPainelDeInsecaoDeDados, "Inserir");
        
        /*Cria a tab de atualizar*/
        pPainelDeAtualizarDados = new JPanel();
        tabbedPane.add(pPainelDeAtualizarDados, "Atualizar");
        
        /*Cria a tab de excluir*/
        pPainelDeExcluirDados = new JPanel();
        tabbedPane.add(pPainelDeExcluirDados, "Excluir");
        
        /*Cria a tab de resumo de jogos*/
        pPainelDeResumoDeJogos = new JPanel();
        tabbedPane.add(pPainelDeResumoDeJogos, "Resumo dos jogos");
        
        JButton connectBtn = new JButton(new AbstractAction("Connect") {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                getDDL((String) jc.getSelectedItem());
            }
        });
        
        pPainelDDL = new JScrollPane();
        pPainelDDL.setLayout(new ScrollPaneLayout());
        pPainelUserInfo = new JPanel();
        pPainelUserInfo.setLayout(new GridLayout(3, 2));
        pPainelUserInfo.add(new JLabel("Username"));
        username = new JTextField("Digite aqui");
        pPainelUserInfo.add(username);
        pPainelUserInfo.add(new JLabel("Password"));
        password = new JTextField("Digite aqui");
        pPainelUserInfo.add(password);
        pPainelUserInfo.add(connectBtn);
        pPainelDDL.setViewportView(pPainelUserInfo);
        //pPainelDDL.add(pPainelUserInfo);
        tabbedPane.add(pPainelDDL, "DDL");
                
        this.DefineEventos();
        j.setVisible(true);

        bd = new DBFuncionalidades(jtAreaDeStatus);
        bd.setConnection(connection);
        
        bd.pegarNomesDeTabelasComboBox(jc);
       
        /*Cria a JTable com os dados resultantes do select na tabela escolhida*/
        createSelectTable((String) jc.getItemAt(0));
    }
    private void createSelectTable(String table)
    {
        if(selectTable != null)
            pPainelDeExibicaoDeDados.remove(selectTable);
        selectTable = bd.preencherTableSelect(table);
        pPainelDeExibicaoDeDados.setViewportView(selectTable);
    }
    
    public void getDDL(String tablename)
    {
        
        /*if(selectTable != null)
            pPainelDeExibicaoDeDados.remove(selectTable);*/
        if(bd.connected == false)
        {
            System.out.println(username.getText());
            if( username.getText() == null || (0 == username.getText().compareTo("Digite aqui")) || password.getText() == null || (0 == password.getText().compareTo("Digite aqui")))
            {
                System.out.println("Username ou Senha em branco");
                return;
            }
            else
            {
                if(new Conection().conectar(username.getText(), password.getText()))
                {
                    bd.pegarNomesDeTabelasComboBox(jc);
                }
            }
        }
        jTAreaDDL = bd.getDDL(tablename);
        pPainelDDL.setViewportView(jTAreaDDL);
    }
    
    private void createInsertTab(String table)
    {
        /*Deleta o panel anterior para setar as novas informacoes*/
        pPainelDeInsecaoDeDados.removeAll();
        
        Vector<String> values;
        Vector<JPanelComponents> panelComponents = new Vector<JPanelComponents>();
        
        /*Seta as novas informacoes*/
        int nColunas = bd.numeroDeColunas(table);
        ResultSet rsNomeColunas = bd.nomeDeColunas(table);
        
        pPainelDeInsecaoDeDados.setLayout(new GridLayout(nColunas+1, 2));
        
        try{
            while (rsNomeColunas.next()) {
                
                pc = new JPanelComponents();
                
                pPainelDeInsecaoDeDados.add(new JLabel(rsNomeColunas.getString("COLUMN_NAME")));
               
                /* Eh uma clausula de check */
                if (bd.isCheck(table, rsNomeColunas.getString("COLUMN_NAME"))){
                    pc.cb = new JComboBox(bd.valuesCheck (table, rsNomeColunas.getString("COLUMN_NAME")));
                    pc.columnName = rsNomeColunas.getString("COLUMN_NAME");
                    pPainelDeInsecaoDeDados.add(pc.cb);
                    panelComponents.add(pc);
                }
                
                else if (bd.isFK (table, rsNomeColunas.getString("COLUMN_NAME"))) {
                    pc.cb = new JComboBox(bd.valuesFK(table, rsNomeColunas.getString("COLUMN_NAME")));
                    pc.columnName = rsNomeColunas.getString("COLUMN_NAME");
                    pPainelDeInsecaoDeDados.add(pc.cb);
                    panelComponents.add(pc);
                }
                /* Eh um campo normal */
                else {
                    pc.tf = new JTextField("Digite aqui");
                    pc.columnName = rsNomeColunas.getString("COLUMN_NAME");
                    pPainelDeInsecaoDeDados.add(pc.tf);
                    panelComponents.add(pc);
                }
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        buttonInsert = new JButton("Inserir");
        pPainelDeInsecaoDeDados.add(buttonInsert);
        
         buttonInsert.addActionListener(new ActionListener() {
            
            // Inserindo
            public void actionPerformed(ActionEvent e)
            {
                String strInsert = new String("");
                String strTF;

                for(int i = 0; i < nColunas; i++){
                    if(panelComponents.elementAt(i).cb == null) {
                        strTF = panelComponents.elementAt(i).tf.getText();
                        System.out.println(strTF);
                    }
                    
                    else {
                        strTF = (String) panelComponents.elementAt(i).cb.getSelectedItem();
                        System.out.println(strTF);
                    }
                    
                    if(i < nColunas-1)
                        strInsert += "'"+strTF+"', ";
                    
                    else
                        strInsert += "'"+strTF+"'";
                }
                
                System.out.println(strInsert);
                bd.insertValuesBD(table, strInsert);
                
                //E se for date
            }
        }); 
    }
     
    private void DefineEventos() {
        jc.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() instanceof JButton)
                {
                }
                
                else
                {
                    JComboBox jcTemp = (JComboBox) e.getSource();
                    String metadata;
                    /*Cria string com metadados sobre a tabela*/
                    metadata = (String) jcTemp.getSelectedItem() + "\n";
                    metadata += bd.getMetaData((String) jcTemp.getSelectedItem());
                    jtAreaDeStatus.setText(metadata);

                    /*Cria a JTable com os dados resultantes do select na tabela escolhida*/
                    createSelectTable((String) jcTemp.getSelectedItem());
                    
                    /*Cria os campos de insercao*/
                    createInsertTab((String) jcTemp.getSelectedItem());
                
                    if(bd.connected)
                    {
                        getDDL((String) jcTemp.getSelectedItem());
                    }
                }
            }
        });
    }
}
