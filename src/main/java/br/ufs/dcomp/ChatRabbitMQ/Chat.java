package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.util.Date;

import java.io.IOException;

public class Chat {

  // server
  private static final String serverUri = "amqp://eduardo:199512@54.165.36.37";
  private static Connection connection;

  private static String user = "";
  private static String sendTo = "a";
  private static String command = "";
  private static String type = "";

  // utils
  private static Scanner scanner;

  /**
   * Escuta mensagens
   * 
   * @throws Exception
   */
  public static void listenMessages() throws Exception {
    String QUEUE_NAME = user + "_messages";
    Channel channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

    // Definindo o Consumidor
    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {

        String msg = new String(body, "UTF-8");
        System.out.println("\n=> " + msg);
        printComandType();
      }
    };
    channel.basicConsume(QUEUE_NAME, true, consumer);
  }

  /**
   * Imprime o tipo de comando na tela (daaah)
   */
  public static void printComandType() {
    if (type == "@") {
      System.out.print("@" + sendTo + " >> ");
    } else if (type == "#") {
      System.out.print("#" + sendTo + " >> ");
    } else {
      System.out.print(">> ");
    }
  }

  /**
   * Pede comando ao usuario
   */
  public static void askCommand() {
    printComandType();
    command = scanner.nextLine().trim();
  }

  /**
   * Executa comando do usuario
   */
  public static void execCommand() {
    if (command != "" && command != null) {
      if (command.startsWith("@")) {
        type = "@";
        sendTo = command.substring(1, command.length());
      } else if (command.startsWith("#")) {
        type = "#";
        sendTo = command.substring(1, command.length());
      } else {
        try {
          String messageToSend = buildMessage(user, command);
          sendMessage(messageToSend);
        } catch (Exception e) {
          System.out.println("[ERRO] " + e.getMessage());
        }
      }
    }
  }

  /**
   * Dispara mensagem ao canal
   * 
   * @param message Mensagem a ser disparada
   * @throws Exception
   */
  public static void sendMessage(String message) throws Exception {
    Channel channel = connection.createChannel();
    String queueName = sendTo + "_messages";
    channel.queueDeclare(queueName, false, false, false, null);
    channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
    channel.close();
  }

  /**
   * Constroi a mensagem a ser enviada
   * 
   * @param sender
   * @param message
   * @return
   */

  public static String buildMessage(String sender, String message) {
    Date now = new Date();
    String out = "(" + now.toLocaleString() + ") " + sender + " diz: " + message;
    return out;
  }

  /**
   * funcional demais, arrasane Main function
   * 
   * @param argv
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {

    scanner = new Scanner(System.in);

    // PEDIR USUARIO
    System.out.print("USER: ");
    user = scanner.nextLine();
    System.out.println("==== OLÁ " + user + " ====");

    // Conexão
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri(serverUri);
    connection = factory.newConnection();

    listenMessages();
    while (true) {
      askCommand();
      execCommand();
    }

  }
}