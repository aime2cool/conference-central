package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.ofy;
import static com.google.devrel.training.conference.service.OfyService.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
//import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.googlecode.objectify.cmd.Query;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Announcement;
import com.google.devrel.training.conference.domain.Conference;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.domain.Session;
import com.google.devrel.training.conference.domain.Speaker;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.google.devrel.training.conference.form.ConferenceQueryForm;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.google.devrel.training.conference.form.SessionForm;
import com.google.devrel.training.conference.form.SessionForm.SessionType;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;

/**
 * Defines conference APIs.
 */
@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = {
        Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID }, description = "API for the Conference Central Backend application.")
public class ConferenceApi {
	public static class WrappedBoolean {

        private final Boolean result;
        private final String reason;

        public WrappedBoolean(Boolean result) {
            this.result = result;
            this.reason = "";
        }

        public WrappedBoolean(Boolean result, String reason) {
            this.result = result;
            this.reason = reason;
        }

        public Boolean getResult() {
            return result;
        }

        public String getReason() {
            return reason;
        }
    }
    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @param profileForm
     *            A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException
     *             when the User object is null.
     */

    // Declare this method as a method available externally through Endpoints
    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    // The request that invokes this method should provide data that
    // conforms to the fields defined in ProfileForm

    // TODO 1 Pass the ProfileForm parameter
    // TODO 2 Pass the User parameter
    public Profile saveProfile(final User user, ProfileForm p) throws UnauthorizedException {

        String userId = null;
        String mainEmail = null;
        String displayName = "Your name will go here";
        TeeShirtSize teeShirtSize = TeeShirtSize.NOT_SPECIFIED;

        
        if (user == null) {
        	throw new UnauthorizedException("User is not logged in.");
        }
        
        
        
        Profile profile = getProfile(user);
        if (profile == null) {
        	userId = user.getUserId();
            mainEmail = user.getEmail();
        	if (p.getTeeShirtSize() != null) {
            	teeShirtSize = p.getTeeShirtSize();
            }
        	if (p.getDisplayName() != null) {
            	displayName = p.getDisplayName();
            }
            else {
            	displayName = extractDefaultDisplayNameFromEmail(mainEmail);
            }
        	
        	profile = new Profile(userId, displayName, mainEmail, teeShirtSize);      	
        }
        else {
        	profile.update(p.getDisplayName(), p.getTeeShirtSize());
        }
        
        ofy().save().entity(profile).now();
        // Return the profile
        return profile;
    }

