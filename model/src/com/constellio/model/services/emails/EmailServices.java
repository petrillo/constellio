package com.constellio.model.services.emails;

import com.constellio.model.conf.email.EmailServerConfiguration;
import com.constellio.model.services.emails.EmailServicesException.EmailPermanentException;
import com.constellio.model.services.emails.EmailServicesException.EmailServerException;
import com.constellio.model.services.emails.EmailServicesException.EmailTempException;

import org.apache.commons.lang.StringUtils;
import org.apache.james.mime4j.dom.TextBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailQueueManager.class);

	public void sendEmail(Message message)
			throws Exception {
		try {
			Transport.send(message);
		} catch (Exception e) {
			Exception exception = throwAppropriateException(e);
			throw exception;
		}
	}

	public Exception throwAppropriateException(Exception e)
			throws EmailServerException, EmailTempException, EmailPermanentException {
		LOGGER.warn(e.getMessage(), e);
		if (e instanceof AuthenticationFailedException ||
				e instanceof NoSuchProviderException) {
			return new EmailServerException(e);
		} else if (e instanceof MessageRemovedException
				|| e instanceof MethodNotSupportedException
				|| e instanceof ReadOnlyFolderException
				|| e instanceof FolderNotFoundException
				|| e instanceof SendFailedException) {
			return new EmailPermanentException(e);
		} else {//FolderClosedException, MailConnectException, ParseException, SendFailedException,
			// StoreClosedException, IllegalWriteException, SearchException, SMTPAddressSucceededException
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof UnknownHostException) {
				return new EmailServerException(e);
			}
			return new EmailTempException(e);
		}
	}

	public Session openSession(EmailServerConfiguration configuration)
			throws EmailServerException {
		if (configuration == null) {
			throw new EmailServerException(new EmailServicesException.NullEmailServerException());
		}
		final String username = configuration.getUsername();
		final String password = configuration.getPassword();

		Properties props = new Properties();
		props.putAll(configuration.getProperties());

		return Session.getInstance(props,
				new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
	}

	public void closeSession(Session session) {
	}

	public Message createMessage(String from, String subject, String body, List<MessageAttachment> attachments)
			throws MessagingException, IOException {
		Message message = new MimeMessage(Session.getInstance(System.getProperties()));
		if (StringUtils.isNotBlank(from)) {
			message.setFrom(new InternetAddress(from));
		}
		if (subject != null) {
			message.setSubject(subject);
		}

		MimeBodyPart content = new MimeBodyPart();
		if (body != null) {
			content.setText(body);
		} else {
			content.setText("");
		}

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(content);
		// add attachments
		if (attachments != null && !attachments.isEmpty()) {
			for (MessageAttachment messageAttachment : attachments) {
				MimeBodyPart attachment = new MimeBodyPart();
				DataSource source = new ByteArrayDataSource(messageAttachment.getInputStream(), messageAttachment.getMimeType());
				attachment.setDataHandler(new DataHandler(source));
				attachment.setFileName(messageAttachment.getAttachmentName());
				multipart.addBodyPart(attachment);
			}
		}
		message.setContent(multipart);

		return message;
	}

	public List<MessageAttachment> getAttachments(Message message)
			throws IOException, MessagingException {
		List<MessageAttachment> returnList = new ArrayList<>();
		Multipart multipart = (Multipart) message.getContent();

		for (int x = 0; x < multipart.getCount(); x++) {
			BodyPart bodyPart = multipart.getBodyPart(x);
			String disposition = bodyPart.getDisposition();
			if (disposition != null
					&& (disposition.equals(BodyPart.ATTACHMENT))) {
				DataHandler handler = bodyPart.getDataHandler();

				returnList.add(new MessageAttachment().setAttachmentName(handler.getName())
						.setInputStream(handler.getInputStream())
						.setMimeType(handler.getContentType()));
			}
		}
		return returnList;
	}

	public String getBody(Message message)
			throws IOException, MessagingException {
		Multipart multipart = (Multipart) message.getContent();

		for (int x = 0; x < multipart.getCount(); x++) {
			BodyPart bodyPart = multipart.getBodyPart(x);
			String disposition = bodyPart.getDisposition();
			if (disposition == null
					|| !(disposition.equals(BodyPart.ATTACHMENT))) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bodyPart.writeTo(baos);
				return new String(baos.toByteArray());
			}
		}
		return "";
	}

	public static class MessageAttachment {
		InputStream inputStream;
		String mimeType;
		String attachmentName;

		public InputStream getInputStream() {
			return inputStream;
		}

		public MessageAttachment setInputStream(InputStream attachment) {
			this.inputStream = attachment;
			return this;
		}

		public String getMimeType() {
			return mimeType;
		}

		public MessageAttachment setMimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}

		public String getAttachmentName() {
			return attachmentName;
		}

		public MessageAttachment setAttachmentName(String attachmentName) {
			this.attachmentName = attachmentName;
			return this;
		}
	}

}
