package com.example.projetoagendafirebase;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender extends AsyncTask<Void, Void, Boolean> {

    private final String emailDestino;
    private final String assunto;
    private final String mensagem;

    public EmailSender(String emailDestino, String assunto, String mensagem) {
        this.emailDestino = emailDestino;
        this.assunto = assunto;
        this.mensagem = mensagem;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            // Configurações do servidor SMTP do Gmail
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            // Credenciais do Gmail
            final String email = "seu-email@gmail.com"; // Substitua pelo seu email
            final String senha = "sua-senha"; // Substitua pela sua senha ou senha de aplicativo

            // Sessão de autenticação
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, senha);
                }
            });

            // Criar a mensagem de e-mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject(assunto);
            message.setText(mensagem);

            // Enviar o e-mail
            Transport.send(message);
            Log.d("EmailSender", "Email enviado com sucesso para " + emailDestino);
            return true;
        } catch (Exception e) {
            Log.e("EmailSender", "Erro ao enviar e-mail", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean resultado) {
        super.onPostExecute(resultado);
        if (resultado) {
            Log.d("EmailSender", "E-mail enviado com sucesso!");
        } else {
            Log.e("EmailSender", "Falha ao enviar e-mail.");
        }
    }
}
