package main.java.services.user;

import main.java.configuration.SessionProvider;
import main.java.entities.School;
import main.java.entities.User;
import main.java.entities.managements.ImageManagement;
import main.java.entities.managements.SchoolManagement;
import main.java.entities.managements.UserManagement;
import main.java.json.JSONResponseGenerator;
import main.java.services.helpers.PathManager;
import main.java.status.manager.LoginStatusManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * The class contains web services for registering the user
 */
@Path("/user")
public class Registration {

    @Context
    HttpServletResponse response;

    /**
     * Registers the new user
     *
     * @param name           The name of the user
     * @param emailAddress   The email address of the user
     * @param password       The password of the user
     * @param gender         The gender of the user. Be careful, the variable type is int
     * @param phone          The phone number of the user
     * @param profilePicData The profile picture raw data
     * @param schoolName     The name of the school
     * @return The JSON response
     * {"Success": "Waiting for account activation."}
     * {"Fail": "The user has existed."}
     * @throws IOException if the profile image data cannot be written to the hard drive
     */
    @Path("/new")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registerNewUser(@FormDataParam("name") String name,
                                    @FormDataParam("emailAddress") String emailAddress,
                                    @FormDataParam("password") String password,
                                    @FormDataParam("gender") int gender,
                                    @FormDataParam("phone") String phone,
                                    @FormDataParam("profilePicture") InputStream profilePicData,
                                    @FormDataParam("schoolName") String schoolName) throws IOException {
        password = DigestUtils.sha256Hex(password);
        JSONObject jsonObject = new JSONObject();
        try (final Session session = SessionProvider.getSession()) {
            Transaction transaction = session.beginTransaction();

            UserManagement userManagement = new UserManagement(session);
            SchoolManagement schoolManagement = new SchoolManagement(session);

            // add school first
            School school = new School();
            school.setName(schoolName);
            int schoolId;
            if (!schoolManagement.isExist(school)) {
                schoolManagement.add(school);
                schoolId = school.getIdSchool();
                school.setLogo(PathManager.getImagesDirectory("pics/schoolLogos/" + schoolId + ".png"));
                schoolManagement.set(school);
            } else {
                schoolId = schoolManagement.get(schoolName).getIdSchool();
            }

            // add user next
            User user = new User();
            UserManagement.setUserBasicInfo(user, name, emailAddress, password, gender, phone, schoolId);
            if (!userManagement.isExist(user)) {
                // create user
                userManagement.add(user);
                user.setProfilePicture(PathManager.getImagesDirectory("pics/profilePicture/" + user.getIdUser() + ".jpeg"));
                userManagement.set(user);
                ImageManagement.writeImage(user.getProfilePicture(), profilePicData);
                LoginStatusManager.refreshLoginCookieStatus(response, session, user);
                jsonObject.put("Success", "Waiting for account activation.");
            } else {
                jsonObject = JSONResponseGenerator.formUserHasExistedJSON();
            }

            transaction.commit();
        }
        return Response.ok(jsonObject.toString()).build();
    }
}
