package javafinalhangmanprojectclient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

class GameGUI extends Thread{
    JTextField username;
    JTextField server;
    Socket sock;
    static PrintStream sout;
    Scanner sin;
    boolean owner = JavaFinalHangmanProjectClient.gameOwner;
    boolean gameStart;
    String Word;
    String Incorrect;
    String WordProgress;
    JPanel InputWrapper; 
    JButton Send;
    JTextField inputText;
    JLabel Info;
    
    GameGUI(JTextField username1, JTextField server1, Socket sock1, PrintStream sout1, Scanner sin1){
        super();
        username = username1;
        server = server1;
        sock = sock1;
        sout = sout1;
        sin = sin1;
        gameStart = false;
        Word = "";
        Incorrect = "";
        WordProgress = "";
        Send = new JButton("Send");
        inputText = new JTextField(20);
        InputWrapper = new JPanel(); 
        Info = new JLabel("<html><font color='red'></font></html>");
    }
    
    @Override
    public void run(){
        String line;
        
        JFrame jfGAME = new JFrame("Hangman Client"); 
        jfGAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jfGAME.setSize(900,500); 
        jfGAME.setResizable(false);

        JPanel jpAllWrapperGAME = new JPanel(); 
        jpAllWrapperGAME.setLayout(new BorderLayout());
        jfGAME.add(jpAllWrapperGAME);

        //NORTH Game Group Name
        jpAllWrapperGAME.add(new JLabel("<html><h1>Hangman Group: "+server.getText()+"</html></h1>"), BorderLayout.NORTH);
        
        //WEST INCORRECT LETTERS
        JPanel InorrectWrapper = new JPanel();
        InorrectWrapper.setLayout(new BoxLayout(InorrectWrapper, BoxLayout.Y_AXIS));
        InorrectWrapper.add(new JLabel("<html><h4>Incorrect Letters</h4>"));
        JLabel IncorrectLetters = new JLabel("<html></html>");
        InorrectWrapper.add(IncorrectLetters);
        jpAllWrapperGAME.add(new JScrollPane(InorrectWrapper), BorderLayout.WEST);
        //EAST CORRECT LETTERS SO FAR
        JPanel CorrectWrapper = new JPanel();
        CorrectWrapper.setLayout(new BoxLayout(CorrectWrapper, BoxLayout.Y_AXIS));
        CorrectWrapper.add(new JLabel("<html><h4>Correct Letters</h4>"));
        JLabel CorrectLetters = new JLabel("<html></html>");
        CorrectWrapper.add(CorrectLetters);
        jpAllWrapperGAME.add(new JScrollPane(CorrectWrapper), BorderLayout.EAST);
        
        //CENTER HangMan Graphic
        HangmanPic HANGMANGRAPHICS = new HangmanPic(6);
        jpAllWrapperGAME.add(new JScrollPane(HANGMANGRAPHICS), BorderLayout.CENTER);

        jfGAME.setVisible(true);
        
        //SOUTH TEXTBOX
        //INPUT WRAPPER
        InputWrapper.setLayout(new BoxLayout(InputWrapper, BoxLayout.Y_AXIS));
        //OWNER
        if(owner&&!gameStart){
            //Owner needs to send word to guess, which also starts the game
            JPanel InputWrapperInput = new JPanel(); 
            
            //Word Input
            InputWrapperInput.add(inputText); 

            //Info Text
            Info.setText("<html><font color='red'></font></html>");
            
            //Start Game Button
            Send.setText("Start Game");
            Send.addActionListener(new StartButtonListener(inputText, sout, Info));
            InputWrapperInput.add(Send);

            InputWrapper.add(InputWrapperInput);
            InputWrapper.add(Info);
            
        }else if(!owner&&!gameStart){
            //Participant and game has not start
            InputWrapper.add(new JLabel("Please Wait for Game Owner to Start Game!"));
        }
        jpAllWrapperGAME.add(InputWrapper, BorderLayout.SOUTH);
        jfGAME.setVisible(true);

        while(sin.hasNext()){
            line = sin.nextLine();
            if ("START GAME".equals(line)){
                line = sin.nextLine();
                Word = line;
                gameStart = true;
                if(owner){
                    InputWrapper.removeAll();
                    InputWrapper.repaint();
                }else{
                    setUpParticipantStart(Send, false);
                }
            }else if("Your Turn".equals(line)){
                setUpParticipantStart(Send, true);
            }else if("Participant".equals(line)){
            }else if("WIN".equals(line)){
                finalMessage("YOU ARE A WINNER!!!");
                break;
            }else if("LOSE".equals(line)){
                finalMessage("OHH NO, YOU LOST!!!");
                break;
            }else if(line.startsWith("WP:")){
                String[] sep = line.split("WP:");
                CorrectLetters.setText("<html>"+sep[1]+"</html>");
            }else if(line.startsWith("INCORRECT:")){
                
                String[] sep = line.split("INCORRECT:");
                if(sep.length > 1){
                    IncorrectLetters.setText("<html>"+sep[1]+"</html>");
                    HANGMANGRAPHICS.refresh((int) Math.ceil((double)sep[1].length()/2));
                }else{
                    IncorrectLetters.setText("<html></html>");
                }
            }
        }
    }    
    
    void setUpParticipantStart(JButton Send, boolean enabled){
        InputWrapper.removeAll();
        
        JPanel InputWrapperInput = new JPanel(); 

        //Word Input
        InputWrapperInput.add(inputText); 
        inputText.setText("");

        //Start Game Button
        removeActionListeners(Send);
        Send.addActionListener((ActionEvent arg0) -> {
            if(inputText.getText().length() == 1){
                sout.println(inputText.getText());
                inputText.setText("");
                setUpParticipantStart(false);
            }else{
            }
        });
        InputWrapperInput.add(Send);
        
        if(!enabled){
            Send.setEnabled(false);
            inputText.setEditable(false);
            Info.setText("<html><font color='red'>Team Member's Turn </font></html>");
        }else{
            Send.setEnabled(true);
            inputText.setEditable(true);
            Info.setText("<html><font color='red'>Guess One Letter of the Word</font></html>");
        }
        
        //Info Text
        InputWrapper.add(InputWrapperInput);
        InputWrapper.add(Info);
        InputWrapper.repaint();
    }
    
    void removeActionListeners(JButton button) {
        if (button == null) {
            return;
        }
        ActionListener[] listeners = button.getActionListeners();
        if (listeners == null) {
            return;
        }
        for (ActionListener listener : listeners) {
            button.removeActionListener(listener);
        }
    }
    
    void setUpParticipantStart(boolean enabled){
        removeActionListeners(Send);
        Send.addActionListener((ActionEvent arg0) -> {
            if(inputText.getText().length() == 1){
                sout.println(inputText.getText());
                inputText.setText("");
                setUpParticipantStart(false);
            }
        });
        
        //Info Text
        Info.setText("<html><font color='red'>Guess One Letter of the Word</font></html>");
        if(!enabled){
            Send.setEnabled(false);
            inputText.setEditable(false);
            Info.setText("<html><font color='red'>Team Member's Turn</font></html>");
        }
    }
    
    //Game over message
    void finalMessage(String msg){
        JFrame jf = new JFrame("Hangman Client"); 
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(200,200); 
        jf.setResizable(false);
        JPanel jp = new JPanel();
        jp.add(new JLabel(msg));
        jf.add(jp);
        jf.setVisible(true);
    }
    
}