    /**
     * Returns a Profile object associated with the given user object. The cloud
     * endpoints system automatically inject the User object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException
     *             when the User object is null.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // TODO
        // load the Profile Entity
        String userId = user.getUserId(); // TODO
        Key<Profile> key = Key.create(Profile.class, userId); // TODO
//        Profile profile = (Profile) ofy().load().key(key).now(); // TODO load the Profile entity
        return ofy().load().key(key).now();
    }
    
    /**
     * Gets the Profile entity for the current user
     * or creates it if it doesn't exist
     * @param user
     * @return user's Profile
     */
    private static Profile getProfileFromUser(final User user) {
        // First fetch the user's Profile from the datastore.
        Profile profile = ofy().load().key(
                Key.create(Profile.class, user.getUserId())).now();
        if (profile == null) {
            // Create a new Profile if it doesn't exist.
            // Use default displayName and teeShirtSize
            String email = user.getEmail();
            profile = new Profile(user.getUserId(),
                    extractDefaultDisplayNameFromEmail(email), email, TeeShirtSize.NOT_SPECIFIED);
        }
        return profile;
    }

/**
     * Creates a new Conference object and stores it to the datastore.
     *
     * @param user A user who invokes this method, null when the user is not signed in.
     * @param conferenceForm A ConferenceForm object representing user's inputs.
     * @return A newly created Conference Object.
     * @throws UnauthorizedException when the user is not signed in.
     */
    @ApiMethod(name = "createConference", path = "conference", httpMethod = HttpMethod.POST)
    public Conference createConference(final User user, final ConferenceForm conferenceForm)
        throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        Conference result = ofy().transact(new Work<Conference>() {
        	public Conference run() {
        		// TODO (Lesson 4)
                // Get the userId of the logged in User
                String userId = user.getUserId();

                // TODO (Lesson 4)
                // Get the key for the User's Profile
                Key<Profile> profileKey = Key.create(Profile.class, userId);

                // TODO (Lesson 4)
                // Allocate a key for the conference -- let App Engine allocate the ID
                // Don't forget to include the parent Profile in the allocated ID
                final Key<Conference> conferenceKey = factory().allocateId(profileKey, Conference.class);

                // TODO (Lesson 4)
                // Get the Conference Id from the Key
                final long conferenceId = conferenceKey.getId();

                // TODO (Lesson 4)
                // Get the existing Profile entity for the current user if there is one
                // Otherwise create a new Profile entity with default values
                Profile profile = getProfileFromUser(user);

                // TODO (Lesson 4)
                // Create a new Conference Entity, specifying the user's Profile entity
                // as the parent of the conference
                Conference conference = new Conference(conferenceId, userId, conferenceForm);

                // TODO (Lesson 4)
                // Save Conference and Profile Entities
                ofy().save().entities(conference, profile).now();
                Queue queue = QueueFactory.getDefaultQueue();
                queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/send_email").
               		    param("email", user.getEmail()).param("conferenceInfo", conference.getName()));
                return conference;
        	}
        });
        
         return result;
     }
    
    @ApiMethod(name = "queryConferences", path = "queryConferences", httpMethod = HttpMethod.POST)
    public List<Conference> queryConferences(ConferenceQueryForm cform) {
//    	List<Conference> list = ofy().load().type(Conference.class).order("name").list();
    	Iterable<Conference> list = cform.getQuery();
    	List<Conference> res = new ArrayList<Conference>();
    	List<Key<Profile>> organizerKeyList = new ArrayList<>();
    	for (Conference conference : list) {
    		organizerKeyList.add(Key.create(Profile.class, conference.getOrganizerUserId()));
    		res.add(conference);
    	}
    	ofy().load().keys(organizerKeyList);
    	return res;
//    	return cform.getQuery().list();
    }
    /**
     * @param user A user who invokes this method, null when the user is not signed in.
     * @return list of conferences that the user created
     * @throws UnauthorizedException when the user is not signed in.
     */
    @ApiMethod(name = "getConferencesCreated", path = "getConferencesCreated", httpMethod = HttpMethod.POST)
    public List<Conference> getConferencesCreated(final User user) throws UnauthorizedException{
    	if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
    	Query<Conference> query = ofy().load().type(Conference.class).ancestor(Key.create(Profile.class, user.getUserId())).order("name");
    	return query.list();
    }
    
    public List<Conference> filterConferences() {
    	Query<Conference> query = ofy().load().type(Conference.class).order("name").filter("city =","London");
    	query = query.filter("topics", "Medical Innovations");
    	query = query.filter("month =", 6);
    	query = query.filter("maxAttendees >", 10);
    	return query.list();
    }
    
    /**
     * Register to attend the specified Conference.
     *
     * @param user An user who invokes this method, null when the user is not signed in.
     * @param websafeConferenceKey The String representation of the Conference Key.
     * @return Boolean true when success, otherwise false
     * @throws UnauthorizedException when the user is not signed in.
     * @throws NotFoundException when there is no Conference with the given conferenceId.
     */
    @ApiMethod(
            name = "registerForConference",
            path = "conference/{websafeConferenceKey}/registration",
            httpMethod = HttpMethod.POST
    )

    public WrappedBoolean registerForConference(final User user,
            @Named("websafeConferenceKey") final String websafeConferenceKey)
            throws UnauthorizedException, NotFoundException,
            ForbiddenException, ConflictException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // Get the userId
