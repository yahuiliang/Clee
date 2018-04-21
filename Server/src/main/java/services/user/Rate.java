package main.java.services.user;

import main.java.configuration.SessionProvider;
import main.java.entities.SellerReview;
import main.java.entities.User;
import main.java.entities.managements.SellerReviewManagement;
import main.java.entities.managements.UserManagement;
import main.java.json.JSONResponseGenerator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/UserServices")
public class Rate {
    @Path("/rateSeller")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response rateSeller(@CookieParam("loginIdentifier") String loginIdentifier,
                               @FormParam("sellerId") int sellerId,
                               @FormParam("rate") double rate) {
        JSONObject jsonObject = new JSONObject();
        try (final Session session = SessionProvider.getSession()) {
            Transaction transaction = session.beginTransaction();

            UserManagement userManagement = new UserManagement(session);
            SellerReviewManagement sellerReviewManagement = new SellerReviewManagement(session);

            User user = new User();
            user.setLoginIdentifier(loginIdentifier);

            if (userManagement.isExist(user)) {
                user = userManagement.get(loginIdentifier);
                SellerReview sellerReview = new SellerReview();
                sellerReview.setUserIdUser(sellerId);
                sellerReview.setStars(rate);
                sellerReviewManagement.add(sellerReview);
                jsonObject.put("Success", "The rate has been posted.");
            } else {
                jsonObject = JSONResponseGenerator.formSignedOutJSON();
            }

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject = JSONResponseGenerator.formUnknownExceptionJSON(e);
        }
        return Response.ok(jsonObject).build();
    }

    @Path("/rateBuyer")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response rateBuyer() {
        return null;
    }
}