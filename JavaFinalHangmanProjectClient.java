package javafinalhangmanprojectclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//IMPORTANT!!!
//Server must be started Before any clients can connect

public class JavaFinalHangmanProjectClient {
    static int serverReady = 0;
    static boolean gameOwner = false;
    public static void main(String[] args) {
        try {
            Socket sock = new Socket("localhost", 5190);
            PrintStream sout = new PrintStream(sock.getOutputStream());
            Scanner sin = new Scanner(sock.getInputStream());
            
            JFrame jf = new JFrame("Hangman Client Setup"); 
            
            //SETUP UI (Get username and server preference)
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setSize(400,400); 
            jf.setResizable(true);

            JPanel jpAllWrapper = new JPanel(); 
            jf.add(jpAllWrapper);

            //username
            JTextField username = new JTextField(20);
            JPanel usernamePanel = new JPanel(); 
            usernamePanel.add(new JLabel("Username:"));
            usernamePanel.add(username);
            jpAllWrapper.add(usernamePanel);

            //server
            JTextField serverPref = new JTextField(20);
            JPanel serverPanel = new JPanel(); 
            serverPanel.add(new JLabel("Game Group:"));
            serverPanel.add(serverPref);
            jpAllWrapper.add(serverPanel);
            
            //info Text
            JPanel infoText = new JPanel(); 
            infoText.add(new JLabel("To Create a Game Server, Enter a New Game Group Name"));
            jpAllWrapper.add(infoText);
            
            JPanel infoText1 = new JPanel(); 
            infoText1.add(new JLabel("To Join an Existing Game Group, Enter the Existing Game Group Name"));
            jpAllWrapper.add(infoText1);
            
            JPanel errorPanel = new JPanel(); 
            JLabel errorLabel = new JLabel("");
            errorPanel.add(errorLabel);
            jpAllWrapper.add(errorPanel);

            //Submit
            JButton submitSetup = new JButton("Join Existing");
            submitSetup.addActionListener(new SetupButtonListener(username, serverPref, jf, sock, sout, sin, 0, errorLabel));
            jpAllWrapper.add(submitSetup);
            
            JButton submitNewSetup = new JButton("Create New Server");
            submitNewSetup.addActionListener(new SetupButtonListener(username, serverPref, jf, sock, sout, sin, 1, errorLabel));
            jpAllWrapper.add(submitNewSetup);

            jf.setVisible(true);
            
        } catch (IOException ex) {
            System.out.println("Error listening on port 5190");
        }
    }
}
        

class StartButtonListener implements ActionListener{
    JTextField word;
    PrintStream sout;
    JLabel Info;
    
    StartButtonListener(JTextField word1, PrintStream sout1, JLabel Info1){
        word = word1;
        sout = sout1;
        Info = Info1;
    }
    
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if(word.getText().length() != 0){
            sout.println(word.getText());
            word.setText("");
        }else{
            Info.setText("<html><font color='red'>Please dont send a empty message!</font></html>");
        }
    }
}

class SetupButtonListener implements ActionListener{
    JTextField username;
    JTextField server;
    JFrame jf;
    Socket sock;
    PrintStream sout;
    Scanner sin;
    int newServerStatus;
    JLabel errorLabel;
    
    SetupButtonListener(JTextField username1, JTextField server1, JFrame jf1, Socket sock1, PrintStream sout1, Scanner sin1, int newServerStatus1, JLabel errorLabel1){
         username = username1;
         server = server1;
         jf = jf1;
         sock = sock1;
         sout = sout1;
         sin = sin1;
         newServerStatus = newServerStatus1;
         errorLabel = errorLabel1;
    }
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if((username.getText().length() != 0) && (server.getText().length() != 0)){
            String line;

            //Make Server Connection
            //Declare if new Server
            if(newServerStatus == 1){
                sout.println("New Server");
            }else{
                sout.println("Existing Server");
            }

            //Verify Server
            sout.println(server.getText());

            line = sin.nextLine();
            if(("Error".equals(line)) && (newServerStatus == 1)){
                errorLabel.setText("ERROR: Please Choose a Different Existing Game Group Name!");
            }else if(("Error".equals(line)) && (newServerStatus == 0)){
                errorLabel.setText("ERROR: Please Enter a Valid Existing Game Group Name!");
            }else if ("Server GOOD".equals(line)){

                //Server verification complete

                //Verify Username and Can Join Game
                sout.println(username.getText());
                line = sin.nextLine();
                if("Error".equals(line)){
                    //Group not available to join
                    errorLabel.setText("ERROR: Please Choose Another Game Group! The Game Group Chosen is Either Full or Has Already Started its Game.");
                }else if("Username Good".equals(line)){
                    //Success with all setup processes and Start Game GUI
                    line = sin.nextLine();
                    JavaFinalHangmanProjectClient.gameOwner = "Owner".equals(line);
                    JavaFinalHangmanProjectClient.serverReady = 1;
                    jf.setVisible(false);
                    new GameGUI(username, server, sock, sout, sin).start();
                }
            }
        }
    }
}