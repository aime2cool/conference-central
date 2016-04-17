package com.google.devrel.training.conference.servlet;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Joiner;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Conference;
import com.google.devrel.training.conference.domain.Session;
import com.googlecode.objectify.Key;

@SuppressWarnings("serial")
public class AddFeaturedSpeakerServlet extends HttpServlet{
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
		String speaker = request.getParameter("speaker");
		String websafeConferenceKey = request.getParameter("websafeConferenceKey");
		Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
		Iterable<Session> iterable = ofy().load().type(Session.class).ancestor(conferenceKey).filter("speaker", speaker);
		List<String> sessionNames = new ArrayList<>(0);
        for (Session session : iterable) {
        	sessionNames.add(session.getName());
        }
        if (sessionNames.size() > 0) {
        	StringBuilder sb = new StringBuilder("Sessions with speaker " + speaker + ": ");
        	Joiner joiner = Joiner.on(", ").skipNulls();
            sb.append(joiner.join(sessionNames));

            MemcacheService memcacheService =
            	     MemcacheServiceFactory.getMemcacheService();

            memcacheService.put(speaker + "_" + conferenceKey.getId(), sb.toString());

        }
//        response.setStatus(204);
	}
}
