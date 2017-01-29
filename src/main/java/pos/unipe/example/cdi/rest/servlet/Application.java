/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pos.unipe.example.cdi.rest.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

import com.google.gson.Gson;

public class Application {

    @ContextName("posunipe")
    public static class PosUnipeRoute extends RouteBuilder {

        @Override
        public void configure() {
        	System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
            rest("/usuario/")
                .produces("text/plain")
                .get("cadastro/{nome}/{cpf}")
                    .route()
                    .bean("cadastro")
                    .log("${body}");
            rest("/agenda/")
	            .produces("text/plain")
	            .get("reserva/{cpf}")
	                .route()
	                .bean("reserva")
	                .log("${body}")
	                .endRest()
	            .get("confirma")
		            .route()
		            .bean("confirma")
		            .log("${body}");
        }
    }
    
    @Named("cadastro")
    public static class Cadastro {

        @Inject
        CamelContext context;
        
        public String cadastro(@Header("nome") String nome, @Header("cpf") String cpf) {
        	Gson gson = new Gson();
        	if(isCPF(cpf)){
        		return gson.toJson(cadastrarUsuario(nome, cpf));        		
        	} else {
        		return "CPF inválido!";
        	}               
        }
    }
    
    @Named("reserva")
    public static class Reservar {

        @Inject
        CamelContext context;
        
        public String reserva(@Header("cpf") String cpf) {
        	Gson gson = new Gson();
        	return gson.toJson(reservar(cpf));            
        }
    }
    
    @Named("confirma")
    public static class Confirmar {

        @Inject
        CamelContext context;
        
        public String confirma() {        	
        	Gson gson = new Gson();
            return gson.toJson(confirmarReserva());
        }
    }
    
