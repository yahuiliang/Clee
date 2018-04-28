package main.java.services.product;

import main.java.configuration.SessionProvider;
import main.java.entities.Product;
import main.java.entities.User;
import main.java.entities.managements.ProductManagement;
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
import java.io.File;

@Path("/product")
public class Delete {
    @Path("/delete/image")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteItemImage(@CookieParam("loginIdentifier") String loginIdentifier,
                                    @FormParam("itemId") int itemId,
                                    @FormParam("itemIndex") int imageIndex) {
        JSONObject jsonObject = new JSONObject();
        try (final Session session = SessionProvider.getSession()) {
            Transaction transaction = session.beginTransaction();
            UserManagement userManagement = new UserManagement(session);
            ProductManagement productManagement = new ProductManagement(session);

            User user = new User();
            user.setLoginIdentifier(loginIdentifier);

            if (userManagement.isExist(user)) {
                Product product = new Product();
                product.setIdProduct(itemId);
                if (productManagement.isExist(product)) {
                    product = productManagement.get(itemId);
                    String imagePath = product.getPictures() + imageIndex + ".jpeg";
                    File file = new File(imagePath);
                    if (file.delete()) {
                        jsonObject.put("Success", "The image is deleted.");
                    } else {
                        jsonObject.put("Fail", "The image does not exist.");
                    }
                } else {
                    jsonObject.put("Fail", "The product does not exist.");
                }
            } else {
                jsonObject = JSONResponseGenerator.formSignedOutJSON();
            }

            transaction.commit();
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @Path("/delete")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeItem(@CookieParam("loginIdentifier") String loginIdentifier,
                               @FormParam("itemId") int itemId) {
        JSONObject jsonObject = new JSONObject();
        try (final Session session = SessionProvider.getSession()) {
            Transaction transaction = session.beginTransaction();

            UserManagement userManagement = new UserManagement(session);
            ProductManagement productManagement = new ProductManagement(session);

            User user = new User();
            user.setLoginIdentifier(loginIdentifier);
            Product product = new Product();
            product.setIdProduct(itemId);

            if (userManagement.isExist(user)) {
                user = userManagement.get(loginIdentifier);
                if (productManagement.isExist(product)) {
                    product = productManagement.get(itemId);
                    if (product.getIdSeller().equals(user.getIdUser())) {
                        productManagement.remove(product);
                        jsonObject.put("Success", "The item has been deleted.");
                    } else {
                        jsonObject.put("Fail", "The user is not the seller of the product.");
                    }
                } else {
                    jsonObject.put("Fail", "The product has been removed.");
                }
            } else {
                jsonObject = JSONResponseGenerator.formSignedOutJSON();
            }

            transaction.commit();
        }
        return Response.ok(jsonObject.toString()).build();
    }
}
