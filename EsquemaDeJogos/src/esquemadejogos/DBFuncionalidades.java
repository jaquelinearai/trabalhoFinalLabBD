/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package esquemadejogos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class DBFuncionalidades {
    Connection connection;
    Statement stmt;
    Statement stmt2;
    Statement stmt3;
    ResultSet rs;
    ResultSet rsColunms;
    ResultSet rsContent;
    ResultSet rsPK;
    JTextArea jtAreaDeStatus;
    Vector<String> columnNames;
    Vector<String> pkColumns;
    Vector<Vector<String>> tableData;
    Boolean connected = false;
    
    public DBFuncionalidades(JTextArea jtaTextArea){
        jtAreaDeStatus = jtaTextArea;
    }

    public DBFuncionalidades() {
        
    }
    
    public void setConnection(Connection connection)
    {
        this.connection = connection;
    }
    
    public JTextArea getDDL(String tablename)
    {
        Statement s;
        CallableStatement cs;
        ResultSet rsddl;
        JTextArea jtaddl = new JTextArea();
        String sddl = new String();
        try
        {
            cs = connection.prepareCall("{ call dbms_metadata.set_transform_param(dbms_metadata.session_transform,'STORAGE', false) }");
            cs.execute();
            cs = connection.prepareCall("{ call dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SEGMENT_ATTRIBUTES',false) }");
            cs.execute();
            cs = connection.prepareCall("{ call dbms_metadata.set_transform_param(dbms_metadata.session_transform,'REF_CONSTRAINTS',false) }");
            cs.execute();
            cs = connection.prepareCall("{ call dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SQLTERMINATOR',true) }");
            cs.execute();
            s = connection.createStatement();
            rsddl = s.executeQuery("select DBMS_METADATA.GET_DDL(object_type,object_name) from user_objects where object_type = 'TABLE' AND object_name = '"+tablename+"'");
            while(rsddl.next()){
                //System.out.println(rsddl.getString(1));
                sddl += rsddl.getString(1);
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        ResultSet rsFKS;
        try
        {
            cs = connection.prepareCall("{ call dbms_metadata.set_transform_param(dbms_metadata.session_transform,'SQLTERMINATOR',true) }");
            cs.execute();
            s = connection.createStatement();
            rsFKS = s.executeQuery("select dbms_metadata.get_ddl('REF_CONSTRAINT', c.constraint_name) from user_constraints c where c.constraint_type = 'R' AND c.table_name = '"+tablename+"'");
            while(rsFKS.next()){
                //System.out.println(rsFKS.getString(1));
                sddl += rsFKS.getString(1);
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        jtaddl.setText(sddl);
        return jtaddl;
    }
    
    public String getMetaData(String tableName)
    {
        String metaData = new String();
        Statement st;
        ResultSet rsmd;
        try
        {
            st = connection.createStatement();
            /*Search for the metadata and adds it to the string to be shown in the lower part of the window*/
            rsmd = st.executeQuery("SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NUM_NULLS, NUM_DISTINCT, "
                    + "DATA_DEFAULT, COLUMN_ID, NULLABLE from USER_TAB_COLUMNS where table_name = '" + tableName + "'");
                while (rsmd.next()) {
                    metaData += "ID ="+rsmd.getString("COLUMN_ID")+" |NAME ="+rsmd.getString("COLUMN_NAME") 
                        +" |TYPE ="+rsmd.getString("DATA_TYPE")+" |LENGTH ="+rsmd.getString("DATA_LENGTH")+" |NULL? ="+rsmd.getString("NULLABLE")
                            + " |DEFAULT =" + rsmd.getString("DATA_DEFAULT")+" |#NULLS = " + rsmd.getString("NUM_NULLS")+" |#DISTINCT = "+rsmd.getString("NUM_DISTINCT")+"\n";
                }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return metaData;
    }
        
    public void pegarNomesDeTabelasComboBox(JComboBox jc){
        String s = "";
        try {
            s = "SELECT table_name FROM user_tables";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                jc.addItem(rs.getString("table_name"));
            }
            stmt.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta");
        }        
    }
    
    public ResultSet pegarNomesDeTabelas(){
        String s = "";
        try {
            s = "SELECT table_name FROM user_tables";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta");
        }       
        return rs;
    }
    
     public int numeroDeColunas( String tableName )
    {        
        int a = 0;
        try{
            /*SELEÇÃO*/
            stmt = connection.createStatement();

            /*Return the number of columns from the table*/
            rsColunms = stmt.executeQuery("SELECT COUNT(*) from USER_TAB_COLUMNS where table_name = '" + tableName + "'");
            
            /*Save the result*/
            while (rsColunms.next())
                a = rsColunms.getInt("COUNT(*)");
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        return a;
   }
     
    public ResultSet nomeDeColunas( String tableName )
    {        
        columnNames = new Vector();
        try{
            /*SELEÇÃO*/
            stmt = connection.createStatement();
            stmt2 = connection.createStatement();
            rsContent = stmt.executeQuery("SELECT * FROM "+tableName);
            
            /*Return the column names from the table*/
            rsColunms = stmt2.executeQuery("SELECT COLUMN_NAME from USER_TAB_COLUMNS where table_name = '" + tableName + "'");
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        return rsColunms;
   }
    
    /* - - - Check - - - */
    public boolean isCheck (String tableName, String columnName)
    {
        ArrayList<String> isCheck = new ArrayList<>();
        Statement stmtCheck;
        ResultSet rsCheck;
        
        try{
            stmtCheck = connection.createStatement();
            rsCheck = stmtCheck.executeQuery("SELECT CONSTRAINT_NAME, SEARCH_CONDITION FROM "+
                    "ALL_CONSTRAINTS WHERE TABLE_NAME ='"+tableName+"' AND CONSTRAINT_TYPE='C'");
        
            while (rsCheck.next()) {
                String condition = rsCheck.getString("SEARCH_CONDITION");

                String parts[];

                if(condition.contains("('")){
                    parts = condition.split(" ");

                    isCheck.add(parts[0]);
                } else{
                    isCheck.add("");
                }
                
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        if(containsCheck(isCheck, columnName))
            return true;
        else return false;
    }
        
    public Vector<String> valuesCheck (String tableName, String columnName)
    {
        ArrayList<String> isCheck = new ArrayList<>();
        Vector<String> checksValues = new Vector<>();
        int i=0;
        Statement stmtCheck;
        ResultSet rsCheck;
        
        try{
            stmtCheck = connection.createStatement();
            rsCheck = stmtCheck.executeQuery("SELECT CONSTRAINT_NAME, SEARCH_CONDITION FROM "+
                    "ALL_CONSTRAINTS WHERE TABLE_NAME ='"+tableName+"' AND CONSTRAINT_TYPE='C'");
        
            while (rsCheck.next()) {
                String condition = rsCheck.getString("SEARCH_CONDITION");

                String strParse[];

                if(condition.contains("('")){
                    strParse = condition.split(" ");
                    
                    for(i=2; i<strParse.length; i++){

                            String a[] = strParse[i].split("'");
                            int j = 1;
                            
                            while (j < a.length) {
                                checksValues.add(a[j]);
                                j+=2;
                            }
                        }

                    
                    isCheck.add(strParse[0]);
                } else{
                    isCheck.add("");
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        return checksValues;
    }
    
    private boolean containsCheck(ArrayList<String> isCheck, String columnName){
        
        for(int i=0; i<isCheck.size(); i++)
            if(isCheck.get(i).equalsIgnoreCase(columnName))
                return true;
        
        return false;
    }

    /* - - - FK - - - */
    public boolean isFK (String tableName, String columnName)
    {
        Statement stmtFK;
        ResultSet rsFK;
        
        try{
            stmtFK = connection.createStatement();
            
            rsFK = stmtFK.executeQuery("SELECT A.TABLE_NAME, A.COLUMN_NAME "+
                    "FROM ALL_CONS_COLUMNS A "+
                    "JOIN ALL_CONSTRAINTS C ON A.OWNER=C.OWNER "+
                    "AND A.CONSTRAINT_NAME=C.CONSTRAINT_NAME "+
                    "WHERE A.TABLE_NAME='"+tableName+"' AND C.CONSTRAINT_TYPE='R'");
        
            while (rsFK.next()) {
                String testingColumnName = rsFK.getString("COLUMN_NAME");
                if (testingColumnName.equals(columnName))
                    return true;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        return false;
    }
    
    public Vector<String> valuesFK (String tableName, String columnName)
    {
        Vector<String> fkValues = new Vector<>();
        Statement stmtFK;
        Statement stmtSelectFK;
        ResultSet rsSelectFK;
        ResultSet rsFK;
        
        fkValues.add("Selecione uma opcao");
        
        try{
            stmtFK = connection.createStatement();
            stmtSelectFK = connection.createStatement();
            
            rsFK = stmtFK.executeQuery("SELECT A.TABLE_NAME, A.COLUMN_NAME, A.POSITION, A.CONSTRAINT_NAME, "+
                    "C_PK.TABLE_NAME AS ORIGINALTABLE, C_PK.CONSTRAINT_NAME R_PK, UCC.COLUMN_NAME AS ORIGINALCOLUMN, UCC.POSITION AS ORIGINALPOSITION "+
                    "FROM ALL_CONS_COLUMNS A "+
                    "JOIN ALL_CONSTRAINTS C ON A.OWNER = C.OWNER "+
                    "AND A.CONSTRAINT_NAME = C.CONSTRAINT_NAME "+
                    "JOIN ALL_CONSTRAINTS C_PK ON C.R_OWNER = C_PK.OWNER "+
                    "AND C.R_CONSTRAINT_NAME = C_PK.CONSTRAINT_NAME "+
                    "JOIN USER_CONS_COLUMNS UCC ON UCC.TABLE_NAME = C_PK.TABLE_NAME "+
                    "AND UCC.CONSTRAINT_NAME = C_PK.CONSTRAINT_NAME "+
                    "WHERE A.TABLE_NAME ='"+tableName+"' AND C.CONSTRAINT_TYPE='R'");
        
            while (rsFK.next()) {
                String testingColumnFK = rsFK.getString("COLUMN_NAME");
                String testingPosition = rsFK.getString("POSITION");
                String originalColumn = rsFK.getString("ORIGINALCOLUMN");
                String originalPosition = rsFK.getString("ORIGINALPOSITION");
                String originalTable = rsFK.getString("ORIGINALTABLE");
                
                
                if (testingColumnFK.equals(columnName) && testingPosition.equals(originalPosition)) {
                    
                    try{
                        rsSelectFK = stmtSelectFK.executeQuery("SELECT UNIQUE O."+originalColumn+" AS NEWNAME "+
                        "FROM "+originalTable+" O, "+tableName+" T where O."+originalColumn+" = T."+testingColumnFK+" ");


                        while (rsSelectFK.next()) {
                            String values = rsSelectFK.getString("NEWNAME");
                            fkValues.add(values);
                        }
                    }catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        return fkValues;
    }
    
    public void insertValuesBD(String tableName, String strInsert) {
        
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("INSERT INTO "+tableName+" VALUES("+strInsert+")");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
     
    /*Cria a JTable com os dados resultantes do select na tabela escolhida*/
    /*Por enquanto, soh imprime os dados na tela*/
    public JTable preencherTableSelect( String tableName )
    {
        
        JTable jtSelect = null;
        columnNames = new Vector();
        tableData = new Vector<>();
        pkColumns = new Vector();
        System.out.println(tableName);
        try{
            /*SELEÇÃO*/
            stmt = connection.createStatement();
            stmt2 = connection.createStatement();
            stmt3 = connection.createStatement();
            rsContent = stmt.executeQuery("SELECT * FROM "+tableName);
            
            /*Return the name of the columns that are primary keys*/
            rsPK = stmt3.executeQuery("SELECT cols.table_name, cols.column_name, cols.position, cons.status, cons.owner "
                    + "FROM all_constraints cons, all_cons_columns cols "
                    + "WHERE cols.table_name = '"+tableName+"' "
                    + "AND cons.constraint_type = 'P' "
                    + "AND cons.constraint_name = cols.constraint_name "
                    + "AND cons.owner = cols.owner "
                    + "ORDER BY cols.table_name, cols.position");
            /*Return the column names from the table*/
            rsColunms = stmt2.executeQuery("SELECT COLUMN_NAME from USER_TAB_COLUMNS where table_name = '" + tableName + "'");
            /*Save the results in vectors*/
            while (rsPK.next()) {
                pkColumns.add(rsPK.getString("COLUMN_NAME"));
            }
            while (rsColunms.next()) {
                columnNames.add(rsColunms.getString("COLUMN_NAME"));
            }
            
            int j = 0;
            while (rsContent.next()) {
                tableData.add(new Vector());
                for(int i = 0; i < columnNames.size(); i++)
                {
                    tableData.get(j).add(rsContent.getString(columnNames.get(i)));
                    System.out.println("Peguei um conteudo"+" "+i+" "+rsContent.getString(columnNames.get(i)));
                }
                j++;
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        /*Creates a new JTable with the information from the select*/
        jtSelect = new JTable(tableData, columnNames){
            
            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
            /* !!!!!!!!!!!!!!!!!!!!!!!!!! PROBLEM DETECTED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
            
            /*This method changes the background color from the columns that are primary keys*/
            /*public Component prepareRenderer( TableCellRenderer r, int rw, int col)
            {
            Component c = super.prepareRenderer(r, rw, col);
            c.setBackground(Color.WHITE);
            System.out.println("oi"+" "+col);
            if(pkColumns.contains(columnNames.get(col)))
            {
                System.out.println("oioi");
                c.setBackground(Color.GREEN);
            }
            return c;
            }*/
        };
        return jtSelect;
    }
    
    public void exibeDados(JTable tATable, String sTableName){
        
    }
    //public void preencheComboBoxComRestricoesDeCheck
    //public void preencheComboBoxComValoresReferenciados
    //
}
