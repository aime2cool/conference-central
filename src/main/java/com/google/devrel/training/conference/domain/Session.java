package com.google.devrel.training.conference.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.ImmutableList;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.google.devrel.training.conference.form.SessionForm;
import com.google.devrel.training.conference.form.SessionForm.SessionType;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

@Entity
@Cache
public class Session {
	private static final String DEFAULT_CITY = "Default City";
	@Index String name;
	@Index SessionType type;
	@Index
	String speaker;
	@Parent
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Key<Conference> conferenceKey;
	@Id
	long id;
	@Index
	String websafeConferenceKey;
	
	@Index
	private List<String> topics;
	@Index
	private Date date;
	private String duration;
	@Index
	private String start_time;

	public Session(long id, String websafeConferenceKey, SessionForm sessionForm) {
		this.id = id;
		this.websafeConferenceKey = websafeConferenceKey;
		this.conferenceKey = Key.create(websafeConferenceKey);
		updateWithSessionForm(sessionForm);
	}

	public Date getDate() {
		return date == null ? null : new Date(date.getTime());
	}
	
	public String getDuration() {
		return duration;
	}

	public String getStartTime() {
		return start_time;
	}

	public String getSpeaker() {
		return speaker;
	}

	public SessionType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	public List<String> getTopics() {
        return topics == null ? null : ImmutableList.copyOf(topics);
    }
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Key<Conference> getConferenceKey() {
		return conferenceKey;
	}
	
	public String getWebSafeConferenceKey() {
		return websafeConferenceKey;
	}
	
	public String getWebSafeSessionKey() {
		return Key.create(conferenceKey, Session.class, id).getString();
	}
	/**
	 * Updates the Session with Session Form. This method is used upon object
	 * creation as well as updating existing Session.
	 * 
	 * @param sessionForm
	 *            contains form data sent from the client.
	 */
	public void updateWithSessionForm(SessionForm sessionForm) {
		type = sessionForm.getType();
		speaker = sessionForm.getSpeaker();
		start_time = sessionForm.getStartTime();
		topics = sessionForm.getTopics();
		name = sessionForm.getName();
		duration = sessionForm.getDuration();
		Date date = sessionForm.getDate();
		this.date = date == null ? null : new Date(
				date.getTime());
		
	}

	private Session() {};
}