//        final String userId = user.getUserId();

        // TODO
        // Start transaction
        WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
        	public WrappedBoolean run() {
        		
                try {

                // TODO
                // Get the conference key -- you can get it from websafeConferenceKey
                // Will throw ForbiddenException if the key cannot be created
                Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
                // TODO
                // Get the Conference entity from the datastore
                Conference conference = ofy().load().key(conferenceKey).now();

                // 404 when there is no Conference with the given conferenceId.
                if (conference == null) {
                    return new WrappedBoolean (false,
                            "No Conference found with key: "
                                    + websafeConferenceKey);
                }

                // TODO
                // Get the user's Profile entity
                Profile profile = getProfileFromUser(user);

                // Has the user already registered to attend this conference?
                if (profile.getConferenceKeysToAttend().contains(
                        websafeConferenceKey)) {
                    return new WrappedBoolean (false, "Already registered");
                } else if (conference.getSeatsAvailable() <= 0) {
                    return new WrappedBoolean (false, "No seats available");
                } else {
                    // All looks good, go ahead and book the seat
                    
                    // TODO
                    // Add the websafeConferenceKey to the profile's
                    // conferencesToAttend property
                    profile.addToConferenceKeysToAttend(websafeConferenceKey);
                    
                    // TODO 
                    // Decrease the conference's seatsAvailable
                    // You can use the bookSeats() method on Conference
                    conference.bookSeats(1);
                    // TODO
                    // Save the Conference and Profile entities
                    ofy().save().entities(profile, conference);
                    // We are booked!
                    return new WrappedBoolean(true, "Registration successful");
                }

                }
                catch (Exception e) {
                    return new WrappedBoolean(false, "Unknown exception");
                }
            }
		});
        // if result is false
        if (!result.getResult()) {
            if (result.getReason().contains("No Conference found with key")) {
                throw new NotFoundException (result.getReason());
            }
            else if (result.getReason() == "Already registered") {
                throw new ConflictException("You have already registered");
            }
            else if (result.getReason() == "No seats available") {
                throw new ConflictException("There are no seats available");
            }
            else {
                throw new ForbiddenException("Unknown exception");
            }
        }
        return result;
    }
    
    /**
     * Unregister from the specified Conference.
     *
     * @param user An user who invokes this method, null when the user is not signed in.
     * @param websafeConferenceKey The String representation of the Conference Key 
     * to unregister from.
     * @return Boolean true when success, otherwise false.
     * @throws UnauthorizedException when the user is not signed in.
     * @throws NotFoundException when there is no Conference with the given conferenceId.
     */
    @ApiMethod(
            name = "unregisterFromConference",
            path = "conference/{websafeConferenceKey}/registration",
            httpMethod = HttpMethod.DELETE
    )
    public WrappedBoolean unregisterFromConference(final User user,
            @Named("websafeConferenceKey") final String websafeConferenceKey)
            throws UnauthorizedException, NotFoundException,
            ForbiddenException, ConflictException {
    	// If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // Get the userId
//        final String userId = user.getUserId();

        // TODO
        // Start transaction
        WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
        	public WrappedBoolean run() {
        		
                try {

                
                Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
                // TODO
                // Get the Conference entity from the datastore
                Conference conference = ofy().load().key(conferenceKey).now();

                // 404 when there is no Conference with the given conferenceId.
                if (conference == null) {
                    return new WrappedBoolean (false,
                            "No Conference found with key: "
                                    + websafeConferenceKey);
                }

                // TODO
                // Get the user's Profile entity
                Profile profile = getProfileFromUser(user);

                // Has the user already registered to attend this conference?
                if (!profile.getConferenceKeysToAttend().contains(
                        websafeConferenceKey)) {
                    return new WrappedBoolean (false, "Not registered");
                } else {
                    // All looks good, go ahead and book the seat
                    
                    
                    profile.unregisterFromConference(websafeConferenceKey);
                    // TODO 
                    
                    conference.giveBackSeats(1);
                    // TODO
                    // Save the Conference and Profile entities
                    ofy().save().entities(profile, conference);
                    // We are booked!
                    return new WrappedBoolean(true, "Unregistration successful");
                }

                }
                catch (Exception e) {
                    return new WrappedBoolean(false, "Unknown exception");
                }
            }
		});
        // if result is false
        if (!result.getResult()) {
            if (result.getReason().contains("No Conference found with key")) {
                throw new NotFoundException (result.getReason());
            }
            else if (result.getReason() == "Not registered") {
                throw new ConflictException("You have not registered");
            }
//            else if (result.getReason() == "No seats available") {
//                throw new ConflictException("There are no seats available");
//            }
            else {
                throw new ForbiddenException("Unknown exception");
            }
        }
        return result;
    }
    
    /**
     * Returns a Conference object with the given conferenceId.
     *
     * @param websafeConferenceKey The String representation of the Conference Key.
     * @return a Conference object with the given conferenceId.
     * @throws NotFoundException when there is no Conference with the given conferenceId.
     */
    @ApiMethod(
            name = "getConference",
            path = "conference/{websafeConferenceKey}",
            httpMethod = HttpMethod.GET
    )
    public Conference getConference(
            @Named("websafeConferenceKey") final String websafeConferenceKey)
            throws NotFoundException {
        Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
        Conference conference = ofy().load().key(conferenceKey).now();
        if (conference == null) {
            throw new NotFoundException("No Conference found with key: " + websafeConferenceKey);
        }
        return conference;
    }

    /**
     * Returns a collection of Conference Object that the user is going to attend.
     *
     * @param user An user who invokes this method, null when the user is not signed in.
     * @return a Collection of Conferences that the user is going to attend.
     * @throws UnauthorizedException when the User object is null.
     */
    @ApiMethod(
            name = "getConferencesToAttend",
            path = "getConferencesToAttend",
            httpMethod = HttpMethod.GET
    )
    public Collection<Conference> getConferencesToAttend(final User user)
            throws UnauthorizedException, NotFoundException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        // TODO
        // Get the Profile entity for the user
        Profile profile = getProfileFromUser(user); // Change this;
        if (profile == null) {
            throw new NotFoundException("Profile doesn't exist.");
        }

        // TODO
        // Get the value of the profile's conferenceKeysToAttend property
        List<String> keyStringsToAttend = profile.getConferenceKeysToAttend(); // change this

        // TODO
        // Iterate over keyStringsToAttend,
        // and return a Collection of the
        // Conference entities that the user has registered to attend
        List<Key<Conference>> keys = new ArrayList<Key<Conference>>();
        for (String k : keyStringsToAttend) {
        	Key<Conference> key = Key.create(k);
        	keys.add(key);
        }
        return ofy().load().keys(keys).values();  // change this
    }
    /**
     * 
     * @return an Announcement object whose message property contains the latest announcement.
     */
    @ApiMethod(
            name = "getAnnouncement",
            path = "getAnnouncement",
            httpMethod = HttpMethod.GET
    )
    public Announcement getAnnouncement() {
    	MemcacheService memcacheService =
       	     MemcacheServiceFactory.getMemcacheService();
    	String message = (String) memcacheService.get(Constants.MEMCACHE_ANNOUNCEMENTS_KEY);
    	if (message != null) {
    		return new Announcement(message);
    	}
    	return null;
    }
    /**
     * Given a conference, return all sessions
     * 
     * @param conference key
     * @return all sessions belongs to this conference
     */
    @ApiMethod(
            name = "getConferenceSessions",
            path = "getConferenceSessions",
            httpMethod = HttpMethod.GET
    )
    public List<Session> getConferenceSessions(@Named("websafeConferenceKey") String websafeConferenceKey) {
    	Key<Conference> key = Key.create(websafeConferenceKey);
    	return ofy().load().type(Session.class).ancestor(key).list();
    }
    /**
     * Given a conference, return all sessions of a specified type (eg lecture, keynote, workshop)
     * 
     * @param websafeConferenceKey
     * @param typeOfSession
     * @return all sessions of a specified type (eg lecture, keynote, workshop)
     */
    @ApiMethod(
            name = "getConferenceSessionsByType",
            path = "getConferenceSessionsByType",
            httpMethod = HttpMethod.GET
    )
    public List<Session> getConferenceSessionsByType(@Named("websafeConferenceKey") String websafeConferenceKey, @Named("sessionType") SessionType typeOfSession) {
    	Key<Conference> key = Key.create(websafeConferenceKey);
    	return ofy().load().type(Session.class).ancestor(key).filter("type", typeOfSession).list();
    }
    /**
     * Given a speaker, return all sessions given by this particular speaker, across all conferences
     * 
     * @param speaker
     * @return  all sessions given by this particular speaker, across all conferences
     */
    @ApiMethod(
            name = "getSessionsBySpeaker",
            path = "getSessionsBySpeaker",
            httpMethod = HttpMethod.GET
    )
    public List<Session> getSessionsBySpeaker(@Named("speaker") String speaker) {
    	Query<Session> query = ofy().load().type(Session.class).filter("speaker", speaker);
    	return query.list();
    }
    /**
     * open only to the organizer of the conference
     * 
     * @param SessionForm
     * @param websafeConferenceKey
     * @return created session
     * @throws UnauthorizedException 
     * @throws NotFoundException 
     */
    @ApiMethod(
            name = "createSession",
            path = "createSession",
            httpMethod = HttpMethod.POST
    )
    public Session createSession(final User user, final SessionForm SessionForm, @Named("websafeConferenceKey") final String websafeConferenceKey) throws UnauthorizedException, NotFoundException {
    	if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
    	Session session = ofy().transact(new Work<Session>() {
        	public Session run() {
        		Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
            	final Key<Session> sessionKey = factory().allocateId(conferenceKey, Session.class);
            	final long sessionId = sessionKey.getId();
            	Session session = new Session(sessionId, websafeConferenceKey, SessionForm);
            	Conference conference;
				try {
					conference = getConference(websafeConferenceKey);
					ofy().save().entities(conference, session).now();
	            	Queue queue = QueueFactory.getDefaultQueue();
	                queue.add(ofy().getTransaction(), TaskOptions.Builder.withUrl("/featured_speaker").
	               		    param("speaker", session.getSpeaker()).param("websafeConferenceKey", websafeConferenceKey));
	            	
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return session;
        	}
    	});
    	return session;
    	
    }
    
    /**
     * adds the session to the user's list of sessions they are interested in attending
     * @param user
     * @param websafeSessionKey
     * @return
     * @throws UnauthorizedException
     * @throws NotFoundException 
     * @throws ConflictException 
     * @throws ForbiddenException 
     */
    @ApiMethod(
            name = "addSessionToWishlist",
            path = "session/{websafeSessionKey}/addSessionToWishlist",
            httpMethod = HttpMethod.POST
            
    )
    public WrappedBoolean addSessionToWishlist(final User user, @Named("websafeSessionKey") final String websafeSessionKey) throws UnauthorizedException, NotFoundException, ConflictException, ForbiddenException {
    	if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
    	WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
        	public WrappedBoolean run() {
        		Key<Session> sessionKey = Key.create(websafeSessionKey);
        		Session session = ofy().load().key(sessionKey).now();
        		if (session == null) {
                    return new WrappedBoolean (false,
                            "No session found with key: "
                                    + websafeSessionKey);
                }
        		Profile profile = getProfileFromUser(user);
        		if (profile.getSessionWishList().contains(websafeSessionKey)) {
        			return new WrappedBoolean (false, "Already added");
        		}
        		else {
        			profile.addToSessionWishList(websafeSessionKey);
        			ofy().save().entity(profile);
                    return new WrappedBoolean (true, "Added to wish list!");
        		}
        	}
    	});
    	if (!result.getResult()) {
            if (result.getReason().contains("No session found with key")) {
                throw new NotFoundException (result.getReason());
            }
            else if (result.getReason() == "Already added") {
                throw new ConflictException("This session already in your wish list");
            }
            else {
                throw new ForbiddenException("Unknown exception");
            }
        }
    	return result;
    }
    /**
     * query for all the sessions in a conference that the user is interested in
     * @param user
     * @param websafeConferenceKey
     * @return all the sessions in a conference that the user is interested in
     * @throws UnauthorizedException 
     * @throws NotFoundException 
     */
    @ApiMethod(
            name = "getSessionsInWishlist",
            path = "session/{websafeConferenceKey}/getSessionsInWishlist",
            httpMethod = HttpMethod.POST
            
    )
    public Collection<Session> getSessionsInWishlist(final User user, @Named("websafeConferenceKey")String websafeConferenceKey) throws UnauthorizedException, NotFoundException {
    	if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
    	Profile profile = getProfileFromUser(user);
    	if (profile == null) {
            throw new NotFoundException("Profile doesn't exist.");
        }
    	List<String> websafeKeys = profile.getSessionWishList();
    	List<Key<Session>> sessionKeys = new ArrayList<Key<Session>>();
    	for (String str : websafeKeys) {
    		Key<Session> sessionKey = Key.create(str);
    		sessionKeys.add(sessionKey);
    	}
    	Collection<Session> wishlist = ofy().load().keys(sessionKeys).values();
    	Collection<Session> result = new ArrayList<Session>();
    	for (Session session : wishlist) {
    		if (session.getWebSafeConferenceKey().equals(websafeConferenceKey)) {
    			result.add(session);
    		}
    	}
    	return result;
    }
    
    /**
     * removes the session from the user’s list of sessions they are interested in attending
     * @param user
     * @param websafeSessionKey
     * @return
     * @throws NotFoundException 
     * @throws UnauthorizedException 
     * @throws ConflictException 
     * @throws ForbiddenException 
     */
    @ApiMethod(
            name = "deleteSessionInWishlist",
            path = "session/{websafeSessionKey}/deleteSessionInWishlist",
            httpMethod = HttpMethod.DELETE
            
    )
    public WrappedBoolean deleteSessionInWishlist(final User user, @Named("websafeSessionKey") final String websafeSessionKey) throws NotFoundException, UnauthorizedException, ConflictException, ForbiddenException {
    	if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
    	
    	
    	WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
        	public WrappedBoolean run() {
        		try {
        			Profile profile = getProfileFromUser(user);
        			if (profile == null) {
        				return new WrappedBoolean(false, "Profile doesn't exist.");
        	        }
            		if (!profile.getSessionWishList().contains(websafeSessionKey)) {
            			return new WrappedBoolean(false, "session is not added");
            		}
            		
            		
            		profile.removeFromSessionWishList(websafeSessionKey);
            		ofy().save().entity(profile);
            		return new WrappedBoolean(true, "session is removed");
        		}
        	
        		catch (Exception e) {
        			return new WrappedBoolean(false, "Unknown exception");
        		}
        		
        	}
    	});
    	if (!result.getResult()) {
    		if (result.getReason().contains("session is not added")) {
                throw new NotFoundException (result.getReason());
            }
            else if (result.getReason() == "session is not added") {
                throw new ConflictException("This session is not in your wish list");
            }
            else if (result.getReason() == "Profile doesn't exist.") {
            	throw new NotFoundException("Profile doesn't exist.");
            }
            else {
                throw new ForbiddenException("Unknown exception");
            }
    	}
    	return result;
    }
   
    /**
     * 
     * @return an Announcement object whose message property contains speaker and sessions
     */
    public Announcement getFeaturedSpeaker(@Named("speaker") final String speaker, @Named("websafeConferenceKey")final String websafeConferenceKey) {
    	MemcacheService memcacheService =
          	     MemcacheServiceFactory.getMemcacheService();
    	Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
       	String message = (String) memcacheService.get(speaker + "_" + conferenceKey.getId());
       	if (message != null) {
       		return new Announcement(message);
       	}
       	return null;
    }
}
