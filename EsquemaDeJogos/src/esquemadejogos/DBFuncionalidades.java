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
import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

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
    
    /*public JTextArea getDDL(String tablename)
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
    }*/
    
    /*Calls statement to get metadata from the table*/
    public String getMetaData(String tableName)
    {
        String metaData = new String();
        CallableStatement st;
       
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getMetaData(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.VARCHAR);
            st.setString(2, tableName);
            st.execute();
            metaData = st.getString(1);
            return metaData;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            jtAreaDeStatus.setText(ex.getStackTrace().toString());
        }
        return metaData;
    }
    /*Calls statement to get the table names from the database and show them in the combobox*/
    public void pegarNomesDeTabelasComboBox(JComboBox jc){
        String s = "";
        CallableStatement st;
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getTableName(?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setFetchSize(100);

            st.executeQuery();

            ResultSet rs = (ResultSet) st.getObject(1);

            while (rs.next())
            {
                jc.addItem(rs.getString("table_name"));
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta");
        }        
    }
    
    /*No need for cursor, only one value returned, need to fix later*/
     public int numeroDeColunas( String tableName )
    {        
        int a = 0;
        CallableStatement st;
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getNCol(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setString(2, tableName);
            st.setFetchSize(100);

            st.executeQuery();

            ResultSet rs = (ResultSet) st.getObject(1);

            while (rs.next())
                a = rs.getInt("COUNT(*)");
            rs.close();
            st.close();
        }
        catch (Exception ex) {
            jtAreaDeStatus.setText("Erro ao pegar numero de colunas");
        }
        
        return a;
   }
   public ResultSet nomeDeColunas( String tableName )
    {       
        ResultSet rs = null;
        CallableStatement st;
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getColName(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setString(2, tableName);
            st.setFetchSize(100);

            st.executeQuery();

            rs = (ResultSet)st.getObject(1);
        }
        catch (Exception ex) {
            jtAreaDeStatus.setText("Erro ao pegar nome de colunas");
        }
        
        return rs;
   }
    
    /* - - - Check - - - */
    public boolean isCheck (String tableName, String columnName)
    {
        ArrayList<String> isCheck = new ArrayList<>();
        Statement stmtCheck;
        ResultSet rsCheck;
        CallableStatement st;
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getCheck(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setString(2, tableName);
            st.setFetchSize(100);

            st.executeQuery();

            ResultSet rs = (ResultSet) st.getObject(1);
            while (rs.next()) {
                String condition = rs.getString("SEARCH_CONDITION");

                String parts[];

                if(condition.contains("('")){
                    parts = condition.split(" ");

                    isCheck.add(parts[0]);
                } else{
                    isCheck.add("");
                }
                
            }

            rs.close();
            st.close();
        }
        catch (Exception ex) {
            jtAreaDeStatus.setText("Erro ao verificar se existe check");
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
        CallableStatement st;
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getCheck(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setString(2, tableName);
            st.setFetchSize(100);

            st.executeQuery();

            ResultSet rs = (ResultSet) st.getObject(1);
             while (rs.next()) {
                String condition = rs.getString("SEARCH_CONDITION");

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

            rs.close();
            st.close();
        }
        catch (Exception ex) {
            jtAreaDeStatus.setText("Erro ao verificar se existe check");
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
        ResultSet rsFK;
        

        CallableStatement st;
        try {
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getFK(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setString(2, tableName);
            st.setFetchSize(100);

            st.executeQuery();

            ResultSet rs = (ResultSet) st.getObject(1);
            while (rs.next()) {
                String testingColumnName = rs.getString("COLUMN_NAME");
                if (testingColumnName.equals(columnName))
                    return true;
            }
        } catch (Exception ex) {
            jtAreaDeStatus.setText("Erro ao verificar se eh FK"+ex.getMessage());
        }
        
        return false;
    }
    
    public Vector<String> valuesFK (String tableName, String columnName)
    {
        Vector<String> fkValues = new Vector<>();
        Statement stmtFK;
        ResultSet rsSelectFK;
        ResultSet rsFK;
        
        fkValues.add("Selecione uma opcao");
        
        try{
            stmtFK = connection.createStatement();
            
            CallableStatement st;
            CallableStatement stmtSelectFK;
            st = this.connection.prepareCall("{ call db_Utilities_pkg.getFKValues(?, ?) }");
         
            st.registerOutParameter(1, OracleTypes.CURSOR);
            st.setString(2, tableName);
            st.setFetchSize(100);

            st.executeQuery();

            ResultSet rs = (ResultSet) st.getObject(1);
            while (rs.next()) {
                String testingColumnFK = rs.getString("COLUMN_NAME");
                String testingPosition = rs.getString("POSITION");
                String originalColumn = rs.getString("ORIGINALCOLUMN");
                String originalPosition = rs.getString("ORIGINALPOSITION");
                String originalTable = rs.getString("ORIGINALTABLE");
                
                if (testingColumnFK.equals(columnName) && testingPosition.equals(originalPosition)) {
                    
                   try{

                        st = this.connection.prepareCall("{ call db_Utilities_pkg.getFKOrigColName(?, ?, ?, ?, ?) }");

                        st.registerOutParameter(1, OracleTypes.CURSOR);
                        st.setString(2, originalTable);
                        st.setString(3, tableName);
                        st.setString(4, originalColumn);
                        st.setString(5, testingColumnFK);
                        st.setFetchSize(100);

                        st.executeQuery();

                        rsSelectFK = (ResultSet) st.getObject(1);

                        while (rsSelectFK.next()) {
                            String values = rsSelectFK.getString("NEWNAME");
                            fkValues.add(values);
                        }
                    }catch (Exception ex) {
                        jtAreaDeStatus.setText("Erro ao verificar origem de FK "+ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            jtAreaDeStatus.setText("Erro ao pegar valores de FK "+ex.getMessage());
        }
        
        return fkValues;
    }
    
    public void insertValuesBD(String tableName, ArrayList<String> colNames, ArrayList<String> colValues) {
        
        CallableStatement st;
        ArrayDescriptor descriptor;
        String error = new String();
        for(int i = 0; i < colNames.size(); i++){
            System.out.println("name: "+colNames.get(i)+ " val: "+colValues.get(i));
        }
        String output = new String();
        try {
            descriptor = ArrayDescriptor.createDescriptor("T_ATTNAMEARRAY", this.connection);
            String[] stringArray = colNames.toArray(new String[colNames.size()]);
            ARRAY atrNames = new ARRAY(descriptor, this.connection, stringArray);
            ArrayDescriptor descriptor2 = ArrayDescriptor.createDescriptor("T_ATTVALUEARRAY", this.connection);
            stringArray = colValues.toArray(new String[colValues.size()]);
            ARRAY atrValues = new ARRAY(descriptor2, this.connection, stringArray);
            st = this.connection.prepareCall("{ call  db_Operations_pkg.insert_procedure(?, ?, ?, ?) }");

            st.registerOutParameter(1, OracleTypes.VARCHAR);
            st.setString(2, tableName);
            st.setArray(3, atrNames);
            st.setArray(4, atrValues);
            st.execute();
            output = st.getString(1);
        } catch (SQLException ex) {
            error = ex.toString();
        }
        jtAreaDeStatus.setText("Insert Result: " + output + error);
    }
    
    public void removeValuesBD(String tableName, ArrayList<String> colNames, ArrayList<String> colValues){
        CallableStatement st;
        ArrayDescriptor descriptor;
        String error = new String();
        for(int i = 0; i < colNames.size(); i++){
            System.out.println("name: "+colNames.get(i)+ " val: "+colValues.get(i));
        }
        String output = new String();
        try {
            descriptor = ArrayDescriptor.createDescriptor("T_ATTNAMEARRAY", this.connection);
            String[] stringArray = colNames.toArray(new String[colNames.size()]);
            ARRAY atrNames = new ARRAY(descriptor, this.connection, stringArray);
            ArrayDescriptor descriptor2 = ArrayDescriptor.createDescriptor("T_ATTVALUEARRAY", this.connection);
            stringArray = colValues.toArray(new String[colValues.size()]);
            ARRAY atrValues = new ARRAY(descriptor2, this.connection, stringArray);
            st = this.connection.prepareCall("{ call  db_Operations_pkg.delete_procedure(?, ?, ?, ?) }");

            st.registerOutParameter(1, OracleTypes.VARCHAR);
            st.setString(2, tableName);
            st.setArray(3, atrNames);
            st.setArray(4, atrValues);
            st.execute();
            output = st.getString(1);
        } catch (SQLException ex) {
            error = ex.toString();
        }
        jtAreaDeStatus.setText("Delete Result: " + output + error);
    }
    
    void updateValuesBD(){
        
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
        /*SELEÇÃO*/
        CallableStatement stmt = null;
        CallableStatement stmt2 = null;
        CallableStatement stmt3 = null;
        try {
            stmt = this.connection.prepareCall("{ call db_Utilities_pkg.getEvrtng(?, ?) }");

            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.setString(2, tableName);
            stmt.setFetchSize(100);

            stmt.executeQuery();

            rsContent = (ResultSet) stmt.getObject(1);
        }catch (Exception ex) {
            jtAreaDeStatus.setText("Erro selecionando conteudo da tabela - "+ex.getMessage());
        }
        try{
            stmt2 = this.connection.prepareCall("{ call db_Utilities_pkg.getPKs(?, ?) }");

            stmt2.registerOutParameter(1, OracleTypes.CURSOR);
            stmt2.setString(2, tableName);
            stmt2.setFetchSize(100);

            stmt2.executeQuery();

            rsPK = (ResultSet) stmt2.getObject(1);
        }catch (Exception ex) {
            jtAreaDeStatus.setText("Erro selecionando PKs da tabela - "+ex.getMessage());
        }
        try{
            stmt3 = this.connection.prepareCall("{ call db_Utilities_pkg.getColName(?, ?) }");

            stmt3.registerOutParameter(1, OracleTypes.CURSOR);
            stmt3.setString(2, tableName);
            stmt3.setFetchSize(100);

            stmt3.executeQuery();

            rsColunms = (ResultSet) stmt3.getObject(1);
        }catch (Exception ex) {
            jtAreaDeStatus.setText("Erro selecionando nomes das colunas - "+ex.getMessage());
        }
        try{
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
                }
                j++;
            } 
            rsColunms.close();
            rsContent.close();
            rsPK.close();
            stmt.close();
            stmt2.close();
            stmt3.close();
        }
        catch (Exception ex) {
            jtAreaDeStatus.setText("Erro select - "+ex.getMessage());
        }
        
        /*Creates a new JTable with the information from the select*/
        jtSelect = new JTable(tableData, columnNames){
            
            @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
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
