package com.emprovise.api.microsoft.exchange;

import com.google.gson.JsonObject;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.AppointmentSchema;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MSExchange {

    private ExchangeService service;

    MSExchange(String emailHost, String username, String password) throws URISyntaxException {
        service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(username, password);
        service.setCredentials(credentials);
        service.setUrl(new URI(String.format("https://%s/ews/exchange.asmx", emailHost)));
        service.setTraceEnabled(true);
    }

    public void sendEmail(String sender, List<String> recipientsList, String subject, String messageText) throws Exception {
        EmailMessage message = new EmailMessage(service);
        message.setSender(new EmailAddress(sender));
        message.setSubject(subject);
        message.setBody(new MessageBody(messageText));
        for (String string : recipientsList) {
            message.getToRecipients().add(string);
        }
        message.sendAndSaveCopy();
    }

    public List<JsonObject> readEmails(int limit) throws Exception {
        List<JsonObject> emailMessages = new ArrayList<>();
        Folder folder = Folder.bind(service, WellKnownFolderName.Inbox);
        SearchFilter itemFilter = new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, true);
        FindItemsResults<Item> items = service.findItems(folder.getId(), itemFilter, new ItemView(limit));

        for (Item item : items) {
            emailMessages.add(readEmailItem(item.getId()));
        }
        return emailMessages;
    }

    private JsonObject readEmailItem(ItemId itemId) throws Exception {
        JsonObject messageJson = new JsonObject();
        Item emailItem = Item.bind(service, itemId, PropertySet.FirstClassProperties);
        EmailMessage emailMessage = EmailMessage.bind(service, emailItem.getId());
        messageJson.addProperty("emailId", emailMessage.getId().toString());
        messageJson.addProperty("subject", emailMessage.getSubject());
        messageJson.addProperty("senderAddress", emailMessage.getFrom().getAddress());
        messageJson.addProperty("senderName", emailMessage.getSender().getName());
        Date dateTimeCreated = emailMessage.getDateTimeCreated();
        messageJson.addProperty("sendDate", dateTimeCreated.toString());
        Date dateTimeReceived = emailMessage.getDateTimeReceived();
        messageJson.addProperty("receivedDate", dateTimeReceived.toString());
        messageJson.addProperty("size", emailMessage.getSize());
        messageJson.addProperty("emailBody", emailMessage.getBody().toString());
        return messageJson;
    }

    /**
     * Reading one appointment at a time. Using Appointment ID of the email.
     * Creating a message data map as a return value.
     */
    public JsonObject readAppointment(Appointment appointment) throws ServiceLocalException {
        JsonObject appointmentJson = new JsonObject();
        appointmentJson.addProperty("appointmentId", appointment.getId().toString());
        appointmentJson.addProperty("appointmentSubject", appointment.getSubject());
        appointmentJson.addProperty("appointmentStartTime", appointment.getStart().toString());
        appointmentJson.addProperty("appointmentEndTime", appointment.getEnd().toString());
        appointmentJson.addProperty("appointmentBody", appointment.getBody().toString());
        return appointmentJson;
    }

    /**
     *Number of Appointments we want to read is defined as NUMBER_EMAILS_FETCH,
     *  Here I also considered the start data and end date which is a 30 day span.
     *  We need to set the CalendarView property depending upon the need of ours.
     */
    public List<JsonObject> readAppointments(int periodInDays, int limit) throws Exception {

        List<JsonObject> appointmentList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, periodInDays);
        Date endDate = calendar.getTime();
        CalendarFolder calendarFolder = CalendarFolder.bind(service, WellKnownFolderName.Calendar, new PropertySet());
        CalendarView calendarView = new CalendarView(startDate, endDate, limit);
        calendarView.setPropertySet(new PropertySet(AppointmentSchema.Subject, AppointmentSchema.Start, AppointmentSchema.End));

        FindItemsResults<Appointment> appointments = calendarFolder.findAppointments(calendarView);

        for (Appointment appointment : appointments.getItems()) {
            JsonObject appointmentJson = readAppointment(appointment);
            appointmentList.add(appointmentJson);
        }
        return appointmentList;
    }

//    public void createAppointment() throws Exception {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date startTime = dateFormat.parse("2017-02-25 16:00:00");
//        Date endTime = dateFormat.parse("2017-02-25 18:00:00");
//
//        Appointment appointment = new Appointment(service);
//        appointment.setSubject("Test");
//        appointment.setBody(new Body("Body text"));
//        appointment.setStartTime(startTime);
//        appointment.setEndTime(endTime);
//        appointment.setLocation("My Office");
//        appointment.setReminderIsSet(true);
//        appointment.setReminderMinutesBeforeStart(30);
//
//        ItemId itemId = service.createItem(appointment);
//    }
}
