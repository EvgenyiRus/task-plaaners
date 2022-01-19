package com.tasklist.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.Future;

/**
 * Класс отправляет различные письма пользователю.
 *
 * Методы отправки письма желательно выполнять в параллельном потоке, чтобы не задерживать пользователя.
 *
 * Самый простой способ:
 * - @EnableAsync - разрешает асинхронный вызов методов (прописать в конфиге Spring)
 * - @Async - запускает метод в параллельном потоке (прописать возле нужного метода)
 * - если метод возвращает какой-либо тип, его нужно обернуть в спец. объект AsyncResult
 *
 * Примеры, документация:
 * https://spring.io/guides/gs/async-method/
 **/
@Service
@Slf4j
public class EmailService {
    @Value("${client.url}")
    private String urlClient; // URL для отправки писем(тестовый)

    @Value("${email.from}")
    private String emailFrom; // почтовый адрес с которого будут приходить письма для регистрации или смерны паролей пользователей

    private JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

//    // отправление пользователю письма активации
//    public void sendActivationEmail(String email, String username, String uuid) {
//
//        /*
//          ссылка на frontend перейдя по которой должна произойти активация аккаунта
//          Прикрепляем uuid к URL как get-параметр.
//          Клиент при нажатии на ссылку из письма - получит этот uuid
//         */
//        String url = urlClient + "/activate-account/" + uuid;
//
//        // html сообщение
//        String message = String.format("Здравствуйте, %s! <br/>" +
//                "Для подтверждения аккаунта перейдите по ссылке <a href='%s'/>", username, url);
//        sendEmail(email, message);
//    }
//
//    // к письму прикрепляется токен для последующей авторизации на backend
//    public void sendResetPasswordEmail(String email, String token) {
//
//        // ссылка на frontend перейдя по которой должен произойти сброс пароля
//        String url = urlClient + "/update-password/" + token;
//
//        // html сообщение
//        String message = String.format("Здравствуйте. <br/>" +
//                "Для сброса пароля перейдите по ссылке <a href='%s'/>", url);
//        sendEmail(email, message);
//    }
//
//    @Async
//    Future<Boolean> sendEmail(String email, String message) {
//        try {
//            // письмо, в виде HTML страницы(по умолчанию - текстовый формат)
//            MimeMessage mimeMessage = mailSender.createMimeMessage();
//
//            // вспомогательный элемент для указания параметров отправки письма
//            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
//            // html сообщение
//
//            mimeMessage.setContent(message, "text/html"); // тип письма
//            mimeMessageHelper.setTo(email); // адрес получателя
//            mimeMessageHelper.setFrom(emailFrom); // адрес отправителя
//            mimeMessageHelper.setSubject("Требуется активация аккаунта"); // тема письма
//            mimeMessageHelper.setText(message, true); // явное указание на HTML страницу
//            mailSender.send(mimeMessage);
//            return new AsyncResult<>(true);
//        } catch (MessagingException exception) {
//            exception.printStackTrace();
//        }
//        return new AsyncResult<>(false);
//    }

        // отправление пользователю письма активации
    @Async
    public Future<Boolean> sendActivationEmail(String email, String username, String uuid) {
        try {

            // письмо, в виде HTML страницы(по умолчанию - текстовый формат)
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // вспомогательный элемент для указания параметров отправки письма
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            // ссылка на frontend перейдя по которой должна произойти активация аккаунта
            String url = urlClient + "/activate-account/" + uuid;

            // текст письма в формате HTML
            String message = String.format("Здравствуйте.<br/><br/>" +
                            "Вы создали аккаунт для веб приложения \"Планировщик дел\": %s <br/><br/>" +
                            "<a href='%s'>%s</a><br/><br/>",  username, url, "Для подтверждения регистрации нажмите на эту ссылку");
            mimeMessage.setContent(message, "text/html"); // тип письма
            mimeMessageHelper.setTo(email); // адрес получателя
            mimeMessageHelper.setFrom(emailFrom); // адрес отправителя
            mimeMessageHelper.setSubject("Требуется активация аккаунта"); // тема письма
            mimeMessageHelper.setText(message, true); // явное указание на HTML страницу
            mailSender.send(mimeMessage);
            return new AsyncResult<>(true);
        } catch (MessagingException exception) {
            exception.printStackTrace();
        }
        return new AsyncResult<>(false);
    }

    // к письму прикрепляется токен для последующей авторизации на backend
    @Async
    public Future<Boolean> sendResetPasswordEmail(String email, String token) {
        try {
            // письмо, в виде HTML страницы(по умолчанию - текстовый формат)
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // вспомогательный элемент для указания параметров отправки письма
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            // ссылка на frontend перейдя по которой должен произойти сброс пароля
            String url = urlClient + "/update-password/" + token;

            // html сообщение
            String message = String.format("Здравствуйте. <br/>" +
                    "Для сброса пароля перейдите по ссылке <a href='%s'/>", url);
            mimeMessage.setContent(message, "text/html"); // тип письма
            mimeMessageHelper.setTo(email); // адрес получателя
            mimeMessageHelper.setFrom(emailFrom); // адрес отправителя
            mimeMessageHelper.setSubject("Требуется активация аккаунта"); // тема письма
            mimeMessageHelper.setText(message, true); // явное указание на HTML страницу
            mailSender.send(mimeMessage);
            return new AsyncResult<>(true);
        } catch (MessagingException exception) {
            exception.printStackTrace();
        }
        return new AsyncResult<>(false);
    }
}