    public static boolean isCPF(String CPF) {
    	// considera-se erro CPF's formados por uma sequencia de numeros iguais
    	    if (CPF.equals("00000000000") || CPF.equals("11111111111") ||
    	        CPF.equals("22222222222") || CPF.equals("33333333333") ||
    	        CPF.equals("44444444444") || CPF.equals("55555555555") ||
    	        CPF.equals("66666666666") || CPF.equals("77777777777") ||
    	        CPF.equals("88888888888") || CPF.equals("99999999999") ||
    	       (CPF.length() != 11))
    	       return(false);

    	    char dig10, dig11;
    	    int sm, i, r, num, peso;

    	// "try" - protege o codigo para eventuais erros de conversao de tipo (int)
    	    try {
    	// Calculo do 1o. Digito Verificador
    	      sm = 0;
    	      peso = 10;
    	      for (i=0; i<9; i++) {              
    	// converte o i-esimo caractere do CPF em um numero:
    	// por exemplo, transforma o caractere '0' no inteiro 0         
    	// (48 eh a posicao de '0' na tabela ASCII)         
    	        num = (int)(CPF.charAt(i) - 48); 
    	        sm = sm + (num * peso);
    	        peso = peso - 1;
    	      }

    	      r = 11 - (sm % 11);
    	      if ((r == 10) || (r == 11))
    	         dig10 = '0';
    	      else dig10 = (char)(r + 48); // converte no respectivo caractere numerico

    	// Calculo do 2o. Digito Verificador
    	      sm = 0;
    	      peso = 11;
    	      for(i=0; i<10; i++) {
    	        num = (int)(CPF.charAt(i) - 48);
    	        sm = sm + (num * peso);
    	        peso = peso - 1;
    	      }

    	      r = 11 - (sm % 11);
    	      if ((r == 10) || (r == 11))
    	         dig11 = '0';
    	      else dig11 = (char)(r + 48);

    	// Verifica se os digitos calculados conferem com os digitos informados.
    	      if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
    	         return(true);
    	      else return(false);
    	    } catch (InputMismatchException erro) {
    	        return(false);
    	    }
    	  }
    
    
	public static Usuario cadastrarUsuario(String name, String cpf) {

		Usuario usuario = new Usuario();
		usuario.setId(1);
		usuario.setNome(name);
		usuario.setCpf(cpf);

		try {
			// Criação de um buffer para a escrita em uma stream
			BufferedWriter StrW = new BufferedWriter(new FileWriter("usuarios.csv"));

			// Escrita dos dados da tabela
			StrW.write("Id;Nome;Cpf\n");
			StrW.write(usuario.getId() + ";" + usuario.getNome() + ";" + usuario.getCpf() + "\n");

			// Fechamos o buffer
			StrW.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return usuario;
	}

	public static Reserva reservar(String cpf) {
		
		Reserva reserva = new Reserva();
		
		try { // Create a connection factory.
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
						
			reserva.setId(1);
			
			Usuario usuario = new Usuario();
			
			try {

				// Criação de um buffer para a ler de uma stream
				BufferedReader StrR = new BufferedReader(new FileReader("usuarios.csv"));

				String Str;
				String[] TableLine;

				// Essa estrutura do looping while é clássica para ler cada
				// linha
				// do arquivo				
				
				while ((Str = StrR.readLine()) != null) {
					// Aqui usamos o método split que divide a linha lida em um
					// array de String
					// passando como parametro o divisor ";".
					TableLine = Str.split(";");
					
					if(cpf.equalsIgnoreCase(TableLine[2])){
						usuario.setId(new Integer(TableLine[0]));
						usuario.setNome(TableLine[1]);
						usuario.setCpf(TableLine[2]);
						break;
					}
				}
				// Fechamos o buffer
				StrR.close();				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			reserva.setUsuario(usuario);
             
             //Create connection.
             Connection connection = factory.createConnection();
          // Start the connection
             connection.start();

             // Create a session which is non transactional
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             
             // Create Destination queue
             Destination queue = session.createQueue("reservas");

             // Create a producer
             MessageProducer producer = session.createProducer(queue);
             
             producer.setDeliveryMode(DeliveryMode.PERSISTENT);
             
             String msg = "Reserva executada com sucesso para o usuario: " + cpf;

             // insert message
             ObjectMessage objMessage = session.createObjectMessage(reserva);
             System.out.println("Producer Sent: " + msg);
             producer.send(objMessage);

             session.close();
             connection.close();
         }
         catch (Exception ex) {
             System.out.println("Exception Occured");
         }
		
		return reserva;
    }
	
	public static Properties getProp() throws IOException {
		Properties props = new Properties();
		FileInputStream file = new FileInputStream(
				"./properties/email.properties");
		props.load(file);
		return props;

	}
    
    public static Reserva confirmarReserva() {
    	
    	Reserva reserva = null;
    	
    	try {
            ActiveMQConnectionFactory factory = 
            new ActiveMQConnectionFactory("tcp://localhost:61616");
            //Cria a conexão com ActiveMQ
            Connection connection = factory.createConnection();
            // Inicia a conexão
            connection.start();
         // Cria a sessão
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            //Crea a fila e informa qual o destinatário.
            
            Destination queue = session.createQueue("reservas");
            
            MessageConsumer consumer = session.createConsumer(queue);
            Message message = consumer.receive();                    
            
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                reserva = (Reserva) objectMessage.getObject();                
                System.out.println("Consumer Received: " + reserva);                
            }
            session.close();
            connection.close();
        }
        catch (Exception ex) {
            System.out.println("Exception Occured");
        }
    	
    	String email = "";
    	String senha = "";
    	String destinatarios = "";
    	
		try {
			Properties prop = getProp();

			email = prop.getProperty("prop.email.endereco");
			senha = prop.getProperty("prop.email.senha");
			destinatarios = prop.getProperty("prop.email.destinatarios");

		} catch (Exception e) {
			//Pass
		}

    	
    	Properties props = new Properties();
        /** Parâmetros de conexão com servidor Gmail */
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                         protected PasswordAuthentication getPasswordAuthentication() 
                         {
                        	 String email = "";
                        	 String senha = "";
                        	 try{
                        	      Properties prop = getProp();
                        	      email = prop.getProperty("prop.email.endereco");
                        	      senha = prop.getProperty("prop.email.senha");
                        	 } catch (Exception e){
                        		 
                        	 } 
                             return new PasswordAuthentication(email, senha);
                         }
                    });
        /** Ativa Debug para sessão */
        session.setDebug(true);
        try {

        	  javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
              message.setFrom(new InternetAddress(email)); //Remetente

              Address[] toUser = InternetAddress //Destinatário(s)
                         .parse(destinatarios);  
              message.setRecipients(javax.mail.Message.RecipientType.TO, toUser);
              message.setSubject("Confirmação de reserva");//Assunto
              message.setText("Oi " + reserva.getUsuario().getNome() + " (portador do CPF:" 
            		  + reserva.getUsuario().getCpf() + "),\n\nSua reserva foi confirmada: \n\n" + reserva);
              /**Método para enviar a mensagem criada*/
              Transport.send(message);
              System.out.println("Feito!!!");
         } catch (MessagingException e) {
              throw new RuntimeException(e);
        }
    	
    	return reserva;
    }   
    
    public static void enviaMensagem(String name) {
        try { // Create a connection factory.
            ActiveMQConnectionFactory factory = 
            new ActiveMQConnectionFactory("tcp://localhost:61616");

            //Create connection.
            Connection connection = factory.createConnection();
         // Start the connection
            connection.start();

            // Create a session which is non transactional
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Create Destination queue
            Destination queue = session.createQueue("br.unipe.pos");

            // Create a producer
            MessageProducer producer = session.createProducer(queue);
            
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            
            String msg = "Aula de Java Pos Web";

            // insert message
            TextMessage message = session.createTextMessage(msg);
            System.out.println("Producer Sent: " + msg);
            producer.send(message);

            session.close();
            connection.close();
        }
        catch (Exception ex) {
            System.out.println("Exception Occured");
        }
    }
}
