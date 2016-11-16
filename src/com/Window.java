/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.actions.AgentsAction;
import com.actions.ExecuteAction;
import com.actions.OsAction;
import com.actions.PackageAction;
import com.actions.SendAction;
import com.actions.SolveAction;
import com.actions.StoreAction;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Honza
 */
public class Window extends javax.swing.JFrame implements Observer
{

    private final List<Agent> agents = new ArrayList<>();
    private final DefaultListModel<String> agentsModel;
    private final DefaultListModel<String> knowledgeModel;
    private final DefaultListModel<String> discoveredAgentsModel;
    private String jarPath;
    private final SimpleAttributeSet attributeSet = new SimpleAttributeSet();

    /**
     * Creates new form Window
     */
    public Window()
    {
        initComponents();
        try
        {
            CodeSource codeSource = Window.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            jarPath = jarFile.getParentFile().getPath();
        }
        catch (URISyntaxException ex)
        {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
        }

        agentsModel = new DefaultListModel<>();
        knowledgeModel = new DefaultListModel<>();
        discoveredAgentsModel = new DefaultListModel<>();

        agentsList.setModel(agentsModel);
        knowledgeList.setModel(knowledgeModel);
        discoveredAgentsList.setModel(discoveredAgentsModel);
        DefaultCaret dc = (DefaultCaret) outputText.getCaret();
        dc.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        System.setOut(new PrintStream(new ConsoleStream(Color.BLACK), true));
        System.setErr(new PrintStream(new ConsoleStream(Color.RED), true));

        messageField.setText("");
        ipField.setText(getLocalIp());
        String ip = getLocalIp();
        //messageField.setText("send " + ip + " 8000 send " + ip + " 10000 " + "solve " + "4+2");
        //messageField.setText("send " + ip + " 8000 " + "some msg");
        //messageField.setText("send " + ip + " 8000 " + "solve 4+4");
        //messageField.setText("solve 192.168.2.102 8000 4+2");
        //messageField.setText("package 192.168.2.102 8000 192.168.2.102 5000");
        //messageField.setText("package 192.168.2.102 8000 D:\\file1.txt D:\\file2.txt D:\\crazy_train.jpg D:\\MS.jar D:\\m.mp3");
        //messageField.setText("package 192.168.2.102 8000 D:\\crazy_train.jpg");
        //messageField.setText("file hash package 192.168.2.102 8000 D:\\file1.txt D:\\file2.txt");
        //messageField.setText("send 192.168.2.102 8000 ahoj");
        //messageField.setText("os");
        //messageField.setText("execute java -jar D:\\MS.jar");
        //messageField.setText("send 192.168.2.102 8000 package 192.168.2.102 5000 192.168.2.102 8000");

        try
        {
            addAgent("a", ipField.getText(), 5000);
            addAgent("b", ipField.getText(), 8000);
            addAgent("c", ipField.getText(), 10000);
        }
        catch (IOException | InterruptedException ex)
        {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getLocalIp()
    {
        try
        {
            return Inet4Address.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    private void addAgent(String name, String ip, int port) throws IOException, InterruptedException
    {
        Agent a = new Agent(name, port, ip);
        String type = "windows";
        if (name.equals("b"))
        {
            type = "linux";
        }
        if (name.equals("c"))
        {
            type = "mac";
        }
        a.addAction(new SendAction());
        a.addAction(new StoreAction());
        a.addAction(new SolveAction());
        a.addAction(new OsAction(type));
        a.addAction(new AgentsAction());
        a.addAction(new ExecuteAction());
        //a.addAction(new PackageAction(jarPath + File.separator + "MS.jar"));
        a.addAction(new PackageAction("C:\\Users\\Honza\\Documents\\NetBeansProjects\\MS\\dist\\MS.jar"));

        a.addObserver(this);
        a.start();
        agents.add(a);
        agentsModel.addElement(name);
        printOutput(String.format("Agent '%s' was created and listening at '%s:%d'", name, ip, port));
    }

    public Agent getSelectedAgent()
    {
        String agentName = agentsList.getSelectedValue();
        if (agentName == null)
        {
            return null;
        }
        return agents.stream().filter((a) -> a.getName().equals(agentName)).findFirst().orElse(null);
    }

    public synchronized void updateData(Agent a)
    {
        if (a != null)
        {
            List<String> discoveredAgents = a.getAgentDb().getAgentsList();
            discoveredAgentsModel.clear();
            for (String agentName : discoveredAgents)
            {
                discoveredAgentsModel.addElement(agentName);
            }

            List<String> agentKnowledges = a.getKnowledges();
            knowledgeModel.clear();
            for (String knowledge : agentKnowledges)
            {
                knowledgeModel.addElement(knowledge);
            }
        }
    }

    private synchronized void printOutput(String text)
    {
        appendTextToOutput(text);
    }

    private void appendTextToOutput(String text)
    {
        Document doc = outputText.getDocument();

        try
        {
            doc.insertString(doc.getLength(), text, attributeSet);
        }
        catch (BadLocationException ex)
        {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jScrollPane1 = new javax.swing.JScrollPane();
        agentsList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        agentNameField = new javax.swing.JTextField();
        createAgentButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        agentPortField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        ipField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        messageField = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        knowledgeList = new javax.swing.JList<>();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        discoveredAgentsList = new javax.swing.JList<>();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        outputText = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        agentsList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                agentsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(agentsList);

        jLabel2.setText("Agents");

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("Agent name");

        createAgentButton.setText("Create");
        createAgentButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                createAgentButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Port");

        jLabel7.setText("Ip");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(createAgentButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(agentNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(agentPortField, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                                    .addComponent(ipField))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(agentNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(agentPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createAgentButton)
                .addContainerGap())
        );

        jLabel3.setText("Create agent");

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sendButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Message for agent");

        jLabel6.setText("Agents outputs");

        jButton1.setText("Remove selected");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(knowledgeList);

        jLabel8.setText("Knowledges");

        jScrollPane4.setViewportView(discoveredAgentsList);

        jLabel9.setText("Discovered agents");

        jScrollPane5.setViewportView(outputText);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messageField, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(sendButton))
                            .addComponent(jLabel6))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendButton)
                    .addComponent(messageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createAgentButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createAgentButtonActionPerformed
    {//GEN-HEADEREND:event_createAgentButtonActionPerformed
        try
        {
            String agentName = agentNameField.getText();
            int agentPort = Integer.parseInt(agentPortField.getText());
            String ip = ipField.getText();
            InetAddress.getByName(ip);
            addAgent(agentName, ip, agentPort);
            agentNameField.setText("");
            agentPortField.setText("");
        }
        catch (IOException | InterruptedException | NumberFormatException e)
        {
            System.err.println(e.toString());
        }

    }//GEN-LAST:event_createAgentButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sendButtonActionPerformed
    {//GEN-HEADEREND:event_sendButtonActionPerformed
        // send message
        String message = messageField.getText();
        if (message.length() == 0)
        {
            return;
        }
        Agent selectedAgent = getSelectedAgent();
        if (selectedAgent != null)
        {
            selectedAgent.see(selectedAgent.getIp() + ":" + selectedAgent.getPort() + " " + message);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No agent is selected in agents list", "No agent", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_sendButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        // remove agent
        Agent selectedAgent = getSelectedAgent();
        if (selectedAgent != null)
        {
            try
            {
                selectedAgent.stop();
                agents.remove(selectedAgent);
                agentsModel.removeElement(selectedAgent.getName());
            }
            catch (IOException | InterruptedException ex)
            {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void agentsListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_agentsListValueChanged
    {//GEN-HEADEREND:event_agentsListValueChanged
        updateData(getSelectedAgent());
    }//GEN-LAST:event_agentsListValueChanged

    @Override
    public void update(Observable o, Object arg)
    {
        if (arg instanceof String)
        {
            String message = (String) arg;
            printOutput(message);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Window.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() ->
        {
            Window w = new Window();
            w.setLocationRelativeTo(null);
            w.setVisible(true);
        });
    }

    private class ConsoleStream extends ByteArrayOutputStream
    {

        private final SimpleAttributeSet attributes;

        public ConsoleStream(Color color)
        {
            attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, color);
        }

        @Override
        public void flush() throws IOException
        {
            String msg = toString();
            StyledDocument doc = outputText.getStyledDocument();
            try
            {
                doc.insertString(doc.getLength(), msg, attributes);
                reset();
            }
            catch (BadLocationException ex)
            {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField agentNameField;
    private javax.swing.JTextField agentPortField;
    private javax.swing.JList<String> agentsList;
    private javax.swing.JButton createAgentButton;
    private javax.swing.JList<String> discoveredAgentsList;
    private javax.swing.JTextField ipField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JList<String> knowledgeList;
    private javax.swing.JTextField messageField;
    private javax.swing.JTextPane outputText;
    private javax.swing.JButton sendButton;
    // End of variables declaration//GEN-END:variables
}
