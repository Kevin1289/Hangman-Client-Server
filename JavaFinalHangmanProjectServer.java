package javafinalhangmanprojectserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;


//IMPORTANT!!!
//Server must be started Before any clients can connect

public class JavaFinalHangmanProjectServer {
    
    static List<GameServer> servers = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(5190);
            
            //Constantly look for new clients and connect them to servers
            while (true){
                Socket client_sock = ss.accept();
                new ProcessConnection(client_sock, servers).start();
            }
            
        } catch (IOException ex) {
            System.out.println("Could not listen on port 5190");
        }
    }
}

class ProcessConnection extends Thread{
    Socket client_sock;
    static List<GameServer> servers;
    GameServer activeServer;
    String username;
    int newServerStatus; //want to make new server or not
    
    ProcessConnection(Socket newClientSock, List<GameServer> servers1){
        client_sock = newClientSock;
        servers = servers1;
        username = "";
    }
    
    @Override
    public void run(){
        try{
            Scanner sin = new Scanner(client_sock.getInputStream());
            PrintStream sout = new PrintStream(client_sock.getOutputStream());

            String server = "";
            booleanServer boolServ;
            
            //Get username and server
            while(("".equals(server)) && ("".equals(username))){
                //see if user wants to make new server

                String line = sin.nextLine();
                if(line.equals("New Server")){
                    newServerStatus = 1;
                }else if ("Existing Server".equals(line)){
                    newServerStatus = 0;
                }
                
                //Differect actions if want to make new server or not
                server = sin.nextLine();
                if(newServerStatus == 1){
                    //Make new server
                    
                    boolServ = checkIfExistingServerName(server);
                    if(boolServ.status){
                        //Server Name Already Exists
                        //Send Error Message
                        sout.println("Error");
                        server = "";
                        username = "";
                    }else{
                        //Server Name Validated
                        sout.println("Server GOOD");
                        
                        //Set up new Game Group
                        GameServer newServer = new GameServer(server, client_sock);
                        activeServer = newServer;
                        servers.add(newServer);
                        
                        //get username
                        username = sin.nextLine();
                    }
                }else{
                    // Join Existing Server
                    
                    boolServ = checkIfExistingServerName(server);
                    if(!boolServ.status){
                        //Server Does not Exist
                        //Return Error
                        sout.println("Error");
                        server = "";
                        username = "";
                    }else{
                        //Server Exists
                        sout.println("Server GOOD");
                        
                        //Check Participant Limit and If Game Started
                        line = sin.nextLine();
                        if(!boolServ.server.gameStarted && (boolServ.server.clients.size() < 4)){
                            // Space Open and Game Did Not Start
                            activeServer = boolServ.server;
                            //Get Username
                            sout.println("Username Good");
                            //Set Up New Game Group
                            boolServ.server.addClient(client_sock);
                            username = line;
                        }else{
                            //Game Started or No Space Open
                            sout.println("Error");
                            server = "";
                            username = "";
                        }
                        
                        
                    }
                }
                
            }
            //Username and Server Set Up
            sout.println("Username Good");
            
            //Owner Server Handles all Interaction Between Clients 
            if(newServerStatus==1){
                //Owner Server Actions
                
                sout.println("Owner");

                //Recieve Word From Owner
                String line = sin.nextLine();
                activeServer.Word = line;
                activeServer.WordLeft = line;

                //Start Game Announcement, Send Answer Word
                sout.println("START GAME");
                sout.println(activeServer.Word);
                for(Socket client: activeServer.clients){
                    PrintStream soutClient = new PrintStream(client.getOutputStream());
                    soutClient.println("START GAME");
                    soutClient.println(activeServer.Word);
                }
                
                //Handle Game Interaction
                while((activeServer.WordLeft.length() > 0) && (activeServer.Errors < 6)){
                    //Loop Through Group Participants
                    for(Socket client: activeServer.clients){
                        
                        //Send Correct Word Progress
                        sendAll("WP:" + blankMaker(activeServer.Word, activeServer.LettersCorrect), 3);
                        
                        //Send Incorrect Word Progress
                        sendAll("INCORRECT:" + activeServer.LettersIncorrect, 3);
                        
                        if((activeServer.WordLeft.length() <= 0)||(activeServer.Errors >= 6)){
                            //Word Sucessfully Guessed or Error Limit Reached
                            break;
                        }
                        
                        //Ask Participant to send Letter
                        PrintStream soutClient = new PrintStream(client.getOutputStream());
                        Scanner sinClient = new Scanner(client.getInputStream());
                        soutClient.println("Your Turn");
                        
                        //Get Letter From Participant
                        String letter = sinClient.nextLine();
                        ProcessLetter(activeServer, letter);
                        
                        //Send Letter Guessed to All Players
                        sendAll("Letter Guessed: " + letter, 3);
                    }
                }

                //Game Ended
                if(activeServer.Errors < 6){
                    //Player Wins
                    sendAll("WIN", 2);
                    sendAll("LOSE", 1);
                }else{
                    //Owner Wins
                    sendAll("WIN", 1);
                    sendAll("LOSE", 2);
                }
                
                //Clean Up Server by Closing All Client Server Connection and Own Connection
                removeServer();
                client_sock.close();
            }else{
                //Participant Server Tasks Complete
                //Remaining Tasks Including Closing Socket is Done on Game Owner Server
                sout.println("Participant");
            }
            
        }
        catch(IOException e){
            System.out.println("Error reading client message");
        }
    }
    
