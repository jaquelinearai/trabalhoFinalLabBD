package esquemadejogos;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JButton;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.oracore.OracleType;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.BLOB;


public class JanelaPrincipal {
    
    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JPanel teste;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JTextArea jtAreaGameDesc;
    JTextArea jTAreaDDL;
    JTextField username;
    JTextField password;
    JPanel pPainelUserInfo;
    JScrollPane pPainelDDL = null;
    JTabbedPane tabbedPane;
    JScrollPane pPainelDeExibicaoDeDados = null;
    JScrollPane panelJogos = null;
    JScrollPane panelGameDescription = null;
    JPanel pPainelDeJogos = null;
    JTable jt;
    JTable selectTable;
    JTable gameTable;
    JTable gameDescriptionTable;
    JPanel pPainelDeInsecaoDeDados;
    JPanel pPainelDeExcluirDados;
    JPanel pPainelDeAtualizarDados;
    JPanel pPainelDeResumoDeJogos;
    DBFuncionalidades bd;
    JPanelComponents pc;
    JButton buttonInsert;
    JButton buttonImage;
    JButton buttonRemove;
    FileInputStream img;
    Boolean isOnDDL;
    Boolean isOnSel;
    Boolean isOnIns;
    JLabel label;
    CallableStatement comando;
    String selectedGame = null;
    
