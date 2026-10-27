package mx.velian.angur.auth;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/auth/login")
public final class LoginServlet extends HttpServlet {
    private final AuthService service = new AuthService(new JdbcUserRepository());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        LoginResult result;
        try {
            result = service.login(request.getParameter("identifier"), request.getParameter("password"));
        } catch (SQLException ex) {
            getServletContext().log("Fallo de autenticación por base de datos", ex);
            sendError(response, 503, "SERVICE_UNAVAILABLE", "No fue posible conectarse al servicio. Inténtalo más tarde.");
            return;
        }

        switch (result.status()) {
            case REQUIRED_FIELDS -> sendError(response, 400, "REQUIRED_FIELDS", "Ingresa tu usuario y contraseña.");
            case INVALID_INPUT -> sendError(response, 400, "INVALID_INPUT", "Revisa los datos ingresados.");
            case INVALID_CREDENTIALS -> sendError(response, 401, "INVALID_CREDENTIALS", "Usuario o contraseña incorrectos.");
            case SUCCESS -> {
                HttpSession previous = request.getSession(false);
                if (previous != null) {
                    previous.invalidate();
                }
                HttpSession session = request.getSession(true);
                session.setMaxInactiveInterval(30 * 60);
                session.setAttribute("userId", result.user().id());
                session.setAttribute("role", result.user().role());
                response.setStatus(200);
                response.getWriter().write("{\"ok\":true,\"name\":"
                        + Json.string(result.user().displayName())
                        + ",\"role\":" + Json.string(result.user().role()) + "}");
            }
        }
    }

    private static void sendError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"ok\":false,\"code\":" + Json.string(code)
                + ",\"message\":" + Json.string(message) + "}");
    }
}