    //Input Current Game Server and New Letter From Participant
    void ProcessLetter(GameServer activeServer, String letter){
        if(activeServer.WordLeft.contains(letter)){
            //Letter is Correct
            activeServer.WordLeft = activeServer.WordLeft.replace(letter, "");
            activeServer.LettersCorrect += letter;
        }else{
            //Letter Is Incorrect
            activeServer.Errors += 1;
            if(activeServer.LettersIncorrect.length() ==0){
                activeServer.LettersIncorrect+=letter;
            }else{
                activeServer.LettersIncorrect+=','+letter;
            }
        }
    }
    
    //Remove All Participant Sockets Server By Closing Them
    void removeServer(){
        for(Socket clientSocket: activeServer.clients){
            try{
                clientSocket.close();
            }
            catch(IOException e){
                System.out.println("Error reading client message");
            }
        }
        servers.remove(activeServer);
    }
    
    //Input: Game Group Name (String)
    //Output: If Currently Exists Game Server With Such Name (Boolean)
    booleanServer checkIfExistingServerName(String newServerName){
        for(GameServer server: servers){
            if(server.serverName.equals(newServerName)){
                return new booleanServer(true, server);
            }
        }
        return new booleanServer(false);
    }
    
    //Make Blank String To Sent To Players
    String blankMaker(String word, String LetterCorrect){
        String result = "";
        for (int i = 0; i < word.length(); i++) {
            if(LetterCorrect.indexOf(word.toCharArray()[i])!=-1){
                result+=word.toCharArray()[i];
            }else{
                result+="_";
            }
        }
        return result;
    }
    
    //Send Message to All Players and Owner Combination
    void sendAll(String msg, int mode){
        try{
            if((mode == 1) || (mode == 3)){
                PrintStream sout = new PrintStream(client_sock.getOutputStream());
                sout.println(msg);
            }
            if((mode == 2) || (mode == 3)){
                for(Socket client: activeServer.clients){
                    PrintStream soutClient = new PrintStream(client.getOutputStream());
                    soutClient.println(msg);
                }
            }
        }
        catch(IOException e){
            System.out.println("Error Sending Message To All");
        }
    }
}

//Class to Showing Server and Boolean
class booleanServer{
    boolean status;
    GameServer server;
    
    booleanServer(boolean status1, GameServer server1){
        status = status1;
        server = server1;
    }
    
    booleanServer(boolean status1){
        status = status1;
    }
}
    
//Server For Each Game
class GameServer{
    String serverName;
    Socket owner;
    List<Socket> clients = new ArrayList<>();
    boolean gameStarted;
    String Word;
    String WordLeft;
    int Errors;
    String LettersCorrect;
    String LettersIncorrect;
    
    
    GameServer(String serverName1, Socket owner1){
        serverName = serverName1;
        owner = owner1;
        gameStarted = false;
        Word = "";
        WordLeft = "";
        LettersCorrect = "";
        LettersIncorrect="";
    }
    
    //Add Participant to Game Group
    void addClient(Socket client_sock){
        clients.add(client_sock);
    }

}