    public JanelaPrincipal(Connection connection) {
        j = new JFrame("ICMC-USP - SCC0541 - Pratica Final");
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
        pPainelDeResumoDeJogos.setLayout( new GridLayout(1,3));
        
                
        pPainelDeJogos = new JPanel();
        pPainelDeJogos.setLayout( new GridLayout(1,3));
    
        panelJogos = new JScrollPane();
        panelJogos.setLayout(new ScrollPaneLayout());
        
        panelGameDescription = new JScrollPane();
        panelGameDescription.setLayout(new ScrollPaneLayout());
        
        
        label = new JLabel();
        pPainelDeJogos.add(label);
        
        buttonImage = new JButton("New Image");
        
        buttonImage.addActionListener(new ActionListener() {
            
                // Inserindo
                public void actionPerformed(ActionEvent e)
                {
                    JFileChooser openFile = new JFileChooser();
                    openFile.showOpenDialog(null);
                    File file = openFile.getSelectedFile();
                    try {
                        img = new FileInputStream(file);
                        insertImage(img, selectedGame);
                    }
                    catch(IOException e1) {
                        jtAreaDeStatus.setText(e1.toString());
                    }
                }
        });
    
        pPainelDeJogos.add(buttonImage);
        /*Cria a tab de imagem de jogos*/
        
        
        
        
        this.DefineEventos();
        j.setVisible(true);

        bd = new DBFuncionalidades(jtAreaDeStatus);
        bd.setConnection(connection);
        
        bd.pegarNomesDeTabelasComboBox(jc);
       
        /*Cria a JTable com os dados resultantes do select na tabela escolhida*/
        createSelectTable((String) jc.getItemAt(0));
        
        gameDescriptionTable = bd.preencherTableSelect("JOGO");
        panelGameDescription.setViewportView(gameDescriptionTable);
                
        gameTable = bd.preencherTableSelect("JOGO");
        panelJogos.setViewportView(gameTable);
        gameTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("Print Image");
                JTable target = (JTable)e.getSource();
                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();
                DefaultTableModel m =  (DefaultTableModel) target.getModel();
                Vector<String> r = (Vector<String>) m.getDataVector().get(row);
                selectedGame = r.get(column);
                BufferedImage img = getImage(r.get(4));
                ImageIcon icon=new ImageIcon(img); // ADDED
                label.setIcon(icon); // ADDED

                Dimension imageSize = new Dimension(icon.getIconWidth(),icon.getIconHeight()); // ADDED
                label.setPreferredSize(imageSize); // ADDED
            }
        });
        
        gameDescriptionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                JTable target = (JTable)e.getSource();
                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();
                DefaultTableModel m =  (DefaultTableModel) target.getModel();
                Vector<String> r = (Vector<String>) m.getDataVector().get(row);
                selectedGame = r.get(column);
                System.out.println(selectedGame);
                
                try {
                    comando = bd.connection.prepareCall("{ call pkg_atualizaJogo.procedure_descricaoJogo(?, ?) }");
                     comando.setString(1, selectedGame);
                      comando.registerOutParameter(2, java.sql.Types.VARCHAR);
                     comando.execute();
                     String str = comando.getString(2);
                     jtAreaGameDesc.setText(str);
                } catch (SQLException ex) {
                    Logger.getLogger(JanelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    jtAreaDeStatus.setText(ex.getStackTrace().toString());
                }
          
            }
        });
        
        jtAreaGameDesc = new JTextArea();
        pPainelDeResumoDeJogos.add(panelGameDescription);
        pPainelDeResumoDeJogos.add(jtAreaGameDesc);
        tabbedPane.add(pPainelDeResumoDeJogos, "Resumo dos jogos"); 
        //Cria a tab de jogos
        pPainelDeJogos.add(panelJogos);
        
        tabbedPane.add(pPainelDeJogos, "Jogo");
        
        
    }
    
    private void createSelectTable(String table)
    {
        if(selectTable != null)
            pPainelDeExibicaoDeDados.remove(selectTable);
        selectTable = bd.preencherTableSelect(table);
        pPainelDeExibicaoDeDados.setViewportView(selectTable);
    }
    public void insertImage(FileInputStream image, String gameName)
    {
        
        try {
            comando = bd.connection.prepareCall("{ call db_Utilities_pkg.insertImage(?, ?) }");
         
            comando.setString(1, gameName);
            comando.setBinaryStream(2, image);
            comando.execute();
        }
        catch (SQLException ex) {
            jtAreaDeStatus.setText(ex.getStackTrace().toString());
        } 
        
    }
    
    BufferedImage getImage(String gameName)
    {
        try {
            comando = bd.connection.prepareCall("{ call db_Utilities_pkg.getImage(?, ?) }");
         
            comando.registerOutParameter(1, OracleTypes.BLOB);
            comando.setString(2, gameName);
            comando.execute();
            Blob image = comando.getBlob(1);
            BufferedImage img = ImageIO.read(image.getBinaryStream());
            return img;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            jtAreaDeStatus.setText("Erro ao pegar imagem - "+ex.getStackTrace().toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            jtAreaDeStatus.setText("Erro de IO - "+ex.getStackTrace().toString());
        }
        return null;
    }
    
    /*public void getDDL(String tablename)
    {
        
        //if(selectTable != null)
            //pPainelDeExibicaoDeDados.remove(selectTable);
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
    }*/
    
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
            rsNomeColunas.close();
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            jtAreaDeStatus.setText(ex.getStackTrace().toString());
        }
        
        buttonInsert = new JButton("Inserir");
        pPainelDeInsecaoDeDados.add(buttonInsert);
        
         buttonInsert.addActionListener(new ActionListener() {
            
            // Inserindo
            public void actionPerformed(ActionEvent e)
            {
                String strInsert = new String("");
                String strTF;
                ArrayList<String> colNames = new ArrayList<>();
                ArrayList<String> colValues = new ArrayList<>();
                
                for(int i = 0; i < nColunas; i++){
                    if(panelComponents.elementAt(i).cb == null) {
                        /*strTF = panelComponents.elementAt(i).tf.getText();
                        System.out.println(strTF);*/
                        colValues.add((String) panelComponents.elementAt(i).tf.getText());
                        colNames.add((String) panelComponents.elementAt(i).columnName);
                    }
                    
                    else {
                        /*strTF = (String) panelComponents.elementAt(i).cb.getSelectedItem();
                        System.out.println(strTF);*/
                        colValues.add((String) panelComponents.elementAt(i).cb.getSelectedItem());
                        colNames.add((String) panelComponents.elementAt(i).columnName);
                    }
                }
                
                System.out.println(strInsert);
                bd.insertValuesBD(table, colNames, colValues);
                
                
                //E se for date
            }
        }); 
    }
     
    private void createDeleteTab(String table)
    {
        /*Deleta o panel anterior para setar as novas informacoes*/
        pPainelDeExcluirDados.removeAll();
        
        Vector<String> values;
        Vector<JPanelComponents> panelComponents = new Vector<JPanelComponents>();
        Vector<String> columnNames = new Vector<String>();
        /*Seta as novas informacoes*/
        int nColunas = bd.numeroDeColunas(table);
        ResultSet rsNomeColunas = bd.nomeDeColunas(table);
        
        pPainelDeExcluirDados.setLayout(new GridLayout(nColunas+1, 2));
        try{
            while (rsNomeColunas.next()) {
                columnNames.add(rsNomeColunas.getString("COLUMN_NAME"));
            }
            rsNomeColunas.close();
            CallableStatement st;
            try {
                for(int i = 0; i < columnNames.size(); i++){
                    pc = new JPanelComponents();
                    
                    st = bd.connection.prepareCall("{ call db_Utilities_pkg.getColDistValues(?, ?, ?) }");

                    st.registerOutParameter(1, OracleTypes.CURSOR);
                    st.setString(2, table);
                    st.setString(3, columnNames.get(i));
                    st.setFetchSize(100);

                    st.executeQuery();
                    values = new Vector<String>();
                    values.add("Selecione uma opcao");
                    pPainelDeExcluirDados.add(new JLabel(columnNames.get(i)));
                    pc.cb = new JComboBox(values);
                    pc.columnName = columnNames.get(i);
                    
                    ResultSet rs = (ResultSet) st.getObject(1);
                    while (rs.next()) {
                        pc.cb.addItem(rs.getString(columnNames.get(i)));
                    }
                    rs.close();
                    st.close();
                    pPainelDeExcluirDados.add(pc.cb);
                    panelComponents.add(pc);
                }
            } catch (Exception ex) {
                jtAreaDeStatus.setText("Erro ao pegar dados para exclusao"+ex.getMessage());
            }
            

            
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
            jtAreaDeStatus.setText(ex.getStackTrace().toString());
        }
        
        buttonRemove = new JButton("Remover");
        pPainelDeExcluirDados.add(buttonRemove);
        
         buttonRemove.addActionListener(new ActionListener() {
            
            // Inserindo
            public void actionPerformed(ActionEvent e)
            {
                String strRemove = new String("");
                String strTF;
                ArrayList<String> colNames = new ArrayList<>();
                ArrayList<String> colValues = new ArrayList<>();
                for(int i = 0; i < nColunas; i++){
                    if(panelComponents.elementAt(i).cb.getSelectedIndex() != 0){
                        colValues.add((String) panelComponents.elementAt(i).cb.getSelectedItem());
                        colNames.add((String) panelComponents.elementAt(i).columnName);
                    }
                }
                
                System.out.println(strRemove);
                bd.removeValuesBD(table, colNames, colValues);
                
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
                
                    /*Cria os campos para delecao*/
                    createDeleteTab((String) jcTemp.getSelectedItem());
                    /*if(bd.connected)
                    {
                        getDDL((String) jcTemp.getSelectedItem());
                    }*/
                }
            }
        });
    }
}
