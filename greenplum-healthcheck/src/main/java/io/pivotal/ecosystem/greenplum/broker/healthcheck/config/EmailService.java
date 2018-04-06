package io.pivotal.ecosystem.greenplum.broker.healthcheck.config;

import java.util.function.Consumer;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGrid.Email;
import com.sendgrid.SendGridException;

@Component
public class EmailService implements Consumer {
private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	@Autowired
	SendgridConfig sendgridConfig;

	private void sendSimpleMessage(String message) throws MessagingException, SendGridException {
		SendGrid sendgrid = new SendGrid(sendgridConfig.getUsername(), sendgridConfig.getPassword());
		Email email = new SendGrid.Email();
		email.addTo("data-cloud-dev@pivotal.io");
		email.addTo("pbardhan@pivotal.io");
		email.setFrom("no-reply@pivotal.io");
		email.setSubject("Greenplum Service Healthcheck Failed");
		email.setText(message);
		sendgrid.send(email);
	}

	@Override
	public void accept(Object t) {
		try {
			sendSimpleMessage(t.toString());
		} catch (MessagingException | SendGridException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}